package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.entity.Incident;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByTaskIdOrderByCreatedAtDescIdDesc(Long taskId);
}
