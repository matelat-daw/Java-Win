package com.formacion.formacion_service.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.formacion.formacion_service.model.Alumno;
import com.formacion.formacion_service.model.Curso;
import com.formacion.formacion_service.repository.AlumnoRepository;
import com.formacion.formacion_service.repository.CursoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/alumnos")
public class AlumnoController {
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;

    public AlumnoController(AlumnoRepository alumnoRepository, CursoRepository cursoRepository) {
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
    }

    // TODO 3: GET /api/v1/alumnos
    // Devuelve ResponseEntity.ok(repo.findAll())
    @GetMapping
    public ResponseEntity<List<Alumno>> findAll() {
        return ResponseEntity.ok(alumnoRepository.findAll());
    }

    // TODO 4: POST /api/v1/alumnos
    // Recibe un Alumno con @RequestBody
    // Devuelve ResponseEntity.ok(repo.save(alumno))
    @PostMapping
    public ResponseEntity<Alumno> save(@RequestBody AlumnoCreateRequest request) {
        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El curso indicado no existe"));

        Alumno alumno = new Alumno();
        alumno.setNombre(request.nombre());
        alumno.setEmail(request.email());
        alumno.setCurso(curso);

        return ResponseEntity.ok(alumnoRepository.save(alumno));
    }

    public record AlumnoCreateRequest(
            String nombre,
            String email,
            @JsonProperty("curso_id") Long cursoId
    ) { }
}