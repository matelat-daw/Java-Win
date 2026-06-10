package com.asociaciondomitila.controller;

import com.asociaciondomitila.dto.AuthSessionDto;
import com.asociaciondomitila.dto.ChangeRoleRequest;
import com.asociaciondomitila.dto.PageResponse;
import com.asociaciondomitila.dto.RegisterRequest;
import com.asociaciondomitila.dto.UserDto;
import com.asociaciondomitila.entity.User;
import com.asociaciondomitila.enums.Role;
import com.asociaciondomitila.service.AuthCookieService;
import com.asociaciondomitila.service.AuthSessionService;
import com.asociaciondomitila.service.ImageService;
import com.asociaciondomitila.service.UserService;
import com.asociaciondomitila.util.ApiConstants;
import com.asociaciondomitila.util.ApiResponse;
import com.asociaciondomitila.util.ApiResponseBuilder;
import com.asociaciondomitila.util.ValidationHelper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controlador optimizado para gestión de usuarios
 * Maneja registro, consultas y operaciones administrativas
 */
@RestController
@RequestMapping(ApiConstants.USER_ENDPOINT)
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthSessionService authSessionService;
    private final AuthCookieService authCookieService;
    private final ImageService imageService;

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
        authCookieService.writeAuthenticationCookies(response, session.accessToken(), session.refreshToken());
        return ApiResponseBuilder.created(ApiConstants.MSG_REGISTER_SUCCESS, session);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<User> usersPage = userService.getUsersExcludingRole(Role.ADMIN, pageable);

        List<UserDto> users = usersPage.getContent().stream()
                .map(UserDto::fromEntity)
                .toList();

        return ApiResponseBuilder.success(
                ApiConstants.MSG_USERS_FETCHED,
                new PageResponse<>(
                        users,
                        usersPage.getNumber(),
                        usersPage.getTotalElements(),
                        usersPage.getTotalPages(),
                        usersPage.getSize(),
                        usersPage.hasNext(),
                        usersPage.hasPrevious()
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        User user = userService.getRequiredUserById(id);
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_FETCHED, UserDto.fromEntity(user));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> changeUserRole(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ChangeRoleRequest request
    ) {
        if (!id.equals(request.getUserId())) {
            throw new IllegalArgumentException("El ID del path no coincide con el cuerpo de la petición");
        }
        User updatedUser = userService.changeUserRole(id, request.getNewRole());
        return ApiResponseBuilder.success("Rol actualizado exitosamente", UserDto.fromEntity(updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        log.info("Usuario eliminado con ID: {}", id);
        return ApiResponseBuilder.success("Usuario eliminado exitosamente");
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
