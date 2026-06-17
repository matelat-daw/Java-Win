package com.miapp.controller;

import com.miapp.model.Contacto;
import com.miapp.service.ContactoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/store")
public class ContactoController {
    
    private final ContactoService contactoService;
    
    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }
    
    @GetMapping("/contact")
    public String mostrarFormularioContacto(Model model) {
        model.addAttribute("contacto", new Contacto());
        return "contacto";
    }
    
    @PostMapping("/contact/send")
    public String enviarContacto(@ModelAttribute Contacto contacto, Model model) {
        contactoService.guardarContacto(contacto);
        model.addAttribute("mensajeExito", "¡Mensaje enviado con éxito!");
        return "contacto";
    }
}