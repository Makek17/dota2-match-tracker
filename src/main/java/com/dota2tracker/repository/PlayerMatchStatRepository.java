package com.dota2tracker.repository;

import com.dota2tracker.entity.PlayerMatchStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/** Репозиторій для сутності {@link PlayerMatchStat}. */
@Repository
public interface PlayerMatchStatRepository extends JpaRepository<PlayerMatchStat, Long> {

    /** Повертає всі записи статистики гравця, від новіших до старіших. */
    List<PlayerMatchStat> findAllByPlayerIdOrderByMatchStartTimeDesc(Long playerId);

    /**
     * Перевіряє, чи вже є запис статистики для цього гравця + матчу.
     * Один DB-запит через унікальний індекс - O(1), замість попереднього in-memory скану.
     */
    boolean existsByPlayerIdAndMatchMatchId(Long playerId, Long matchId);

    /**
     * Повертає статистику гравця для матчів, що почалися після {@code cutoff}.
     * Наприклад, для фільтру "За тиждень": {@code Instant.now().minus(7, ChronoUnit.DAYS)}.
     */
    List<PlayerMatchStat> findAllByPlayerIdAndMatchStartTimeAfterOrderByMatchStartTimeDesc(
            Long playerId, Instant cutoff);

    /**
     * Агрегована статистика гравця за вказаний часовий проміжок.
     *
     * <p>Повертає один рядок Object[] з індексами:
     * [0] avgKills, [1] avgDeaths, [2] avgAssists, [3] avgGpm, [4] avgXpm, [5] matchCount
     *
     * @param cutoff для ALL_TIME передавати {@code Instant.EPOCH}
     */
    @Query("""
            SELECT
                AVG(s.kills),
                AVG(s.deaths),
                AVG(s.assists),
                AVG(s.gpm),
                AVG(s.xpm),
                COUNT(s)
            FROM PlayerMatchStat s
            WHERE s.player.id = :playerId
              AND s.match.startTime > :cutoff
            """)
    List<Object[]> aggregateStatsForPlayer(@Param("playerId") Long playerId,
                                           @Param("cutoff") Instant cutoff);
}
