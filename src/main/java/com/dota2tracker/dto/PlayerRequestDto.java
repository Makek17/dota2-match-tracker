package com.dota2tracker.dto;

import com.dota2tracker.util.SteamIdUtils;
import jakarta.validation.constraints.Positive;

/**
 * DTO тіла запиту для додавання нового гравця.
 *
 * <p>Підтримує два способи ідентифікації Steam-акаунту:
 * <ol>
 *   <li><b>SteamID64</b> (рекомендовано) - 64-бітний ID зі Steam profile URL.
 *       Вкажіть {@code steam_id_64}, поле {@code steam_account_id} не потрібне.</li>
 *   <li><b>SteamID32</b> (застарілий) - 32-бітний Account ID, який OpenDota приймає напряму.
 *       Вкажіть {@code steam_account_id}, якщо {@code steam_id_64} відсутній.</li>
 * </ol>
 *
 * <pre>{@code
 * // Через SteamID64:
 * { "steam_id_64": 76561198050563298, "persona_name": "Arteezy", "tracking_enabled": true }
 *
 * // Через SteamID32 (старий спосіб):
 * { "steam_account_id": 90297570, "persona_name": "Arteezy", "tracking_enabled": true }
 * }</pre>
 */
public record PlayerRequestDto(

        /** 64-бітний SteamID зі Steam profile URL. Якщо вказаний - steamAccountId деривується автоматично. */
        Long steamId64,

        /** 32-бітний Steam Account ID для OpenDota API. Може бути null, якщо передано steamId64. */
        @Positive(message = "steamAccountId must be a positive number")
        Long steamAccountId,

        /** Нікнейм гравця (опціонально). Може бути null - оновиться при першій синхронізації. */
        String personaName,

        /** Чи активне відслідковування. null → true за замовчуванням. */
        Boolean trackingEnabled
) {
    public PlayerRequestDto {
        // Якщо переданий steamId64 - конвертуємо його в steamAccountId через формулу SteamIdUtils
        if (steamId64 != null && steamId64 > 0) {
            steamAccountId = SteamIdUtils.toAccountId(steamId64);
        }
        if (steamAccountId == null || steamAccountId <= 0) {
            throw new IllegalArgumentException(
                    "Either steam_id_64 or a positive steam_account_id must be provided.");
        }
        if (trackingEnabled == null) {
            trackingEnabled = true;
        }
    }
}
