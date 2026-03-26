package com.dota2tracker.repository;

import com.dota2tracker.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Репозиторій для сутності {@link Match}. */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * Перевіряє, чи вже є матч із таким Dota 2 match ID.
     * Використовується сервісом синхронізації для захисту від дублів.
     */
    boolean existsByMatchId(Long matchId);

    /**
     * Знаходить матч за Dota 2 match ID.
     * Потрібен для прив'язки нового запису {@code PlayerMatchStat} до вже існуючого матчу.
     */
    Optional<Match> findByMatchId(Long matchId);

    // Калер сам рахує cutoff = Instant.now().minus(period) і передає сюди.
    // Для ALL_TIME - використовується findAll() з JpaRepository.

    /** Знаходить матчі, що почалися після {@code cutoff}, від новіших до старіших. */
    List<Match> findAllByStartTimeAfterOrderByStartTimeDesc(Instant cutoff);
}
