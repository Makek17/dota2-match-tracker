package com.dota2tracker.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * DTO зі статистикою конкретного гравця у конкретному матчі.
 * Використовується сервісним шаром і Thymeleaf-шаблонами.
 * Хелпер-методи можна викликати прямо з th:text-виразів.
 */
public record PlayerMatchStatDto(
        Long id,
        Long matchId,
        Integer heroId,
        Integer kills,
        Integer deaths,
        Integer assists,
        Integer gpm,
        Integer xpm,
        /** Момент початку матчу (UTC). */
        Instant startTime,
        /** Тривалість матчу в секундах. */
        Integer duration,
        /** true - перемогла команда Radiant. */
        Boolean radiantWin,
        /** Тип лобі: 0 - звичайний, 7 - ранг, 9 - Battle Cup. */
        Integer lobbyType,
        /** Ігровий режим: 1 - All Pick, 22 - Turbo, 23 - Single Draft тощо. */
        Integer gameMode,
        /**
         * Слот гравця у матчі (player_slot з OpenDota API).
         * 0–127 - команда Radiant, 128–255 - команда Dire.
         * Потрібен для розрахунку перемоги/поразки конкретного гравця.
         */
        Integer playerSlot) {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault());

    /**
     * Дата початку матчу у читабельному форматі, наприклад:
     * {@code "25 Mar 2025, 21:10"}.
     */
    public String formattedDate() {
        return startTime == null ? "–" : DATE_FMT.format(startTime);
    }

    /**
     * Тривалість матчу у форматі {@code "ХХ:СС"}, наприклад 1943с →
     * {@code "32:23"}.
     */
    public String formattedDuration() {
        if (duration == null || duration <= 0)
            return "–";
        return String.format("%02d:%02d", duration / 60, duration % 60);
    }

    /**
     * Повертає читабельну назву типу лобі за числовим кодом з API:
     * 7 → "Ranked", 9 → "Battle Cup", 5 → "Team Match", решта → "Unranked".
     */
    public String lobbyLabel() {
        if (lobbyType == null)
            return "Unranked";
        return switch (lobbyType) {
            case 7 -> "Ranked";
            case 9 -> "Battle Cup";
            case 5 -> "Team Match";
            default -> "Unranked";
        };
    }

    /**
     * Визначає, чи переміг саме цей гравець у матчі.
     *
     * <p>
     * Логіка: якщо {@code playerSlot < 128} - гравець на Radiant.
     * Radiant перемагає при {@code radiantWin == true}, Dire - при {@code false}.
     *
     * @return {@code true} - перемога, {@code false} - поразка,
     *         {@code null} - дані ще не синхронізовані
     */
    public Boolean isWin() {
        if (radiantWin == null || playerSlot == null)
            return null;
        boolean isRadiant = playerSlot < 128;
        return isRadiant ? radiantWin : !radiantWin;
    }
}
