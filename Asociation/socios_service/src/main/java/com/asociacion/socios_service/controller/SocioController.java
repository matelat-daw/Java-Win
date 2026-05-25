package com.asociacion.socios_service.controller;

import java.util.List;
import com.asociacion.socios_service.model.Socios;
import com.asociacion.socios_service.service.SocioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/socios")
public class SocioController {
    private final SocioService service;

    public SocioController(SocioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Socios>> getAllSocios() {
        return ResponseEntity.ok(service.getAllSocios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Socios> getSocioById(@PathVariable int id) {
        return service.getSocioById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Socios> createSocio(@RequestBody Socios socio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSocio(socio));
    }
}