package com.formacion.formacion_service.controller;

import com.formacion.formacion_service.model.Alumno;
import com.formacion.formacion_service.model.Curso;
import com.formacion.formacion_service.repository.AlumnoRepository;
import com.formacion.formacion_service.repository.CursoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
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
    public ResponseEntity<List<AlumnoBasicResponse>> findAll() {
        return ResponseEntity.ok(alumnoRepository.findAll().stream()
                .map(AlumnoBasicResponse::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/en-cursos")
    public ResponseEntity<List<AlumnoResponse>> findAllEnCursos() {
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

        Alumno alumno = new Alumno();
        alumno.setNombre(request.nombre());
        alumno.setEmail(request.email());

        try {
            Alumno saved = alumnoRepository.save(alumno);
            return ResponseEntity.ok(AlumnoResponse.from(saved));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un alumno con ese email");
        }
    }

    @DeleteMapping("/{alumnoId}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long alumnoId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El alumno indicado no existe"));

        alumno.getCursos().forEach(curso -> curso.getAlumnos().remove(alumno));
        alumno.getCursos().clear();
        alumnoRepository.delete(alumno);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{alumnoId}/cursos/{cursoId}")
    public ResponseEntity<AlumnoResponse> inscribirEnCurso(@PathVariable Long alumnoId, @PathVariable Long cursoId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El alumno indicado no existe"));
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El curso indicado no existe"));

        Set<Curso> cursos = alumno.getCursos();
        if (cursos != null && cursos.contains(curso)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El alumno ya está inscrito en ese curso");
        }

        alumno.getCursos().add(curso);
        curso.getAlumnos().add(alumno);

        Alumno updated = alumnoRepository.save(alumno);
        return ResponseEntity.ok(AlumnoResponse.from(updated));
    }

    public record AlumnoCreateRequest(
            String nombre,
            String email
    ) { }

    public record AlumnoBasicResponse(
            Long id,
            String nombre,
            String email
    ) {
        public static AlumnoBasicResponse from(Alumno alumno) {
            return new AlumnoBasicResponse(alumno.getId(), alumno.getNombre(), alumno.getEmail());
        }
    }

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
            List<CursoResumen> cursos
    ) {
        public static AlumnoResponse from(Alumno alumno) {
            List<CursoResumen> cursos = alumno.getCursos() == null
                    ? List.of()
                    : alumno.getCursos().stream()
                    .map(CursoResumen::from)
                    .collect(Collectors.toList());

            return new AlumnoResponse(
                    alumno.getId(),
                    alumno.getNombre(),
                    alumno.getEmail(),
                    cursos
            );
        }
    }
}
