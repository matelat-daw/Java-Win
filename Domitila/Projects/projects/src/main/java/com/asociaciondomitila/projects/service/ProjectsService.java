package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.dto.ProjectsRequestDTO;
import com.asociaciondomitila.projects.dto.ProjectsResponseDTO;
import com.asociaciondomitila.projects.entity.Project;
import com.asociaciondomitila.projects.repository.ProjectsRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectsService {

    private final ProjectsRepository projectsRepository;

    public ProjectsService(ProjectsRepository projectsRepository) {
        this.projectsRepository = projectsRepository;
    }

    // Guardar un nuevo proyecto
    public ProjectsResponseDTO createProject(ProjectsRequestDTO request) {
        Project project = new Project();
        applyRequest(project, request);
        return toResponse(projectsRepository.save(project));
    }

    // Obtener todos los proyectos
    public List<ProjectsResponseDTO> getAllProjects() {
        return projectsRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    // Buscar proyecto por ID
    public Optional<ProjectsResponseDTO> getProjectById(Long id) {
        return projectsRepository.findById(id).map(this::toResponse);
    }

    // Actualizar un proyecto existente
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectsResponseDTO updateProject(Long id, ProjectsRequestDTO details) {
        return projectsRepository.findById(id).map(existingProject -> {
            applyRequest(existingProject, details);
            return toResponse(projectsRepository.save(existingProject));
        }).orElseThrow(() -> new RuntimeException("Proyecto no encontrado con el id: " + id));
    }

    // Eliminar un proyecto
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteProject(Long id) {
        if (projectsRepository.existsById(id)) {
            projectsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void applyRequest(Project project, ProjectsRequestDTO request) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setType(request.getType());
    }

    private ProjectsResponseDTO toResponse(Project project) {
        return new ProjectsResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getType()
        );
    }
}
