package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.dto.ProjectsRequestDTO;
import com.asociaciondomitila.projects.dto.ProjectsResponseDTO;
import com.asociaciondomitila.projects.dto.TaskRequestDTO;
import com.asociaciondomitila.projects.dto.TaskResponseDTO;
import com.asociaciondomitila.projects.dto.UpdateTaskStatusRequest;
import com.asociaciondomitila.projects.dto.StaffDto;
import com.asociaciondomitila.projects.entity.Project;
import com.asociaciondomitila.projects.entity.Task;
import com.asociaciondomitila.projects.entity.Staff;
import com.asociaciondomitila.projects.repository.ProjectsRepository;
import com.asociaciondomitila.projects.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectsService {

    private final ProjectsRepository projectsRepository;
    private final TaskRepository taskRepository;
    private final StaffService staffService;

    public ProjectsService(
            ProjectsRepository projectsRepository,
            TaskRepository taskRepository,
            StaffService staffService
    ) {
        this.projectsRepository = projectsRepository;
        this.taskRepository = taskRepository;
        this.staffService = staffService;
    }

    // Guardar un nuevo proyecto
    @Transactional
    public ProjectsResponseDTO createProject(ProjectsRequestDTO request) {
        Project project = new Project();
        applyRequest(project, request);
        return toResponse(projectsRepository.save(project));
    }

    // Obtener todos los proyectos
    @Transactional(readOnly = true)
    public List<ProjectsResponseDTO> getAllProjects(Staff currentStaff) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Project> projects = isAdmin(currentStaff)
                ? projectsRepository.findAll(sort)
                : projectsRepository.findAccessibleProjectsByStaffId(currentStaff.getId(), sort);

        return projects.stream()
                .map(project -> toResponse(project, currentStaff))
                .toList();
    }

    // Buscar proyecto por ID
    @Transactional(readOnly = true)
    public Optional<ProjectsResponseDTO> getProjectById(Long id, Staff currentStaff) {
        return projectsRepository.findById(id)
                .filter(project -> canAccessProject(project, currentStaff))
                .map(project -> toResponse(project, currentStaff));
    }

    @Transactional(readOnly = true)
    public List<StaffDto> getProjectTeam(Long projectId, String surname, String sortDir, Staff currentStaff) {
        Project project = getAccessibleProject(projectId, currentStaff);
        String search = surname == null ? "" : surname.trim().toLowerCase();
        Comparator<Staff> comparator = Comparator
                .comparing((Staff staff) -> safeLower(staff.getSurname1()))
                .thenComparing(staff -> safeLower(staff.getSurname2()))
                .thenComparing(staff -> safeLower(staff.getName()));

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        return project.getTeamMembers() == null ? List.of() : project.getTeamMembers().stream()
                .filter(staff -> search.isBlank()
                        || safeLower(staff.getSurname1()).contains(search)
                        || safeLower(staff.getSurname2()).contains(search))
                .sorted(comparator)
                .map(StaffDto::fromEntity)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffDto> addMember(Long projectId, Long staffId) {
        Project project = getRequiredProject(projectId);
        Staff staff = staffService.getRequiredStaffById(staffId);

        if (project.getTeamMembers() == null) {
            project.setTeamMembers(new ArrayList<>());
        }

        boolean alreadyAssigned = project.getTeamMembers().stream().anyMatch(member -> member.getId().equals(staffId));
        if (!alreadyAssigned) {
            project.getTeamMembers().add(staff);
        }

        return projectsRepository.save(project).getTeamMembers().stream()
                .map(StaffDto::fromEntity)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffDto> removeMember(Long projectId, Long staffId) {
        Project project = getRequiredProject(projectId);
        if (project.getTeamMembers() != null) {
            project.getTeamMembers().removeIf(member -> member.getId().equals(staffId));
        }

        if (project.getTasks() != null) {
            project.getTasks().forEach(task -> {
                if (task.getAssignedStaff() != null && task.getAssignedStaff().getId().equals(staffId)) {
                    task.setAssignedStaff(null);
                }
            });
        }

        return projectsRepository.save(project).getTeamMembers() == null ? List.of() : project.getTeamMembers().stream()
                .map(StaffDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getProjectTasks(Long projectId, Staff currentStaff) {
        Project project = getAccessibleProject(projectId, currentStaff);
        return taskRepository.findByProjectIdOrderByDueDateAscIdAsc(projectId).stream()
                .filter(task -> canViewTask(task, currentStaff, project))
                .map(TaskResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public TaskResponseDTO createTask(Long projectId, TaskRequestDTO request, Staff currentStaff) {
        if (!isAdmin(currentStaff)) {
            throw new AccessDeniedException("Solo un administrador puede crear tareas");
        }

        Project project = getRequiredProject(projectId);

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setStatus(normalizeTaskStatus(request.getStatus()));
        task.setDueDate(request.getDueDate());
        task.setAssignedStaff(resolveAssignedStaff(project, request.getAssignedStaffId()));

        return TaskResponseDTO.fromEntity(taskRepository.save(task));
    }

    @Transactional
    public TaskResponseDTO updateTaskStatus(Long projectId, Long taskId, UpdateTaskStatusRequest request, Staff currentStaff) {
        Project project = getAccessibleProject(projectId, currentStaff);
        Task task = getRequiredTask(taskId);
        if (task.getProject() == null || !task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("La tarea no pertenece al proyecto indicado");
        }
        if (!canUpdateTask(task, currentStaff, project)) {
            throw new AccessDeniedException("No tienes permisos para actualizar esta tarea");
        }

        task.setStatus(normalizeTaskStatus(request.getStatus()));
        return TaskResponseDTO.fromEntity(taskRepository.save(task));
    }

    // Actualizar un proyecto existente
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProjectsResponseDTO updateProject(Long id, ProjectsRequestDTO details) {
        return projectsRepository.findById(id).map(existingProject -> {
            applyRequest(existingProject, details);
            return toResponse(projectsRepository.save(existingProject));
        }).orElseThrow(() -> new RuntimeException("Proyecto no encontrado con el id: " + id));
    }

    // Eliminar un proyecto
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public boolean deleteProject(Long id) {
        if (projectsRepository.existsById(id)) {
            projectsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void applyRequest(Project project, ProjectsRequestDTO request) {
        project.setCode(blankToNull(request.getCode()));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setType(request.getType());
    }

    private ProjectsResponseDTO toResponse(Project project, Staff currentStaff) {
        return new ProjectsResponseDTO(
                project.getId(),
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getType(),
                project.getTeamMembers() != null ? project.getTeamMembers().size() : 0,
                countVisibleTasks(project, currentStaff)
        );
    }

    private ProjectsResponseDTO toResponse(Project project) {
        return toResponse(project, null);
    }

    private Project getRequiredProject(Long projectId) {
        return projectsRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));
    }

    private Task getRequiredTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));
    }

    private Project getAccessibleProject(Long projectId, Staff currentStaff) {
        Project project = getRequiredProject(projectId);
        if (!canAccessProject(project, currentStaff)) {
            throw new AccessDeniedException("No tienes acceso a este proyecto");
        }
        return project;
    }

    private Staff resolveAssignedStaff(Project project, Long assignedStaffId) {
        if (assignedStaffId == null) {
            return null;
        }

        Staff staff = staffService.getRequiredStaffById(assignedStaffId);
        boolean belongsToTeam = project.getTeamMembers() != null
                && project.getTeamMembers().stream().anyMatch(member -> member.getId().equals(assignedStaffId));
        boolean isProjectManager = project.getProjectManager() != null
                && project.getProjectManager().getId().equals(assignedStaffId);

        if (!belongsToTeam && !isProjectManager) {
            throw new IllegalArgumentException("La tarea solo se puede asignar a miembros del proyecto");
        }

        return staff;
    }

    private String normalizeTaskStatus(String status) {
        if (status == null || status.isBlank()) {
            return "POR_HACER";
        }

        return switch (status.trim().toUpperCase()) {
            case "TODO", "POR_HACER" -> "POR_HACER";
            case "IN_PROGRESS", "EN_PROGRESO" -> "EN_PROGRESO";
            case "DONE", "TERMINADO" -> "TERMINADO";
            default -> status.trim().toUpperCase();
        };
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isAdmin(Staff staff) {
        return staff != null && com.asociaciondomitila.projects.enums.Role.ADMIN.equals(staff.getRole());
    }

    private boolean canAccessProject(Project project, Staff currentStaff) {
        if (project == null || currentStaff == null) {
            return false;
        }
        if (isAdmin(currentStaff)) {
            return true;
        }

        boolean isManager = project.getProjectManager() != null && currentStaff.getId().equals(project.getProjectManager().getId());
        boolean isMember = project.getTeamMembers() != null
                && project.getTeamMembers().stream().anyMatch(member -> currentStaff.getId().equals(member.getId()));

        return isManager || isMember;
    }

    private boolean canViewTask(Task task, Staff currentStaff, Project project) {
        if (!canAccessProject(project, currentStaff)) {
            return false;
        }
        if (isAdmin(currentStaff)) {
            return true;
        }
        return task.getAssignedStaff() != null && currentStaff.getId().equals(task.getAssignedStaff().getId());
    }

    private boolean canUpdateTask(Task task, Staff currentStaff, Project project) {
        return canViewTask(task, currentStaff, project);
    }

    private int countVisibleTasks(Project project, Staff currentStaff) {
        if (project.getTasks() == null) {
            return 0;
        }
        if (currentStaff == null) {
            return project.getTasks().size();
        }
        if (isAdmin(currentStaff)) {
            return project.getTasks().size();
        }
        return (int) project.getTasks().stream()
                .filter(task -> task.getAssignedStaff() != null && currentStaff.getId().equals(task.getAssignedStaff().getId()))
                .count();
    }
}
