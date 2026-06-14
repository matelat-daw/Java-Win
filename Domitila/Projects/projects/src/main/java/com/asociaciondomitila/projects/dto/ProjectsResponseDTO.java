package com.asociaciondomitila.projects.dto;

import lombok.Data;
import java.time.LocalDate;
import com.asociaciondomitila.projects.enums.ProjectStatus;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private String type;
    private Long teamMemberCount;
    private Long taskCount;
}
