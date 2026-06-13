package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.config.JwtProperties;
import com.asociaciondomitila.projects.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    private final SecurityProperties securityProperties;
    private final JwtProperties jwtProperties;

    public void writeAuthCookie(HttpServletResponse response, String accessToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                securityProperties.getCookie().getAccessTokenName(),
                accessToken,
                jwtProperties.getExpiration()
        ).toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(
                securityProperties.getCookie().getAccessTokenName(),
                "",
                Duration.ZERO
        ).toString());
    }

    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        return resolveCookieValue(request, securityProperties.getCookie().getAccessTokenName());
    }

    private Optional<String> resolveCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .path(securityProperties.getCookie().getPath())
                .httpOnly(securityProperties.getCookie().isHttpOnly())
                .secure(securityProperties.getCookie().isSecure())
                .sameSite(securityProperties.getCookie().getSameSite())
                .maxAge(maxAge)
                .build();
    }
}