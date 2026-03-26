package com.dota2tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Гравець (друг), якого ми відслідковуємо.
 *
 * <p>Зв'язок many-to-many з {@link Match} реалізовано через {@link PlayerMatchStat},
 * яка додатково несе KDA / GPM / XPM статистику.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 32-бітний Steam Account ID. Унікальний - один рядок на акаунт. */
    @Column(name = "steam_account_id", nullable = false, unique = true)
    private Long steamAccountId;

    /** Нікнейм у Steam. Оновлюється при синхронізації. */
    @Column(name = "persona_name")
    private String personaName;

    /** Якщо false - глобальний синк пропускає цього гравця. */
    @Column(name = "tracking_enabled", nullable = false)
    private boolean trackingEnabled = true;

    /** Статистика гравця по матчах. Cascade ALL - видалення гравця забирає і його stats. */
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerMatchStat> matchStats = new ArrayList<>();
}
