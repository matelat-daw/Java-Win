package com.asociaciondomitila.projects.controller;

import com.asociaciondomitila.projects.dto.ChangeRoleRequest;
import com.asociaciondomitila.projects.dto.PageResponse;
import com.asociaciondomitila.projects.dto.StaffDto;
import com.asociaciondomitila.projects.entity.Staff;
import com.asociaciondomitila.projects.enums.Role;
import com.asociaciondomitila.projects.service.StaffService;
import com.asociaciondomitila.projects.util.ApiConstants;
import com.asociaciondomitila.projects.util.ApiResponse;
import com.asociaciondomitila.projects.util.ApiResponseBuilder;
import com.asociaciondomitila.projects.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping(ApiConstants.STAFF_ENDPOINT)
@Slf4j
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<StaffDto>>> getAllStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "surname1") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        String safeSortBy = switch (sortBy) {
            case "surname1", "surname2", "name", "email", "phone", "createdAt", "nick" -> sortBy;
            default -> "surname1";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));

        Page<Staff> staffPage = staffService.getStaffExcludingRoleBySurname(Role.ADMIN, surname, pageable);

        List<StaffDto> staff = staffPage.getContent().stream()
                .map(StaffDto::fromEntity)
                .toList();

        return ApiResponseBuilder.success(
                ApiConstants.MSG_STAFF_FETCHED,
                new PageResponse<>(
                        staff,
                        staffPage.getNumber(),
                        staffPage.getTotalElements(),
                        staffPage.getTotalPages(),
                        staffPage.getSize(),
                        staffPage.hasNext(),
                        staffPage.hasPrevious()
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffDto>> getStaffById(@PathVariable Long id) {
        Staff staff = staffService.getRequiredStaffById(id);
        return ApiResponseBuilder.success(ApiConstants.MSG_PROFILE_FETCHED, StaffDto.fromEntity(staff));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffDto>> updateStaffProfile(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateProfileRequest request
    ) {
        Staff updatedStaff = staffService.updateStaffProfileAsAdmin(id, request);
        return ApiResponseBuilder.success("Staff actualizado exitosamente", StaffDto.fromEntity(updatedStaff));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffDto>> changeStaffRole(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ChangeRoleRequest request
    ) {
        if (!id.equals(request.getStaffId())) {
            throw new IllegalArgumentException("El ID del path no coincide con el cuerpo de la petición");
        }
        Staff updatedStaff = staffService.changeStaffRole(id, request.getNewRole());
        return ApiResponseBuilder.success("Rol actualizado exitosamente", StaffDto.fromEntity(updatedStaff));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        log.info("Staff eliminado con ID: {}", id);
        return ApiResponseBuilder.success("Staff eliminado exitosamente");
    }
}
