package com.ejemplo.modulo3demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {
    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/publico")
    public String publico() {
        log.info("Se ha llamado al endpoint publico");
        return "Ruta publica: cualquiera puede verla";
    }

    @GetMapping("/privado")
    public String privado() {
        log.info("Se ha llamado al endpoint privado");
        return "Ruta privada: solo usuarios autenticados";
    }

    @GetMapping("/admin")
    public String admin() {
        log.info("Se ha llamado al endpoint de administracion");
        return "Ruta de administracion: solo ADMIN";
    }
}