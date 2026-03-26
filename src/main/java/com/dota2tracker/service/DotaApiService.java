package com.dota2tracker.service;

import com.dota2tracker.dto.OpenDotaRecentMatchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Сервіс HTTP-комунікації з публічним OpenDota API.
 * Підтримує опціональний API-ключ для підвищених лімітів запитів.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DotaApiService {

    @Value("${opendota.api.base-url}")
    private String baseUrl;

    /**
     * Опціональний API-ключ OpenDota (підвищує ліміт з ~60 до 1200 req/min).
     * Береться з {@code opendota.api.api-key} в {@code application.yml}.
     * Якщо не вказаний - запити йдуть без ключа.
     */
    @Value("${opendota.api.api-key:}")   // пустий рядок за замовчуванням
    private String apiKey;

    private final RestTemplate restTemplate;

    /** Будує повний URL із опціональним {@code ?api_key=...}. */
    private String buildUrl(String path) {
        String url = baseUrl + path;
        if (apiKey != null && !apiKey.isBlank()) {
            url += "?api_key=" + apiKey;
        }
        return url;
    }

    /**
     * Отримує список останніх матчів гравця через OpenDota API.
     * Endpoint: {@code GET /api/players/{account_id}/recentMatches}
     *
     * @param steamAccountId 32-бітний Steam Account ID
     * @return список матчів; порожній список при будь-якій помилці
     */
    public List<OpenDotaRecentMatchDto> fetchRecentMatches(Long steamAccountId) {
        String url = buildUrl("/players/" + steamAccountId + "/recentMatches");
        log.debug("Calling OpenDota API: {}", url);

        try {
            OpenDotaRecentMatchDto[] response =
                    restTemplate.getForObject(url, OpenDotaRecentMatchDto[].class);

            if (response == null) {
                log.warn("OpenDota returned null body for accountId={}", steamAccountId);
                return Collections.emptyList();
            }

            log.debug("Received {} matches for accountId={}", response.length, steamAccountId);
            return Arrays.asList(response);

        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("OpenDota 404 for accountId={}. Profile may be private.", steamAccountId);
            return Collections.emptyList();

        } catch (Exception ex) {
            log.error("Error fetching from OpenDota for accountId={}: {}",
                    steamAccountId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
