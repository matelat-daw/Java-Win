package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.dto.ProjectsRequestDTO;
import com.asociaciondomitila.projects.entity.Project;
import com.asociaciondomitila.projects.entity.Staff;
import com.asociaciondomitila.projects.entity.Task;
import com.asociaciondomitila.projects.enums.ProjectStatus;
import com.asociaciondomitila.projects.repository.BeneficiaryUserRepository;
import com.asociaciondomitila.projects.repository.IncidentRepository;
import com.asociaciondomitila.projects.repository.ProjectsRepository;
import com.asociaciondomitila.projects.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsServiceTest {

    @Mock
    private ProjectsRepository projectsRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private BeneficiaryUserRepository beneficiaryUserRepository;

    @Mock
    private StaffService staffService;

    @InjectMocks
    private ProjectsService projectsService;

    @Test
    void updateProject_shouldRejectCompletion_whenAnyTaskIsPending() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Proyecto de prueba");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(10));
        project.setStatus(ProjectStatus.ACTIVO);

        Task pendingTask = new Task();
        pendingTask.setStatus("EN_PROGRESO");

        ProjectsRequestDTO request = new ProjectsRequestDTO(
                "PR-1",
                "Proyecto de prueba",
                "Descripcion",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                ProjectStatus.COMPLETADO,
                "General"
        );

        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectIdOrderByDueDateAscIdAsc(1L)).thenReturn(List.of(pendingTask));

        assertThrows(IllegalArgumentException.class, () -> projectsService.updateProject(1L, request));

        verify(projectsRepository, never()).save(any());
    }

    @Test
    void updateProject_shouldAllowCompletion_whenAllTasksAreFinished() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Proyecto de prueba");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(10));
        project.setStatus(ProjectStatus.ACTIVO);

        Task finishedTask = new Task();
        finishedTask.setStatus("TERMINADO");

        ProjectsRequestDTO request = new ProjectsRequestDTO(
                "PR-1",
                "Proyecto de prueba",
                "Descripcion",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                ProjectStatus.COMPLETADO,
                "General"
        );

        when(projectsRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectIdOrderByDueDateAscIdAsc(1L)).thenReturn(List.of(finishedTask));
        when(projectsRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectsRequestDTO safeRequest = request;
        projectsService.updateProject(1L, safeRequest);

        verify(projectsRepository).save(any(Project.class));
    }
}