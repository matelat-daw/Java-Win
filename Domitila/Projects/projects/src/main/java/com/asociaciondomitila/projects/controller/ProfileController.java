package com.asociaciondomitila.projects.controller;

import com.asociaciondomitila.projects.dto.BooleanResultDto;
import com.asociaciondomitila.projects.dto.ProfileDeleteRequest;
import com.asociaciondomitila.projects.dto.UpdatePasswordRequest;
import com.asociaciondomitila.projects.dto.UpdateProfileRequest;
import com.asociaciondomitila.projects.dto.StaffDto;
import com.asociaciondomitila.projects.entity.Staff;
import com.asociaciondomitila.projects.service.ImageService;
import com.asociaciondomitila.projects.service.StaffService;
import com.asociaciondomitila.projects.util.ApiConstants;
import com.asociaciondomitila.projects.util.ApiResponse;
import com.asociaciondomitila.projects.util.ApiResponseBuilder;
import com.asociaciondomitila.projects.util.AuthenticationHelper;
import com.asociaciondomitila.projects.util.ValidationHelper;
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

    private final StaffService userService;
    private final ImageService imageService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping
    public ResponseEntity<ApiResponse<StaffDto>> getProfile(Authentication authentication) {
        Staff staff = authenticationHelper.requireAuthenticatedStaff(authentication);
        authenticationHelper.logAuthEvent(staff.getEmail(), "profile_retrieved");
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_FETCHED, StaffDto.fromEntity(staff));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<StaffDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Staff staff = authenticationHelper.requireAuthenticatedStaff(authentication);
        
        Staff updatedUser = userService.updateStaffProfile(
                staff.getId(),
                request.getName(),
                request.getSurname1(),
                request.getSurname2(),
                request.getPhone()
        );

        authenticationHelper.logAuthEvent(staff.getEmail(), "profile_updated");
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_UPDATED, StaffDto.fromEntity(updatedUser));
    }

    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StaffDto>> updateProfilePicture(
            Authentication authentication,
            @RequestParam("profilePicture") MultipartFile file
    ) {
        Staff staff = authenticationHelper.requireAuthenticatedStaff(authentication);
        ValidationHelper.validateImageFile(file);

        try {
            imageService.ensureStaffImageDirectory(staff.getId());
            String fileName = imageService.saveProfileImage(file, staff.getId());
            Staff updatedUser = userService.updateProfileImage(staff.getId(), fileName);

            authenticationHelper.logAuthEvent(staff.getEmail(), "profile_picture_updated");
            return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_PICTURE_UPDATED, StaffDto.fromEntity(updatedUser));
        } catch (Exception e) {
            log.error("Error al guardar imagen: {}", e.getMessage());
            throw new RuntimeException(ApiConstants.ERR_IMAGE_SAVE_FAILED);
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<StaffDto>> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        Staff staff = authenticationHelper.requireAuthenticatedStaff(authentication);
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(ApiConstants.ERR_PASSWORD_MISMATCH);
        }

        Staff updatedUser = userService.changePassword(
                staff.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        authenticationHelper.logAuthEvent(staff.getEmail(), "password_changed");
        return ApiResponseBuilder.success(ApiConstants.MSG_PASSWORD_UPDATED, StaffDto.fromEntity(updatedUser));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            Authentication authentication,
            @Valid @RequestBody ProfileDeleteRequest request
    ) {
        Staff staff = authenticationHelper.requireAuthenticatedStaff(authentication);
        userService.deleteStaffAccount(staff.getId(), request.getPassword());
        authenticationHelper.logAuthEvent(staff.getEmail(), "account_deleted");
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_DELETED);
    }

    @PostMapping("/validate-password")
    public ResponseEntity<ApiResponse<BooleanResultDto>> validatePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request
    ) {
        Staff staff = authenticationHelper.requireAuthenticatedStaff(authentication);
        
        String password = request.get("password");
        if (!ValidationHelper.isValidString(password)) {
            throw new IllegalArgumentException(ApiConstants.ERR_PASSWORD_REQUIRED);
        }

        boolean isValid = userService.isPasswordValid(staff, password);
        return ApiResponseBuilder.success(ApiConstants.MSG_PASSWORD_VALIDATION, new BooleanResultDto(isValid));
    }
}
