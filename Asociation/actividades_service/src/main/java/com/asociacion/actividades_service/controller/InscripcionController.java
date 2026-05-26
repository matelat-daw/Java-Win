package com.asociacion.actividades_service.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.asociacion.actividades_service.service.InscripcionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.asociacion.actividades_service.model.Inscripcion;
import com.asociacion.actividades_service.model.Actividad;
import com.asociacion.actividades_service.repository.ActividadesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/actividades")
public class InscripcionController {
    
    private final InscripcionService service;
    private final ActividadesRepository actividadesRepository;

    public InscripcionController(InscripcionService service, ActividadesRepository actividadesRepository) {
        this.service = service;
        this.actividadesRepository = actividadesRepository;
    }

    // Único endpoint activo para gestionar las inscripciones
    @PostMapping("/{id}/inscripciones")
    public ResponseEntity<Inscripcion> inscribirSocio(
            @PathVariable("id") Integer actividadId, 
            @RequestBody Inscripcion inscripcion) {  

        Actividad actividad = actividadesRepository.findById(actividadId)
                .orElse(null);

        if (actividad == null) {
            return ResponseEntity.notFound().build();
        }

        // Vinculamos la entidad actividad completa para que JPA cree la FK
        inscripcion.setActividad(actividad);
        
        // El servicio procesa la regla de negocio y almacena el registro
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inscribirSocio(inscripcion));
    }
}