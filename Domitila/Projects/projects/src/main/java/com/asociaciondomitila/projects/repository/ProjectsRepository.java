package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.dto.ProjectsResponseDTO;
import com.asociaciondomitila.projects.entity.Project;
import com.asociaciondomitila.projects.enums.ProjectStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectsRepository extends JpaRepository<Project, Long> {
    
    // 1. Buscar proyectos por su estado (ej: activos, pausados)
    List<Project> findByStatus(ProjectStatus status);
    
    // 2. Buscar proyectos donde un usuario específico es el Project Manager
    List<Project> findByProjectManagerId(Long managerId);
    
    // 3. Buscar proyectos que contengan una palabra en su nombre (para buscadores)
    List<Project> findByNameContainingIgnoreCase(String name);

    @Query("""
            select distinct p
            from Project p
            left join p.teamMembers tm
            left join p.projectManager pm
            where pm.id = :staffId or tm.id = :staffId
            """)
    List<Project> findAccessibleProjectsByStaffId(@Param("staffId") Long staffId, Sort sort);

    @Query(value = """
            select new com.asociaciondomitila.projects.dto.ProjectsResponseDTO(
                p.id,
                p.code,
                p.name,
                p.description,
                p.startDate,
                p.endDate,
                p.status,
                p.type,
                (select count(tmCount.id) from Project pTeam join pTeam.teamMembers tmCount where pTeam.id = p.id),
                (select count(t.id) from Task t where t.project.id = p.id)
            )
            from Project p
            order by p.id asc
            """,
            countQuery = "select count(p) from Project p")
    Page<ProjectsResponseDTO> findProjectSummaries(Pageable pageable);

    @Query(value = """
            select new com.asociaciondomitila.projects.dto.ProjectsResponseDTO(
                p.id,
                p.code,
                p.name,
                p.description,
                p.startDate,
                p.endDate,
                p.status,
                p.type,
                (select count(tmCount.id) from Project pTeam join pTeam.teamMembers tmCount where pTeam.id = p.id),
                (select count(t.id) from Task t where t.project.id = p.id and t.assignedStaff.id = :staffId)
            )
            from Project p
            where p.projectManager.id = :staffId
               or exists (
                    select 1
                    from Project pAccess join pAccess.teamMembers tmAccess
                    where pAccess.id = p.id and tmAccess.id = :staffId
               )
            order by p.id asc
            """,
            countQuery = """
            select count(p)
            from Project p
            where p.projectManager.id = :staffId
               or exists (
                    select 1
                    from Project pAccess join pAccess.teamMembers tmAccess
                    where pAccess.id = p.id and tmAccess.id = :staffId
               )
            """)
    Page<ProjectsResponseDTO> findAccessibleProjectSummariesByStaffId(@Param("staffId") Long staffId, Pageable pageable);
}
