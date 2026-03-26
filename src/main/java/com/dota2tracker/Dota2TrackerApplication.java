package com.dota2tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Точка входу в застосунок Dota 2 Friend Match Tracker.
 *
 * <p>Основні можливості:
 * <ul>
 *   <li>Відслідковування Steam-акаунтів друзів</li>
 *   <li>Отримання останніх матчів через публічний OpenDota API</li>
 *   <li>Збереження історії матчів у PostgreSQL через Spring Data JPA</li>
 *   <li>REST API для запитів і ручного тригеру синхронізації</li>
 * </ul>
 */
@SpringBootApplication
public class Dota2TrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dota2TrackerApplication.class, args);
    }

    /** Спільний {@link RestTemplate} бін для HTTP-запитів до OpenDota API. */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
