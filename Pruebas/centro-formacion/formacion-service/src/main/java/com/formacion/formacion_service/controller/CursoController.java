package com.formacion.formacion_service.controller;

import com.formacion.formacion_service.model.Curso;
import com.formacion.formacion_service.repository.CursoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

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
    public ResponseEntity<List<Curso>> findAll() {
        return ResponseEntity.ok(cursoRepository.findAll());
    }

    // TODO 4: POST /api/v1/cursos
    // Recibe un Curso con @RequestBody
    // Devuelve ResponseEntity.ok(cursoRepository.save(curso))
    @PostMapping
    public ResponseEntity<Curso> save(@RequestBody Curso curso) {
        return ResponseEntity.ok(cursoRepository.save(curso));
    }
}