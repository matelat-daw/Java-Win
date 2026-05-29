package com.asociacion.actividades_service.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.asociacion.actividades_service.service.InscripcionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.asociacion.actividades_service.model.Inscripcion;
import com.asociacion.actividades_service.model.Actividad;
import com.asociacion.actividades_service.repository.ActividadesRepository;
import com.asociacion.actividades_service.repository.InscripcionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/actividades")
public class InscripcionController {
    
    private final InscripcionService service;
    private final ActividadesRepository actividadesRepository;
    private final InscripcionRepository inscripcionRepository;

    public InscripcionController(InscripcionService service, ActividadesRepository actividadesRepository, InscripcionRepository inscripcionRepository) {
        this.service = service;
        this.actividadesRepository = actividadesRepository;
        this.inscripcionRepository = inscripcionRepository;
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

    @DeleteMapping("/{actividadId}/inscripciones/{inscripcionId}")
    public ResponseEntity<Void> deleteInscripcion(
            @PathVariable Integer actividadId,
            @PathVariable Integer inscripcionId) {

        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La inscripción indicada no existe"));

        if (inscripcion.getActividad() == null || !actividadId.equals(inscripcion.getActividad().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La inscripción no pertenece a la actividad indicada");
        }

        inscripcionRepository.delete(inscripcion);
        return ResponseEntity.noContent().build();
    }
}