package com.asociaciondomitila.controller;

import com.asociaciondomitila.dto.ChangeRoleRequest;
import com.asociaciondomitila.dto.PageResponse;
import com.asociaciondomitila.dto.UserDto;
import com.asociaciondomitila.entity.User;
import com.asociaciondomitila.enums.Role;
import com.asociaciondomitila.service.UserService;
import com.asociaciondomitila.util.ApiConstants;
import com.asociaciondomitila.util.ApiResponse;
import com.asociaciondomitila.util.ApiResponseBuilder;
import com.asociaciondomitila.dto.UpdateProfileRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUserProfile(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateProfileRequest request
    ) {
        User updatedUser = userService.updateUserProfileAsAdmin(id, request);
        return ApiResponseBuilder.success("Usuario actualizado exitosamente", UserDto.fromEntity(updatedUser));
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
}
