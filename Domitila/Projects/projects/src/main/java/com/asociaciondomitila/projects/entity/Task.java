package com.asociaciondomitila.projects.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;

    // ESTA LÍNEA TAMBIÉN ES INDISPENSABLE
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}