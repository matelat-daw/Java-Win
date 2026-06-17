package com.formacion.formacion_service.repository;

import com.formacion.formacion_service.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> { }