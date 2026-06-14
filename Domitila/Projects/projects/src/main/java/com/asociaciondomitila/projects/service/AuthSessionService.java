package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.config.JwtProvider;
import com.asociaciondomitila.projects.dto.AuthSessionDto;
import com.asociaciondomitila.projects.dto.StaffDto;
import com.asociaciondomitila.projects.entity.Staff;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final JwtProvider jwtProvider;

    public AuthSessionDto createSession(Staff staff) {
        String accessToken = jwtProvider.generateAccessToken(staff);
        return new AuthSessionDto(accessToken, null, StaffDto.fromEntity(staff));
    }
}