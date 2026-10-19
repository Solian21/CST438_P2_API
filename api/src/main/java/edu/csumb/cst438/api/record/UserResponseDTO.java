package edu.csumb.cst438.api.record;

public record UserResponseDTO(
        Long id,
        String email,
        String displayName,
        String role
) {}
