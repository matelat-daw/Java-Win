package com.asociaciondomitila.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.asociaciondomitila.projects.enums.ProjectStatus;
import com.asociaciondomitila.projects.util.ApiConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsRequestDTO {

    @NotBlank(message = ApiConstants.ERR_PROJECT_NAME_REQUIRED)
    @Size(max = 100, message = ApiConstants.ERR_PROJECT_NAME_MAX_LENGTH)
    private String name;

    @Size(max = 500, message = ApiConstants.ERR_PROJECT_DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull(message = ApiConstants.ERR_PROJECT_START_DATE_REQUIRED)
    private LocalDate startDate;

    @NotNull(message = ApiConstants.ERR_PROJECT_END_DATE_REQUIRED)
    private LocalDate endDate;  

    @NotNull(message = ApiConstants.ERR_PROJECT_STATUS_REQUIRED)
    private ProjectStatus status; // Puede ser tu Enum mapeado

    private String type;
}