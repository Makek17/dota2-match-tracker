package com.dota2tracker.controller;

import com.dota2tracker.dto.MatchDto;
import com.dota2tracker.dto.PlayerMatchStatDto;
import com.dota2tracker.dto.PlayerRequestDto;
import com.dota2tracker.dto.PlayerResponseDto;
import com.dota2tracker.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST-контролер для всіх ендпоінтів трекера під префіксом {@code /api/players}.
 *
 * <pre>
 * POST   /api/players              – додати нового гравця
 * GET    /api/players              – список усіх відслідковуваних гравців
 * POST   /api/players/{id}/sync   – запустити синхронізацію матчів з OpenDota
 * GET    /api/players/{id}/stats  – отримати збережену статистику гравця
 * DELETE /api/players/{id}        – видалити гравця і всю його статистику
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Додає нового гравця до списку відслідковування.
     *
     * <p>Тіло запиту (JSON):
     * <pre>{@code
     * { "steam_id_64": 76561198050563298, "persona_name": "Arteezy", "tracking_enabled": true }
     * }</pre>
     *
     * <p>Повертає {@code 201 Created} при успіху, {@code 409 Conflict} якщо акаунт вже є в базі.
     */
    @PostMapping
    public ResponseEntity<PlayerResponseDto> addPlayer(@Valid @RequestBody PlayerRequestDto dto) {
        log.info("POST /api/players – steamAccountId={}", dto.steamAccountId());
        PlayerResponseDto created = playerService.addPlayer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Повертає всіх гравців із бази. */
    @GetMapping
    public ResponseEntity<List<PlayerResponseDto>> getAllPlayers() {
        log.info("GET /api/players");
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    /**
     * Запускає синхронізацію матчів гравця через OpenDota API.
     * У відповіді — кількість нових збережених матчів:
     * <pre>{@code { "newMatchesSaved": 5 } }</pre>
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<Map<String, Object>> syncPlayer(@PathVariable Long id) {
        log.info("POST /api/players/{}/sync", id);
        int saved = playerService.syncPlayerMatches(id);
        return ResponseEntity.ok(Map.of(
                "playerId", id,
                "newMatchesSaved", saved
        ));
    }

    /** Повертає збережену статистику матчів гравця, від новіших до старіших. */
    @GetMapping("/{id}/stats")
    public ResponseEntity<List<PlayerMatchStatDto>> getPlayerStats(@PathVariable Long id) {
        log.info("GET /api/players/{}/stats", id);
        return ResponseEntity.ok(playerService.getPlayerStats(id));
    }

    /**
     * Видаляє гравця і всю його статистику.
     * Спільні записи в таблиці {@code matches} не зачіпаються.
     * Повертає {@code 204 No Content}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        log.info("DELETE /api/players/{}", id);
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
