package com.asociaciondomitila.projects.dto;

public record AuthSessionDto(
        String accessToken,
        String refreshToken,
        UserDto user
) {
}