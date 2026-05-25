package com.asociacion.actividades_service.controller;

import javax.annotation.processing.Generated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.asociacion.actividades_service.service.InscripcionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.asociacion.actividades_service.model.Inscripcion;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/inscripciones")
public class InscripcionController {
    private InscripcionService service;

    public InscripcionController(InscripcionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Inscripcion>> getAllInscripciones() {
        return ResponseEntity.ok(service.getAllInscripciones());
    }

    @GetMapping("/inscripcion/{id}")
    public ResponseEntity<Inscripcion> mostrarDetallesInscripcion(@PathVariable Integer id) {
        return service.getInscripcionById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/inscripcion")
    public ResponseEntity<Inscripcion> inscribirSocio(@RequestBody Inscripcion inscripcion) {
        return ResponseEntity.ok(service.inscribirSocio(inscripcion));
    }
}