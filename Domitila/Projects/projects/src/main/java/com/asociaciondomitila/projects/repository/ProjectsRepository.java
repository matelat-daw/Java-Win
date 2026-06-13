package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.entity.Project;
import com.asociaciondomitila.projects.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectsRepository extends JpaRepository<Project, Long> {
    
    // 1. Buscar proyectos por su estado (ej: activos, pausados)
    List<Project> findByStatus(ProjectStatus status);
    
    // 2. Buscar proyectos donde un usuario específico es el Project Manager
    List<Project> findByProjectManagerId(Long managerId);
    
    // 3. Buscar proyectos que contengan una palabra en su nombre (para buscadores)
    List<Project> findByNameContainingIgnoreCase(String name);
}