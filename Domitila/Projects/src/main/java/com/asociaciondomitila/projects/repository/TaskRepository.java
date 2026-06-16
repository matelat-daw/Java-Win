package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.entity.Task;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(attributePaths = {"assignedStaff", "assignedStaff.roles", "incidents"})
    List<Task> findDetailedByProjectIdOrderByDueDateAscIdAsc(Long projectId);

    @EntityGraph(attributePaths = {"assignedStaff", "assignedStaff.roles", "incidents"})
    List<Task> findDetailedByProjectIdAndAssignedStaffIdOrderByDueDateAscIdAsc(Long projectId, Long staffId);

    List<Task> findByProjectIdOrderByDueDateAscIdAsc(Long projectId);
}
