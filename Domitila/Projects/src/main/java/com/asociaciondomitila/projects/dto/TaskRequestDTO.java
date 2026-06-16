package com.asociaciondomitila.projects.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {
    @NotBlank(message = "El titulo de la tarea es obligatorio")
    private String title;

    private String description;
    private String status;
    private LocalDate dueDate;
    private Long assignedStaffId;
}
