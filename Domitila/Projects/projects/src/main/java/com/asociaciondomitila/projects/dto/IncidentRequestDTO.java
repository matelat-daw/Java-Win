package com.asociaciondomitila.projects.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequestDTO {
    @NotBlank(message = "El titulo de la incidencia es obligatorio")
    private String title;

    @NotBlank(message = "La descripcion de la incidencia es obligatoria")
    private String description;

    private String severity;
}
