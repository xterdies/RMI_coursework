package com.platform.api.dto;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String email,
        String fullName,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {}
