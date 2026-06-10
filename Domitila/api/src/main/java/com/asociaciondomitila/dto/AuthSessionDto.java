package com.asociaciondomitila.dto;

public record AuthSessionDto(
        String accessToken,
        String refreshToken,
        UserDto user
) {
}
