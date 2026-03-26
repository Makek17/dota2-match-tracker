package com.dota2tracker.repository;

import com.dota2tracker.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Репозиторій для сутності {@link Player}. Базовий CRUD успадкований від {@link JpaRepository}. */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /** Шукає гравця за Steam Account ID. Використовується при додаванні для перевірки дублів. */
    Optional<Player> findBySteamAccountId(Long steamAccountId);

    /** Повертає лише тих гравців, у яких увімкнене відслідковування. */
    List<Player> findAllByTrackingEnabledTrue();
}
