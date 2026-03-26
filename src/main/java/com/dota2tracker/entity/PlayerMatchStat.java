package com.dota2tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Статистика конкретного гравця у конкретному матчі.
 *
 * <p>Таблиця {@code player_match_stats} вирішує зв'язок many-to-many між
 * {@link Player} і {@link Match}: один гравець може брати участь у багатьох матчах,
 * і для кожної такої пари зберігається окремий запис із KDA / GPM / XPM.
 */
@Entity
@Table(
    name = "player_match_stats",
    // Унікальна пара (гравець + матч) — один запис статистики на матч
    uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "match_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class PlayerMatchStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // ─── Ігрова статистика ───────────────────────────────────────────────────

    /** ID героя, яким гравець зіграв матч. */
    @Column(name = "hero_id")
    private Integer heroId;

    @Column(name = "kills")
    private Integer kills;

    @Column(name = "deaths")
    private Integer deaths;

    @Column(name = "assists")
    private Integer assists;

    /** Золото за хвилину (GPM). */
    @Column(name = "gpm")
    private Integer gpm;

    /** Досвід за хвилину (XPM). */
    @Column(name = "xpm")
    private Integer xpm;

    /**
     * Слот гравця у матчі (player_slot з OpenDota API).
     * 0–127 — команда Radiant, 128–255 — команда Dire.
     * Використовується разом із {@code match.radiantWin} для визначення перемоги/поразки.
     */
    @Column(name = "player_slot")
    private Integer playerSlot;

    // ─── Предмети (6 слотів) ────────────────────────────────────────────────

    @Column(name = "item0") private Integer item0;
    @Column(name = "item1") private Integer item1;
    @Column(name = "item2") private Integer item2;
    @Column(name = "item3") private Integer item3;
    @Column(name = "item4") private Integer item4;
    @Column(name = "item5") private Integer item5;
}
