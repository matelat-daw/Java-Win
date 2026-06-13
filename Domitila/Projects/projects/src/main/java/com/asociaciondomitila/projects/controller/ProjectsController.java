package com.asociaciondomitila.projects.controller;

import com.asociaciondomitila.projects.dto.ProjectsRequestDTO;
import com.asociaciondomitila.projects.dto.ProjectsResponseDTO;
import com.asociaciondomitila.projects.service.ProjectsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.asociaciondomitila.projects.util.ApiConstants;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectsController {

    private final ProjectsService projectsService;

    public ProjectsController(ProjectsService projectsService) {
        this.projectsService = projectsService;
    }

    // 1. CREAR (POST)
    @PostMapping
    public ResponseEntity<ProjectsResponseDTO> createProject(@Valid @RequestBody ProjectsRequestDTO projectRequest) {
        // El controlador solo delega el DTO de entrada al servicio
        ProjectsResponseDTO created = projectsService.createProject(projectRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 2. LEER TODOS (GET)
    @GetMapping
    public ResponseEntity<List<ProjectsResponseDTO>> getAllProjects() {
        // El servicio ya se encarga de devolver la lista convertida en DTOs
        List<ProjectsResponseDTO> projects = projectsService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    // 3. LEER UNO POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<ProjectsResponseDTO> getProjectById(@PathVariable Long id) {
        return projectsService.getProjectById(id)
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
