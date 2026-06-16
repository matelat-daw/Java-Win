package com.asociaciondomitila.projects.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentStatusRequest {
    @NotBlank(message = "El estado de la incidencia es obligatorio")
    private String status;
}
