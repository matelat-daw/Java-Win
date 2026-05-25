package com.asociacion.actividades_service.controller;

import org.springframework.web.bind.annotation.RestController;
import com.asociacion.actividades_service.model.Actividad;
import com.asociacion.actividades_service.service.ActividadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/actividades")
public class ActividadesController {
    public ActividadService service;

    public ActividadesController(ActividadService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Actividad>> getAllActividades() {
        return ResponseEntity.ok(service.getAllActividades());
    }

    @GetMapping("/actividades/{id}")
    public ResponseEntity<Actividad> getActividadesById(@PathVariable Integer id) {
        return service.getActividadesById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/actividades/nueva")
    public ResponseEntity<Actividad> createActividades(@RequestBody Actividad actividad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearActividades(actividad));
    }
}