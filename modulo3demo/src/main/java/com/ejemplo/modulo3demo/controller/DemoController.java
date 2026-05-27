package com.ejemplo.modulo3demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {
    
    // SOLUCIÓN NATIVA: Usamos el registrador nativo del JDK de Java 25
    private static final System.Logger log = System.getLogger(DemoController.class.getName());

    @GetMapping("/publico")
    public String publico() {
        log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint publico");
        return "Ruta publica: cualquiera puede verla";
    }

    @GetMapping("/privado")
    public String privado() {
        log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint privado");
        return "Ruta privada: solo usuarios autenticados";
    }

    @GetMapping("/admin")
    public String admin() {
        log.log(System.Logger.Level.INFO, "Se ha llamado al endpoint de administracion");
        return "Ruta de administracion: solo ADMIN";
    }
}