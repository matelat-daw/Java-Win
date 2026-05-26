package com.asociacion.actividades_service.repository;

import org.springframework.stereotype.Repository;
import com.asociacion.actividades_service.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    List<Inscripcion> findByActividad_Id(Integer actividadId);
}