package com.miapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TiendaController {
    
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("nombreTienda", "Tienda Tech");
        model.addAttribute("mensaje", "Aplicación convertida a Spring Boot");
        return "tienda";
    }
    
    @GetMapping("/store")
    public String store(Model model) {
        model.addAttribute("nombreTienda", "Tienda Tech");
        model.addAttribute("mensaje", "Aplicación convertida a Spring Boot");
        return "tienda";
    }
}