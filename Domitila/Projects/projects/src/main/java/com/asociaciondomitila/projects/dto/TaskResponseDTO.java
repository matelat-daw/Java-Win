package com.asociaciondomitila.projects.dto;

import com.asociaciondomitila.projects.entity.Task;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private Long projectId;
    private StaffDto assignedStaff;
    private List<IncidentResponseDTO> incidents;
    private Integer incidentCount;

    public static TaskResponseDTO fromEntity(Task task) {
        List<IncidentResponseDTO> incidents = task.getIncidents() == null
                ? List.of()
                : task.getIncidents().stream()
                .sorted(Comparator
                        .comparing(
                                (com.asociaciondomitila.projects.entity.Incident incident) ->
                                        incident.getCreatedAt() == null ? java.time.LocalDateTime.MIN : incident.getCreatedAt(),
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                incident -> incident.getId() == null ? Long.MIN_VALUE : incident.getId(),
                                Comparator.reverseOrder()
                        ))
                .map(IncidentResponseDTO::fromEntity)
                .toList();

        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getAssignedStaff() != null ? StaffDto.fromEntity(task.getAssignedStaff()) : null,
                incidents,
                incidents.size()
        );
    }
}
