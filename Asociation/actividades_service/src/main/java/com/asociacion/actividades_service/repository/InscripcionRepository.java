package com.asociacion.actividades_service.repository;

import com.asociacion.actividades_service.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    List<Inscripcion> findByActividad_Id(Integer actividadId);
}