package com.asociaciondomitila.controller;

import com.asociaciondomitila.dto.AuthSessionDto;
import com.asociaciondomitila.dto.LoginRequest;
import com.asociaciondomitila.dto.RegisterRequest;
import com.asociaciondomitila.entity.User;
import com.asociaciondomitila.service.AuthCookieService;
import com.asociaciondomitila.service.AuthSessionService;
import com.asociaciondomitila.service.ImageService;
import com.asociaciondomitila.service.UserService;
import com.asociaciondomitila.util.ApiConstants;
import com.asociaciondomitila.util.ApiResponse;
import com.asociaciondomitila.util.ApiResponseBuilder;
import com.asociaciondomitila.util.AuthenticationHelper;
import com.asociaciondomitila.util.ValidationHelper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(ApiConstants.AUTH_ENDPOINT)
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthSessionService authSessionService;
    private final AuthCookieService authCookieService;
    private final AuthenticationHelper authenticationHelper;
    private final ImageService imageService;

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
        authCookieService.writeAuthCookie(response, session.accessToken());
        authenticationHelper.logAuthEvent(user.getEmail(), "login_successful");
        return ApiResponseBuilder.success(ApiConstants.MSG_LOGIN_SUCCESS, session);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthSessionDto>> register(
            @Valid @ModelAttribute RegisterRequest request,
            @RequestParam(required = false) String bday,
            @RequestParam(required = false) MultipartFile profilePicture,
            HttpServletResponse response
    ) {
        log.info("Intento de registro para: {} ({})", request.getEmail(), request.getNick());
        request.setBday(parseBirthDate(bday));

        User user = userService.registerUser(request);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                ValidationHelper.validateImageFile(profilePicture);
                imageService.ensureUserImageDirectory(user.getId());
                String fileName = imageService.saveProfileImage(profilePicture, user.getId());
                user = userService.updateProfileImage(user.getId(), fileName);
            } catch (Exception e) {
                log.warn("No se pudo guardar imagen de perfil post-registro para {}: {}", user.getEmail(), e.getMessage());
            }
        }

        AuthSessionDto session = authSessionService.createSession(user);
        authCookieService.writeAuthCookie(response, session.accessToken());
        return ApiResponseBuilder.created(ApiConstants.MSG_REGISTER_SUCCESS, session);
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
            Authentication authentication,
            HttpServletResponse response
    ) {
        User user = authenticationHelper.requireAuthenticatedUser(authentication);
        AuthSessionDto session = authSessionService.createSession(user);
        authCookieService.writeAuthCookie(response, session.accessToken());
        authenticationHelper.logAuthEvent(user.getEmail(), "token_refreshed");
        return ApiResponseBuilder.success(ApiConstants.MSG_TOKEN_REFRESHED, session);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);
        log.info("Logout realizado");
        return ApiResponseBuilder.success("Logout exitoso");
    }

    private static LocalDate parseBirthDate(String bday) {
        if (bday == null) {
            return null;
        }
        String trimmed = bday.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendOptional(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
                .toFormatter();

        try {
            return LocalDate.parse(trimmed, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use YYYY-MM-DD o DD/MM/YYYY");
        }
    }
}
