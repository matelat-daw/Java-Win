package com.asociaciondomitila.projects.entity;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.persistence.*; 
import java.time.LocalDate;
import java.util.List;
import com.asociaciondomitila.projects.enums.ProjectStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project")
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String code; 
    private String name;
    private String description;
    
    @Column(name = "start_date") // Añadido para respetar las buenas prácticas de bases de datos
    private LocalDate startDate; 
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus status; 
    
    private String type;

    // Relación con el líder del proyecto
    @ManyToOne
    @JoinColumn(name = "project_manager_id")
    private User projectManager;

    // CORRECCIÓN: Relación de muchos a muchos para los miembros del equipo
    @ManyToMany
    @JoinTable(
        name = "project_team",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> teamMembers;

    // Relación con las tareas
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true) 
    private List<Task> tasks;

    // CORRECCIÓN: Se añade mappedBy apuntando al campo de la entidad Incident
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Incident> incidents;
}