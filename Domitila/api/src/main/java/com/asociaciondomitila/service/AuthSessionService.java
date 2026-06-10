package com.asociaciondomitila.service;

import com.asociaciondomitila.config.JwtProvider;
import com.asociaciondomitila.dto.AuthSessionDto;
import com.asociaciondomitila.dto.UserDto;
import com.asociaciondomitila.entity.User;
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
