package com.miapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TiendaController {
    
    @GetMapping("/")
    public String inicio() {
        return "redirect:/frontend/index.html";
    }
    
    @GetMapping("/store")
    public String store() {
        return "redirect:/frontend/index.html";
    }
}