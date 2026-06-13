package com.asociaciondomitila.projects.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "incident")
public class Incident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private String severity;

    // ESTA ES LA LÍNEA QUE FALTA Y RESUELVE EL ERROR
    @ManyToOne
    @JoinColumn(name = "project_id") // Apunta a la columna de tu base de datos
    private Project project;
}