package com.autolavado.autolavadomvc.controller;

import com.autolavado.autolavadomvc.service.ServiciosLavadoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServiciosLavadoController {

    private final ServiciosLavadoService serviciosLavadoService;

    public ServiciosLavadoController(ServiciosLavadoService serviciosLavadoService) {
        this.serviciosLavadoService = serviciosLavadoService;
    }

    @GetMapping("/reservas/servicios")
    public String servicios(Model model) {
        model.addAttribute("servicios", serviciosLavadoService.listarServicios());
        return "reservas/servicios-catalogo";
    }
}