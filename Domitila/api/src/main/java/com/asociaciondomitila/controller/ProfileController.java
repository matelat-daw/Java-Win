package com.asociaciondomitila.controller;

import com.asociaciondomitila.dto.BooleanResultDto;
import com.asociaciondomitila.dto.ProfileDeleteRequest;
import com.asociaciondomitila.dto.UpdatePasswordRequest;
import com.asociaciondomitila.dto.UpdateProfileRequest;
import com.asociaciondomitila.dto.UserDto;
import com.asociaciondomitila.entity.User;
import com.asociaciondomitila.service.ImageService;
import com.asociaciondomitila.service.UserService;
import com.asociaciondomitila.util.ApiConstants;
import com.asociaciondomitila.util.ApiResponse;
import com.asociaciondomitila.util.ApiResponseBuilder;
import com.asociaciondomitila.util.AuthenticationHelper;
import com.asociaciondomitila.util.ValidationHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Controlador optimizado para gestión del perfil del usuario
 * Todas las responsabilidades comunes han sido centralizadas en servicios y helpers
 */
@RestController
@RequestMapping(ApiConstants.PROFILE_ENDPOINT)
@Slf4j
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ImageService imageService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping
    public ResponseEntity<ApiResponse<UserDto>> getProfile(Authentication authentication) {
        User user = authenticationHelper.requireAuthenticatedUser(authentication);
        authenticationHelper.logAuthEvent(user.getEmail(), "profile_retrieved");
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_FETCHED, UserDto.fromEntity(user));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = authenticationHelper.requireAuthenticatedUser(authentication);
        
        User updatedUser = userService.updateUserProfile(
                user.getId(),
                request.getName(),
                request.getSurname1(),
                request.getSurname2(),
                request.getPhone()
        );

        authenticationHelper.logAuthEvent(user.getEmail(), "profile_updated");
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_UPDATED, UserDto.fromEntity(updatedUser));
    }

    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDto>> updateProfilePicture(
            Authentication authentication,
            @RequestParam("profilePicture") MultipartFile file
    ) {
        User user = authenticationHelper.requireAuthenticatedUser(authentication);
        ValidationHelper.validateImageFile(file);

        try {
            imageService.ensureUserImageDirectory(user.getId());
            String fileName = imageService.saveProfileImage(file, user.getId());
            User updatedUser = userService.updateProfileImage(user.getId(), fileName);

            authenticationHelper.logAuthEvent(user.getEmail(), "profile_picture_updated");
            return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_PICTURE_UPDATED, UserDto.fromEntity(updatedUser));
        } catch (Exception e) {
            log.error("Error al guardar imagen: {}", e.getMessage());
            throw new RuntimeException(ApiConstants.ERR_IMAGE_SAVE_FAILED);
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<UserDto>> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        User user = authenticationHelper.requireAuthenticatedUser(authentication);
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las nuevas contraseñas no coinciden");
        }

        User updatedUser = userService.changePassword(
                user.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        authenticationHelper.logAuthEvent(user.getEmail(), "password_changed");
        return ApiResponseBuilder.success(ApiConstants.MSG_PASSWORD_UPDATED, UserDto.fromEntity(updatedUser));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            Authentication authentication,
            @Valid @RequestBody ProfileDeleteRequest request
    ) {
        User user = authenticationHelper.requireAuthenticatedUser(authentication);
        userService.deleteUserAccount(user.getId(), request.getPassword());
        authenticationHelper.logAuthEvent(user.getEmail(), "account_deleted");
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_DELETED);
    }

    @PostMapping("/validate-password")
    public ResponseEntity<ApiResponse<BooleanResultDto>> validatePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request
    ) {
        User user = authenticationHelper.requireAuthenticatedUser(authentication);
        
        String password = request.get("password");
        if (!ValidationHelper.isValidString(password)) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }

        boolean isValid = userService.isPasswordValid(user, password);
        return ApiResponseBuilder.success("Validación completada", new BooleanResultDto(isValid));
    }
}
