package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.config.JwtProvider;
import com.asociaciondomitila.projects.dto.AuthSessionDto;
import com.asociaciondomitila.projects.dto.UserDto;
import com.asociaciondomitila.projects.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final JwtProvider jwtProvider;

    public AuthSessionDto createSession(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        return new AuthSessionDto(accessToken, null, UserDto.fromEntity(user));
    }
}