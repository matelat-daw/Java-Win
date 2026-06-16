package com.asociaciondomitila.projects.dto;

import com.asociaciondomitila.projects.entity.Incident;
import com.asociaciondomitila.projects.enums.IncidentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String severity;
    private IncidentStatus status;
    private LocalDateTime createdAt;

    public static IncidentResponseDTO fromEntity(Incident incident) {
        return new IncidentResponseDTO(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getCreatedAt()
        );
    }
}
