package com.formacion.formacion_service.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.formacion.formacion_service.model.Alumno;
import com.formacion.formacion_service.model.Curso;
import com.formacion.formacion_service.repository.AlumnoRepository;
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
    public ResponseEntity<List<AlumnoResponse>> findAll() {
        return ResponseEntity.ok(alumnoRepository.findAll().stream()
                .map(AlumnoResponse::from)
                .collect(Collectors.toList()));
    }

    // TODO 4: POST /api/v1/alumnos
    // Recibe un Alumno con @RequestBody
    // Devuelve ResponseEntity.ok(repo.save(alumno))
    @PostMapping
    public ResponseEntity<AlumnoResponse> save(@RequestBody AlumnoCreateRequest request) {
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es obligatorio");
        }

        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El curso indicado no existe"));

        Alumno alumno = new Alumno();
        alumno.setNombre(request.nombre());
        alumno.setEmail(request.email());
        alumno.setCurso(curso);

        try {
            Alumno saved = alumnoRepository.save(alumno);
            return ResponseEntity.ok(AlumnoResponse.from(saved));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un alumno con ese email");
        }
    }

    public record AlumnoCreateRequest(
            String nombre,
            String email,
            @JsonProperty("curso_id") Long cursoId
    ) { }

    public record CursoResumen(
            Long id,
            String titulo,
            String modalidad,
            int duracion
    ) {
        public static CursoResumen from(Curso curso) {
            return new CursoResumen(curso.getId(), curso.getTitulo(), curso.getModalidad(), curso.getDuracion());
        }
    }

    public record AlumnoResponse(
            Long id,
            String nombre,
            String email,
            CursoResumen curso
    ) {
        public static AlumnoResponse from(Alumno alumno) {
            return new AlumnoResponse(
                    alumno.getId(),
                    alumno.getNombre(),
                    alumno.getEmail(),
                    alumno.getCurso() == null ? null : CursoResumen.from(alumno.getCurso())
            );
        }
    }
}
