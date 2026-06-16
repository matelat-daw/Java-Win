package com.asociaciondomitila.projects.controller;

import com.asociaciondomitila.projects.dto.ProjectsRequestDTO;
import com.asociaciondomitila.projects.dto.ProjectsResponseDTO;
import com.asociaciondomitila.projects.dto.BeneficiaryAssignmentResultDTO;
import com.asociaciondomitila.projects.dto.BeneficiaryUserRequestDTO;
import com.asociaciondomitila.projects.dto.BeneficiaryUserResponseDTO;
import com.asociaciondomitila.projects.dto.IncidentRequestDTO;
import com.asociaciondomitila.projects.dto.IncidentResponseDTO;
import com.asociaciondomitila.projects.dto.UpdateIncidentStatusRequest;
import com.asociaciondomitila.projects.dto.TaskRequestDTO;
import com.asociaciondomitila.projects.dto.TaskResponseDTO;
import com.asociaciondomitila.projects.dto.UpdateTaskStatusRequest;
import com.asociaciondomitila.projects.dto.PageResponse;
import com.asociaciondomitila.projects.dto.StaffDto;
import com.asociaciondomitila.projects.entity.Staff;
import com.asociaciondomitila.projects.service.ProjectsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.asociaciondomitila.projects.util.ApiConstants;
import com.asociaciondomitila.projects.util.ApiResponse;
import com.asociaciondomitila.projects.util.ApiResponseBuilder;
import com.asociaciondomitila.projects.util.AuthenticationHelper;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectsController {

    private final ProjectsService projectsService;
    private final AuthenticationHelper authenticationHelper;

    public ProjectsController(ProjectsService projectsService, AuthenticationHelper authenticationHelper) {
        this.projectsService = projectsService;
        this.authenticationHelper = authenticationHelper;
    }

    // 1. CREAR (POST)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectsResponseDTO> createProject(@Valid @RequestBody ProjectsRequestDTO projectRequest) {
        ProjectsResponseDTO created = projectsService.createProject(projectRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 2. LEER TODOS (GET)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectsResponseDTO>>> getAllProjects(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100)
        );
        return ApiResponseBuilder.success(
                "Proyectos obtenidos exitosamente",
                projectsService.getAllProjects(currentStaff, pageable)
        );
    }

    @GetMapping("/{id}/team")
    public ResponseEntity<ApiResponse<List<StaffDto>>> getProjectTeam(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        return ApiResponseBuilder.success(
                "Equipo del proyecto obtenido exitosamente",
                projectsService.getProjectTeam(id, surname, sortDir, currentStaff)
        );
    }

    @PostMapping("/{id}/team/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffDto>>> addMember(@PathVariable Long id, @PathVariable Long staffId) {
        return ApiResponseBuilder.success(
                "Miembro agregado al proyecto exitosamente",
                projectsService.addMember(id, staffId)
        );
    }

    @DeleteMapping("/{id}/team/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffDto>>> removeMember(@PathVariable Long id, @PathVariable Long staffId) {
        return ApiResponseBuilder.success(
                "Miembro eliminado del proyecto exitosamente",
                projectsService.removeMember(id, staffId)
        );
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getProjectTasks(Authentication authentication, @PathVariable Long id) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        return ApiResponseBuilder.success(
                "Tareas del proyecto obtenidas exitosamente",
                projectsService.getProjectTasks(id, currentStaff)
        );
    }

    @GetMapping("/{id}/beneficiaries")
    public ResponseEntity<ApiResponse<PageResponse<BeneficiaryUserResponseDTO>>> getProjectBeneficiaries(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100)
        );
        return ApiResponseBuilder.success(
                "Beneficiarios del proyecto obtenidos exitosamente",
                projectsService.getProjectBeneficiaries(id, currentStaff, pageable)
        );
    }

    @PostMapping("/{id}/beneficiaries")
    public ResponseEntity<ApiResponse<BeneficiaryAssignmentResultDTO>> createProjectBeneficiary(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BeneficiaryUserRequestDTO request,
            @RequestParam(defaultValue = "false") boolean confirmExisting
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        BeneficiaryAssignmentResultDTO result = projectsService.createProjectBeneficiary(id, request, currentStaff, confirmExisting);
        return ApiResponseBuilder.success(
                result.getMessage(),
                result
        );
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO request
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        return ApiResponseBuilder.success(
                "Tarea creada exitosamente",
                projectsService.createTask(id, request, currentStaff)
        );
    }

    @PutMapping("/{projectId}/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTaskStatus(
            Authentication authentication,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        return ApiResponseBuilder.success(
                "Estado de la tarea actualizado exitosamente",
                projectsService.updateTaskStatus(projectId, taskId, request, currentStaff)
        );
    }

    @PostMapping("/{projectId}/tasks/{taskId}/incidents")
    public ResponseEntity<ApiResponse<IncidentResponseDTO>> createTaskIncident(
            Authentication authentication,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody IncidentRequestDTO request
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        return ApiResponseBuilder.created(
                "Incidencia registrada exitosamente",
                projectsService.createTaskIncident(projectId, taskId, request, currentStaff)
        );
    }

    @PutMapping("/{projectId}/tasks/{taskId}/incidents/{incidentId}/status")
    public ResponseEntity<ApiResponse<IncidentResponseDTO>> updateTaskIncidentStatus(
            Authentication authentication,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long incidentId,
            @Valid @RequestBody UpdateIncidentStatusRequest request
    ) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        return ApiResponseBuilder.success(
                "Estado de la incidencia actualizado exitosamente",
                projectsService.updateTaskIncidentStatus(projectId, taskId, incidentId, request, currentStaff)
        );
    }

    // 3. LEER UNO POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<ProjectsResponseDTO> getProjectById(Authentication authentication, @PathVariable Long id) {
        Staff currentStaff = authenticationHelper.requireAuthenticatedStaff(authentication);
        return projectsService.getProjectById(id, currentStaff)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 4. ACTUALIZAR (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<ProjectsResponseDTO> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectsRequestDTO projectDetails) {
        try {
            ProjectsResponseDTO updated = projectsService.updateProject(id, projectDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 5. ELIMINAR (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        boolean deleted = projectsService.deleteProject(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiConstants.ERR_PROJECT_NOT_FOUND);
    }
}
