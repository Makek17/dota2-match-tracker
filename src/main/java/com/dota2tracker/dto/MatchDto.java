package com.dota2tracker.dto;

import java.time.Instant;

/**
 * DTO матчу для REST API-відповіді.
 * Відображає поля сутності {@link com.dota2tracker.entity.Match}
 * без JPA-внутрішніх деталей і двонаправлених колекцій.
 */
public record MatchDto(
        Long id,
        Long matchId,
        Integer duration,
        Instant startTime,
        Boolean radiantWin
) {}
