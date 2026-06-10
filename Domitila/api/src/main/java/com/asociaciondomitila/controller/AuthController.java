package com.asociaciondomitila.controller;

import com.asociaciondomitila.dto.AuthSessionDto;
import com.asociaciondomitila.dto.LoginRequest;
import com.asociaciondomitila.entity.User;
import com.asociaciondomitila.service.AuthCookieService;
import com.asociaciondomitila.service.AuthSessionService;
import com.asociaciondomitila.service.UserService;
import com.asociaciondomitila.util.ApiConstants;
import com.asociaciondomitila.util.ApiResponse;
import com.asociaciondomitila.util.ApiResponseBuilder;
import com.asociaciondomitila.util.AuthenticationHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(ApiConstants.AUTH_ENDPOINT)
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthSessionService authSessionService;
    private final AuthCookieService authCookieService;
    private final AuthenticationHelper authenticationHelper;

    @Value("${app.frontend.login-url:http://localhost/login}")
    private String frontendLoginUrl;

    @Value("${app.frontend.register-url:http://localhost/register}")
    private String frontendRegisterUrl;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthSessionDto>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        AuthSessionDto session = authSessionService.createSession(user);
        authCookieService.writeAuthenticationCookies(response, session.accessToken(), session.refreshToken());
        authenticationHelper.logAuthEvent(user.getEmail(), "login_successful");
        return ApiResponseBuilder.success(ApiConstants.MSG_LOGIN_SUCCESS, session);
    }

    @GetMapping("/verify/{token}")
    public void verifyEmail(@PathVariable String token, HttpServletResponse response) throws IOException {
        try {
            userService.verifyEmail(token);
            response.sendRedirect(frontendLoginUrl + "?verified=1");
        } catch (Exception e) {
            log.error("Error en verificación: {}", e.getMessage());
            response.sendRedirect(frontendRegisterUrl + "?verification=failed");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthSessionDto>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = authCookieService.resolveRefreshToken(request)
                .orElseThrow(() -> new IllegalArgumentException(ApiConstants.ERR_INVALID_TOKEN));
        if (!authenticationHelper.isRefreshTokenValid(refreshToken)) {
            throw new IllegalArgumentException(ApiConstants.ERR_INVALID_TOKEN);
        }

        User user = userService.getRequiredUserByEmail(authenticationHelper.getEmailFromToken(refreshToken));
        AuthSessionDto session = authSessionService.createSession(user);
        authCookieService.writeAuthenticationCookies(response, session.accessToken(), session.refreshToken());
        authenticationHelper.logAuthEvent(user.getEmail(), "token_refreshed");
        return ApiResponseBuilder.success(ApiConstants.MSG_TOKEN_REFRESHED, session);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authCookieService.clearAuthenticationCookies(response);
        log.info("Logout realizado");
        return ApiResponseBuilder.success("Logout exitoso");
    }
}
