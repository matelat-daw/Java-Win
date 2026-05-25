package com.asociacion.actividades_service.controller;

import org.springframework.web.bind.annotation.RestController;
import com.asociacion.actividades_service.model.Actividad;
import com.asociacion.actividades_service.service.ActividadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@RestController
@RequestMapping("/api/v1/actividades")
public class ActividadesController {
    private ActividadService service;

    public ActividadesController(ActividadService service) {
        this.service = service;
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
}