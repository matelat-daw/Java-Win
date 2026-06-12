package com.asociaciondomitila.config;

import com.asociaciondomitila.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLES = "roles";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;

    private SecretKey key;
    private io.jsonwebtoken.JwtParser jwtParser;

    @PostConstruct
    public void init() {
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().isBlank()) {
            throw new IllegalStateException("jwt.secret no está configurado");
        }

        this.key = resolveKey(jwtProperties.getSecret());
        this.jwtParser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(jwtProperties.getIssuer())
                .build();
    }

    private static SecretKey resolveKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return Keys.hmacShaKeyFor(bytes);
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return Keys.hmacShaKeyFor(digest);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar la clave JWT", e);
        }
    }

    public String generateAccessToken(User user) {
        return createToken(
                user.getEmail(),
                jwtProperties.getExpiration(),
                Map.of(
                        CLAIM_TYPE, TOKEN_TYPE_ACCESS,
                        CLAIM_ROLES, user.getRoles().stream().map(role -> role.getName()).toList()
                )
        );
    }

    public String generateRefreshToken(User user) {
        return createToken(
                user.getEmail(),
                jwtProperties.getRefreshExpiration(),
                Map.of(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
        );
    }

    public String getEmailFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Claims getAllClaimsFromToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(getAllClaimsFromToken(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getAllClaimsFromToken(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT inválido: {}", ex.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token) && isRefreshToken(token);
    }

    private String createToken(String subject, java.time.Duration expiration, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expiration);

        return Jwts.builder()
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(key)
                .compact();
    }
}