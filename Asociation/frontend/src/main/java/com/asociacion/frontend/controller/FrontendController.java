package com.asociacion.frontend.controller;

import java.util.List;
import java.util.Map;
import com.asociacion.frontend.service.FrontendService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class FrontendController {

    private final FrontendService frontendService;

    // Inyección por constructor (Buena práctica de Spring)
    public FrontendController(FrontendService frontendService) {
        this.frontendService = frontendService;
    }

    @GetMapping("/")
    public String index() {
        return "index"; // Busca el archivo index.html en templates
    }

    // Ruta en el navegador: http://localhost:8081/socios
    @GetMapping("/socios")
    public String listarSocios(Model model) {
        // 1. Llamamos al servicio para traer los datos desde la Gateway
        List<Map<String, Object>> listaSocios = frontendService.obtenerTodosLosSocios();
        
        // 2. Pasamos la lista a la vista bajo el nombre "socios"
        model.addAttribute("socios", listaSocios);
        
        // 3. Retornamos el nombre del archivo HTML (socios.html) sin la extensión
        return "socios"; 
    }
}