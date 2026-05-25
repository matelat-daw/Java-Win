package com.asociacion.actividades_service.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.asociacion.actividades_service.service.InscripcionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.asociacion.actividades_service.model.Inscripcion;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/actividades")
public class InscripcionController {
    
    private final InscripcionService service;

    public InscripcionController(InscripcionService service) {
        this.service = service;
    }

    // Único endpoint activo para gestionar las inscripciones
    @PostMapping("/{id}/inscripciones")
    public ResponseEntity<Inscripcion> inscribirSocio(
            @PathVariable("id") Integer actividadId, 
            @RequestBody Inscripcion inscripcion) {  
        
        // Vinculamos el ID de la actividad que viene de la URL al objeto de inscripción
        inscripcion.setActividadId(actividadId);
        
        // El servicio procesa la regla de negocio y almacena el registro
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inscribirSocio(inscripcion));
    }
}