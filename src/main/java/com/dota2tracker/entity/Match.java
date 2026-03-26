package com.dota2tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Сутність одного Dota 2 матчу.
 *
 * <p>Один матч може мати кілька записів {@link PlayerMatchStat} -
 * по одному на кожного відслідковуваного гравця, що брав участь.
 * Поле {@code matchId} - натуральний ключ із OpenDota API,
 * використовується для дедублікації при синхронізації.
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Офіційний Dota 2 match ID з OpenDota API. Унікальний - захищає від дублів при синку. */
    @Column(name = "match_id", nullable = false, unique = true)
    private Long matchId;

    /** Тривалість матчу в секундах. */
    @Column(name = "duration")
    private Integer duration;

    /** Час початку матчу (UTC). Зберігається як TIMESTAMPTZ. */
    @Column(name = "start_time")
    private Instant startTime;

    /** true - перемогла команда Radiant. */
    @Column(name = "radiant_win")
    private Boolean radiantWin;

    /** Тип лобі: 0 - звичайний, 7 - ранг, 9 - Battle Cup. */
    @Column(name = "lobby_type")
    private Integer lobbyType;

    /** Ігровий режим: 1 -All Pick, 2 - CM, 22 - Turbo, 23 - Single Draft. */
    @Column(name = "game_mode")
    private Integer gameMode;

    /** Статистика гравців цього матчу. Cascade ALL - при видаленні матчу видаляються і stats. */
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerMatchStat> playerMatchStats = new ArrayList<>();
}
