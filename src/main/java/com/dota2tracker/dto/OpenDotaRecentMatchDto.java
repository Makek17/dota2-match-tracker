package com.dota2tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для парсингу одного елемента JSON-масиву від:
 * {@code GET https://api.opendota.com/api/players/{account_id}/recentMatches}
 *
 * <p>Jackson ігнорує невідомі поля; snake_case → camelCase перетворення
 * робиться через глобальне налаштування {@code property-naming-strategy: SNAKE_CASE}
 * в {@code application.yml}. Але {@link JsonProperty} залишено явно для надійності
 * (records не завжди коректно підхоплюють глобальну стратегію).
 *
 * <p>Приклад одного елемента відповіді від OpenDota:
 * <pre>{@code
 * {
 *   "match_id": 7812345678,
 *   "duration": 1943,
 *   "start_time": 1710000000,
 *   "radiant_win": true,
 *   "hero_id": 1,
 *   "kills": 12, "deaths": 3, "assists": 8,
 *   "gold_per_min": 580, "xp_per_min": 670,
 *   "player_slot": 0, "lobby_type": 7, "game_mode": 1
 * }
 * }</pre>
 */
public record OpenDotaRecentMatchDto(

        @JsonProperty("match_id")
        Long matchId,

        @JsonProperty("duration")
        Integer duration,

        /** Unix-timestamp (секунди) початку матчу. Конвертується в {@link java.time.Instant} у сервісі. */
        @JsonProperty("start_time")
        Long startTime,

        @JsonProperty("radiant_win")
        Boolean radiantWin,

        @JsonProperty("hero_id")
        Integer heroId,

        @JsonProperty("kills")
        Integer kills,

        @JsonProperty("deaths")
        Integer deaths,

        @JsonProperty("assists")
        Integer assists,

        /** GPM — у API називається "gold_per_min". */
        @JsonProperty("gold_per_min")
        Integer goldPerMin,

        /** XPM — у API називається "xp_per_min". */
        @JsonProperty("xp_per_min")
        Integer xpPerMin,

        /**
         * Слот гравця: 0–127 = Radiant, 128–255 = Dire.
         * Зберігається в {@code PlayerMatchStat} для розрахунку перемоги/поразки.
         */
        @JsonProperty("player_slot")
        Integer playerSlot,

        /** Тип лобі: 0 — звичайний, 7 — ранг, 9 — Battle Cup. */
        @JsonProperty("lobby_type")
        Integer lobbyType,

        /** Ігровий режим: 1 — All Pick, 22 — Turbo, 23 — Single Draft. */
        @JsonProperty("game_mode")
        Integer gameMode,

        // ─── Предмети (6 слотів) ─────────────────────────────────────────────

        @JsonProperty("item_0") Integer item0,
        @JsonProperty("item_1") Integer item1,
        @JsonProperty("item_2") Integer item2,
        @JsonProperty("item_3") Integer item3,
        @JsonProperty("item_4") Integer item4,
        @JsonProperty("item_5") Integer item5
) {}
