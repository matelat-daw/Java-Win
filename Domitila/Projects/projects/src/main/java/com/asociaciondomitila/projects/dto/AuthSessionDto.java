package com.asociaciondomitila.projects.dto;

public record AuthSessionDto(
        String accessToken,
        String refreshToken,
        StaffDto staff
) {
}