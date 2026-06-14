package com.asociaciondomitila.projects.util;

import com.asociaciondomitila.projects.config.JwtProvider;
import com.asociaciondomitila.projects.entity.Staff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.asociaciondomitila.projects.service.StaffService;
import java.util.Optional;

/**
 * Utilidad centralizada para operaciones relacionadas con autenticación
 * Elimina validaciones repetidas en los controllers
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationHelper {

    private final StaffService userService;
    private final JwtProvider jwtProvider;

    /**
     * Valida que el usuario esté autenticado
     * @return true si está autenticado, false si no
     */
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Obtiene el email del usuario autenticado
     */
    public String getAuthenticatedEmail(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Obtiene el usuario autenticado actual
     */
    public Optional<Staff> getAuthenticatedStaff(Authentication authentication) {
        String email = getAuthenticatedEmail(authentication);
        if (email == null) {
            return Optional.empty();
        }
        return userService.getStaffByEmail(email);
    }

    /**
     * Valida autenticación y retorna el usuario o lanza excepción
     */
    public Staff requireAuthenticatedStaff(Authentication authentication) {
        return getAuthenticatedStaff(authentication)
                .orElseThrow(() -> new IllegalStateException(ApiConstants.ERR_STAFF_NOT_FOUND));
    }

    /**
     * Log de evento de autenticación
     */
    public void logAuthEvent(String email, String action) {
        log.info("Auth event for staff '{}': {}", email, action);
    }

    /**
     * Log de intento de acceso no autorizado
     */
    public void logUnauthorizationAttempt(String reason) {
        log.warn("Unauthorized access attempt: {}", reason);
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return jwtProvider.isRefreshTokenValid(refreshToken);
    }

    public String getEmailFromToken(String token) {
        return jwtProvider.getEmailFromToken(token);
    }
}