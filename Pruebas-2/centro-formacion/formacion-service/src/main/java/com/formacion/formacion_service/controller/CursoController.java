package com.formacion.formacion_service.controller;

import com.formacion.formacion_service.model.Curso;
import com.formacion.formacion_service.repository.CursoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cursos")
public class CursoController {
    private final CursoRepository cursoRepository;

    public CursoController(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    // TODO 3: GET /api/v1/cursos
    // Devuelve ResponseEntity.ok(cursoRepository.findAll())
    @GetMapping
    public ResponseEntity<List<CursoBasicResponse>> findAll() {
        return ResponseEntity.ok(cursoRepository.findAll().stream()
                .map(CursoBasicResponse::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/con-alumnos")
    public ResponseEntity<List<CursoResponse>> findAllConAlumnos() {
        return ResponseEntity.ok(cursoRepository.findAll().stream()
                .map(CursoResponse::from)
                .collect(Collectors.toList()));
    }

    // TODO 4: POST /api/v1/cursos
    // Recibe un Curso con @RequestBody
    // Devuelve ResponseEntity.ok(cursoRepository.save(curso))
    @PostMapping
    public ResponseEntity<CursoResponse> save(@RequestBody Curso curso) {
        if (curso.getTitulo() == null || curso.getTitulo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título es obligatorio");
        }

        try {
            return ResponseEntity.ok(CursoResponse.from(cursoRepository.save(curso)));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un curso con ese título");
        }
    }

    public record AlumnoResumen(
            Long id,
            String nombre,
            String email
    ) { }

    public record CursoBasicResponse(
            Long id,
            String titulo,
            String modalidad,
            int duracion
    ) {
        public static CursoBasicResponse from(Curso curso) {
            return new CursoBasicResponse(curso.getId(), curso.getTitulo(), curso.getModalidad(), curso.getDuracion());
        }
    }

    public record CursoResponse(
            Long id,
            String titulo,
            String modalidad,
            int duracion,
            List<AlumnoResumen> alumnos
    ) {
        public static CursoResponse from(Curso curso) {
            List<AlumnoResumen> alumnos = curso.getAlumnos() == null
                    ? List.of()
                    : curso.getAlumnos().stream()
                    .map(alumno -> new AlumnoResumen(alumno.getId(), alumno.getNombre(), alumno.getEmail()))
                    .collect(Collectors.toList());

            return new CursoResponse(
                    curso.getId(),
                    curso.getTitulo(),
                    curso.getModalidad(),
                    curso.getDuracion(),
                    alumnos
            );
        }
    }
}
