package com.dota2tracker.util;

/**
 * Утиліта для конвертації Steam ID.
 *
 * <p>OpenDota API приймає 32-бітний Account ID (SteamID32),
 * а користувачі зазвичай знають свій 64-бітний SteamID64 зі Steam profile URL.
 *
 * <p>Формула: {@code SteamID32 = SteamID64 − 76561197960265728}
 *
 * <p>Магічне число {@code 76561197960265728L} (= {@code 0x0110000100000000}) -
 * фіксований базовий офсет із специфікації Steam Universe.
 *
 * <pre>{@code
 *   SteamIdUtils.toAccountId(76561198050563298L)  // → 90297570
 * }</pre>
 */
public final class SteamIdUtils {

    /** Базовий офсет SteamID64 → SteamID32. */
    private static final long STEAM_ID64_BASE = 76561197960265728L;

    private SteamIdUtils() {}

    /**
     * Конвертує SteamID64 у 32-бітний Account ID для OpenDota API.
     *
     * @throws IllegalArgumentException якщо значення менше за базовий офсет (не валідний SteamID64)
     */
    public static long toAccountId(long steamId64) {
        if (steamId64 <= STEAM_ID64_BASE) {
            throw new IllegalArgumentException(
                    "Invalid SteamID64: " + steamId64 +
                    ". Must be greater than " + STEAM_ID64_BASE);
        }
        return steamId64 - STEAM_ID64_BASE;
    }

    /** Зворотна операція: Account ID → SteamID64. */
    public static long toSteamId64(long accountId) {
        return accountId + STEAM_ID64_BASE;
    }
}
