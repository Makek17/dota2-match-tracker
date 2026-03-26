package com.dota2tracker.dto;

/**
 * DTO відповіді API при поверненні даних гравця.
 * Ізолює внутрішню сутність {@link com.dota2tracker.entity.Player} від зовнішнього API.
 */
public record PlayerResponseDto(
        Long id,
        Long steamAccountId,
        String personaName,
        boolean trackingEnabled
) {}
