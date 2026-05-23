package com.autolavado.autolavadomvc.controller;

import com.autolavado.autolavadomvc.service.CatalogoServiciosService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoServiciosController {

    private final CatalogoServiciosService catalogoServiciosService;

    public CatalogoServiciosController(CatalogoServiciosService catalogoServiciosService) {
        this.catalogoServiciosService = catalogoServiciosService;
    }

    @GetMapping("/reservas/servicios")
    public String servicios(Model model) {
        model.addAttribute("servicios", catalogoServiciosService.listarServicios());
        return "reservas/servicios-catalogo";
    }
}