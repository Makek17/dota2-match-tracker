package com.dota2tracker.service;

import com.dota2tracker.dto.*;
import com.dota2tracker.entity.Match;
import com.dota2tracker.entity.Player;
import com.dota2tracker.entity.PlayerMatchStat;
import com.dota2tracker.repository.MatchRepository;
import com.dota2tracker.repository.PlayerMatchStatRepository;
import com.dota2tracker.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Основний сервіс для управління гравцями і синхронізації матчів.
 *
 * <p>Відповідає за CRUD гравців, синхронізацію через OpenDota API,
 * фільтрацію статистики за часом і агрегацію KDA/GPM/XPM.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final PlayerMatchStatRepository statRepository;
    private final DotaApiService dotaApiService;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    /**
     * Додає нового гравця до відслідковування.
     * Кидає {@link IllegalArgumentException}, якщо такий акаунт вже є в базі.
     */
    @Transactional
    public PlayerResponseDto addPlayer(PlayerRequestDto dto) {
        playerRepository.findBySteamAccountId(dto.steamAccountId()).ifPresent(existing -> {
            throw new IllegalArgumentException(
                    "Player with steamAccountId=" + dto.steamAccountId() + " is already tracked.");
        });

        Player player = new Player();
        player.setSteamAccountId(dto.steamAccountId());
        player.setPersonaName(dto.personaName());
        player.setTrackingEnabled(dto.trackingEnabled());

        Player saved = playerRepository.save(player);
        log.info("Added new tracked player: id={}, steamAccountId={}", saved.getId(), saved.getSteamAccountId());
        return toResponseDto(saved);
    }

    /** Повертає всіх гравців із бази (незалежно від trackingEnabled). */
    @Transactional(readOnly = true)
    public List<PlayerResponseDto> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    // ─── Синхронізація ───────────────────────────────────────────────────────

    /**
     * Завантажує останні матчі гравця з OpenDota API і зберігає нові в базу.
     *
     * <p>Алгоритм: отримуємо до 20 матчів → для кожного перевіряємо наявність у БД →
     * зберігаємо тільки нові {@link Match} і {@link PlayerMatchStat}.
     * Якщо матч вже є (наприклад, доданий іншим гравцем) - тільки додаємо stat-рядок.
     *
     * @return кількість нових збережених матчів
     */
    @Transactional
    public int syncPlayerMatches(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: id=" + playerId));

        log.info("Syncing matches for player id={}, steamAccountId={}", playerId, player.getSteamAccountId());

        List<OpenDotaRecentMatchDto> recentMatches =
                dotaApiService.fetchRecentMatches(player.getSteamAccountId());

        int newMatchCount = 0;

        for (OpenDotaRecentMatchDto dto : recentMatches) {
            boolean matchAlreadyExists = matchRepository.existsByMatchId(dto.matchId());

            Match match;
            if (matchAlreadyExists) {
                match = matchRepository.findByMatchId(dto.matchId()).orElseThrow();
                log.debug("Match {} already exists – skipping match creation, checking stats.", dto.matchId());
            } else {
                match = new Match();
                match.setMatchId(dto.matchId());
                match.setDuration(dto.duration());
                match.setRadiantWin(dto.radiantWin());
                match.setLobbyType(dto.lobbyType());
                match.setGameMode(dto.gameMode());
                if (dto.startTime() != null) {
                    match.setStartTime(Instant.ofEpochSecond(dto.startTime()));
                }
                match = matchRepository.save(match);
                newMatchCount++;
                log.debug("Saved new match: matchId={}", dto.matchId());
            }

            // Перевірка дедублікації: один DB-запит через унікальний індекс - O(1)
            boolean statExists = statRepository.existsByPlayerIdAndMatchMatchId(player.getId(), dto.matchId());

            if (!statExists) {
                PlayerMatchStat stat = new PlayerMatchStat();
                stat.setPlayer(player);
                stat.setMatch(match);
                stat.setHeroId(dto.heroId());
                stat.setKills(dto.kills());
                stat.setDeaths(dto.deaths());
                stat.setAssists(dto.assists());
                stat.setGpm(dto.goldPerMin());
                stat.setXpm(dto.xpPerMin());
                stat.setPlayerSlot(dto.playerSlot());   // потрібен для розрахунку Win/Loss
                statRepository.save(stat);
                log.debug("Saved stats for player={} match={}", playerId, dto.matchId());
            }
        }

        log.info("Sync complete for player id={}: {} new matches saved.", playerId, newMatchCount);
        return newMatchCount;
    }

    // ─── Статистика ──────────────────────────────────────────────────────────

    /** Часові фільтри для запитів статистики. */
    public enum Period {
        LAST_WEEK, LAST_MONTH, LAST_YEAR, ALL_TIME
    }

    /** Повертає статистику матчів гравця за вказаний проміжок, від новіших до старіших. */
    @Transactional(readOnly = true)
    public List<PlayerMatchStatDto> getPlayerStats(Long playerId, Period period) {
        if (!playerRepository.existsById(playerId)) {
            throw new IllegalArgumentException("Player not found: id=" + playerId);
        }

        List<PlayerMatchStat> rows;
        if (period == null || period == Period.ALL_TIME) {
            rows = statRepository.findAllByPlayerIdOrderByMatchStartTimeDesc(playerId);
        } else {
            Instant cutoff = cutoffFor(period);
            rows = statRepository
                    .findAllByPlayerIdAndMatchStartTimeAfterOrderByMatchStartTimeDesc(playerId, cutoff);
        }

        return rows.stream().map(this::toStatDto).toList();
    }

    /**
     * Перевантаження з рядковим period - для URL-параметрів і Thymeleaf-форм.
     * Невідоме значення → ALL_TIME.
     */
    @Transactional(readOnly = true)
    public List<PlayerMatchStatDto> getPlayerStats(Long playerId, String periodName) {
        Period period;
        try {
            period = (periodName == null) ? Period.ALL_TIME : Period.valueOf(periodName.toUpperCase());
        } catch (IllegalArgumentException e) {
            period = Period.ALL_TIME;
        }
        return getPlayerStats(playerId, period);
    }

    /** Перевантаження без фільтру - повертає ALL_TIME. */
    @Transactional(readOnly = true)
    public List<PlayerMatchStatDto> getPlayerStats(Long playerId) {
        return getPlayerStats(playerId, Period.ALL_TIME);
    }

    // ─── Агрегація ───────────────────────────────────────────────────────────

    /**
     * Агрегована статистика за вказаний проміжок.
     * [0] avgKills [1] avgDeaths [2] avgAssists [3] avgGpm [4] avgXpm [5] matchCount
     */
    public record AggregatedStats(
            long matchCount,
            double avgKills,
            double avgDeaths,
            double avgAssists,
            double avgKda,
            double avgGpm,
            double avgXpm
    ) {}

    /** Рахує середні показники (KDA, GPM, XPM) і кількість матчів. Якщо матчів немає - всі нулі. */
    @Transactional(readOnly = true)
    public AggregatedStats getAggregatedStats(Long playerId, String periodName) {
        if (!playerRepository.existsById(playerId)) {
            throw new IllegalArgumentException("Player not found: id=" + playerId);
        }

        Period period;
        try {
            period = (periodName == null) ? Period.ALL_TIME : Period.valueOf(periodName.toUpperCase());
        } catch (IllegalArgumentException e) {
            period = Period.ALL_TIME;
        }

        // Для ALL_TIME передаємо Instant.EPOCH, щоб запит покрив усю історію
        Instant cutoff = (period == Period.ALL_TIME) ? Instant.EPOCH : cutoffFor(period);

        Object[] row = statRepository.aggregateStatsForPlayer(playerId, cutoff)
                .stream().findFirst().orElse(null);

        if (row == null || row[5] == null || ((Number) row[5]).longValue() == 0) {
            return new AggregatedStats(0, 0, 0, 0, 0, 0, 0);
        }

        double avgKills   = toDouble(row[0]);
        double avgDeaths  = toDouble(row[1]);
        double avgAssists = toDouble(row[2]);
        double avgGpm     = toDouble(row[3]);
        double avgXpm     = toDouble(row[4]);
        long   count      = ((Number) row[5]).longValue();

        // KDA: (kills + assists) / max(deaths, 1)
        double avgKda = (avgKills + avgAssists) / Math.max(avgDeaths, 1.0);

        return new AggregatedStats(count, round2(avgKills), round2(avgDeaths),
                round2(avgAssists), round2(avgKda), round2(avgGpm), round2(avgXpm));
    }

    // ─── Приватні хелпери ────────────────────────────────────────────────────

    private static Instant cutoffFor(Period period) {
        return switch (period) {
            case LAST_WEEK  -> Instant.now().minus(7,   ChronoUnit.DAYS);
            case LAST_MONTH -> Instant.now().minus(30,  ChronoUnit.DAYS);
            case LAST_YEAR  -> Instant.now().minus(365, ChronoUnit.DAYS);
            case ALL_TIME   -> Instant.EPOCH;
        };
    }

    private static double toDouble(Object obj) {
        return obj == null ? 0.0 : ((Number) obj).doubleValue();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private PlayerResponseDto toResponseDto(Player p) {
        return new PlayerResponseDto(p.getId(), p.getSteamAccountId(),
                p.getPersonaName(), p.isTrackingEnabled());
    }

    private PlayerMatchStatDto toStatDto(PlayerMatchStat s) {
        return new PlayerMatchStatDto(
                s.getId(), s.getMatch().getMatchId(), s.getHeroId(),
                s.getKills(), s.getDeaths(), s.getAssists(),
                s.getGpm(), s.getXpm(),
                s.getMatch().getStartTime(), s.getMatch().getDuration(),
                s.getMatch().getRadiantWin(), s.getMatch().getLobbyType(),
                s.getMatch().getGameMode(), s.getPlayerSlot()
        );
    }

    // ─── Видалення гравця ────────────────────────────────────────────────────

    /**
     * Видаляє гравця і всі його stat-рядки.
     * Записи в таблиці {@code matches} не чіпаємо - вони можуть належати іншим гравцям.
     */
    @Transactional
    public void deletePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: id=" + playerId));

        // Спершу видаляємо stat-рядки, щоб не порушити FK-обмеження
        List<PlayerMatchStat> stats = statRepository.findAllByPlayerIdOrderByMatchStartTimeDesc(playerId);
        statRepository.deleteAll(stats);

        playerRepository.delete(player);
        log.info("Deleted player id={} and {} stat rows.", playerId, stats.size());
    }
}
