package com.asociaciondomitila.projects.dto;

import com.asociaciondomitila.projects.entity.Task;
import java.time.LocalDate;
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

    public static TaskResponseDTO fromEntity(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getAssignedStaff() != null ? StaffDto.fromEntity(task.getAssignedStaff()) : null
        );
    }
}
