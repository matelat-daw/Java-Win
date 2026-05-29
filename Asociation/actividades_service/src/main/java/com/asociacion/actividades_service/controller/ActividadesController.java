package com.asociacion.actividades_service.controller;

import org.springframework.web.bind.annotation.RestController;
import com.asociacion.actividades_service.model.Actividad;
import com.asociacion.actividades_service.service.ActividadService;
import com.asociacion.actividades_service.repository.ActividadesRepository;
import com.asociacion.actividades_service.repository.InscripcionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/actividades")
public class ActividadesController {
    private ActividadService service;
    private final ActividadesRepository actividadesRepository;
    private final InscripcionRepository inscripcionRepository;

    public ActividadesController(ActividadService service, ActividadesRepository actividadesRepository, InscripcionRepository inscripcionRepository) {
        this.service = service;
        this.actividadesRepository = actividadesRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Actividad>> getAllActividades() {
        return ResponseEntity.ok(service.getAllActividades());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actividad> getActividadById(@PathVariable Integer id) {
        return service.getActividadById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Actividad> createActividad(@RequestBody Actividad actividad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearActividad(actividad));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteActividad(@PathVariable Integer id) {
        Actividad actividad = actividadesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La actividad indicada no existe"));

        inscripcionRepository.findByActividad_Id(id)
                .forEach(inscripcionRepository::delete);
        actividadesRepository.delete(actividad);

        return ResponseEntity.noContent().build();
    }
}