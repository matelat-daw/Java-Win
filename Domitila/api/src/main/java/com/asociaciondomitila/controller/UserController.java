package com.asociaciondomitila.controller;

import com.asociaciondomitila.config.JwtProvider;
import com.asociaciondomitila.dto.RegisterRequest;
import com.asociaciondomitila.dto.UserDto;
import com.asociaciondomitila.entity.User;
import com.asociaciondomitila.enums.Role;
import com.asociaciondomitila.repository.UserRepository;
import com.asociaciondomitila.service.ImageService;
import com.asociaciondomitila.service.UserService;
import com.asociaciondomitila.util.ApiConstants;
import com.asociaciondomitila.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

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
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ImageService imageService;

    /**
     * POST /api/user/register - Registro de usuario con imagen opcional
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String nick,
            @RequestParam String name,
            @RequestParam String surname1,
            @RequestParam(required = false) String surname2,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String gender,
            @RequestParam(required = false) String bday,
            @RequestParam(required = false) MultipartFile profilePicture
    ) {
        log.info("Intento de registro para: {} ({})", email, nick);
        LocalDate birthDate = parseBirthDate(bday);

        RegisterRequest request = RegisterRequest.builder()
                .nick(nick)
                .name(name)
                .surname1(surname1)
                .surname2(surname2)
                .email(email)
                .phone(phone)
                .password(password)
                .gender(gender)
                .bday(birthDate)
                .build();

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

        String token = jwtProvider.generateToken(user.getEmail());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", ApiConstants.MSG_REGISTER_SUCCESS,
                "token", token,
                "data", UserDto.fromEntity(user)
        ));
    }

    /**
     * GET /api/user - Lista usuarios (ADMIN only, excluye ADMIN users)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<User> usersPage = userRepository.findAllExcludingRoleName(Role.ADMIN.name(), pageable);

        List<UserDto> users = usersPage.getContent().stream()
                .map(UserDto::fromEntity)
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", ApiConstants.MSG_USERS_FETCHED,
                "users", users,
                "pagination", Map.of(
                        "currentPage", usersPage.getNumber(),
                        "totalItems", usersPage.getTotalElements(),
                        "totalPages", usersPage.getTotalPages(),
                        "pageSize", usersPage.getSize(),
                        "hasNext", usersPage.hasNext(),
                        "hasPrevious", usersPage.hasPrevious()
                )
        ));
    }

    /**
     * GET /api/user/{id} - Obtener usuario por ID (ADMIN o el mismo usuario)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalStateException(ApiConstants.ERR_USER_NOT_FOUND));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", ApiConstants.MSG_PROFILE_FETCHED,
                "data", UserDto.fromEntity(user)
        ));
    }

    /**
     * PUT /api/user/{id}/role - Cambiar rol de usuario (ADMIN only)
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestParam String newRole
    ) {
        if (!ValidationHelper.isValidString(newRole)) {
            throw new IllegalArgumentException("El rol es requerido");
        }
        User updatedUser = userService.changeUserRole(id, newRole.toUpperCase());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rol actualizado exitosamente",
                "data", UserDto.fromEntity(updatedUser)
        ));
    }

    /**
     * DELETE /api/user/{id} - Eliminar usuario (ADMIN only, no permite eliminar ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalStateException(ApiConstants.ERR_USER_NOT_FOUND));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("No se puede eliminar usuarios con rol ADMIN");
        }

        userService.deleteUser(id);
        log.info("Usuario eliminado: {} (ID: {})", user.getEmail(), id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario eliminado exitosamente"
        ));
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
