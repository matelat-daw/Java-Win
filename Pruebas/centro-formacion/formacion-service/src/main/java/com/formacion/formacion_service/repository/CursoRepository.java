package com.formacion.formacion_service.repository;

import com.formacion.formacion_service.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> { }