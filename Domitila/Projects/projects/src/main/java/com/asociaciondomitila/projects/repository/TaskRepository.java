package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.entity.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectIdOrderByDueDateAscIdAsc(Long projectId);
}
