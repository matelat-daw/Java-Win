package com.asociacion.web_ui_thymeleaf.controller;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.asociacion.web_ui_thymeleaf.service.ActividadesService;
import com.asociacion.web_ui_thymeleaf.service.SociosService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SociosController {

    private final SociosService service;
    private final ActividadesService actividadesService;

    // Inyección por constructor (Buena práctica de Spring)
    public SociosController(SociosService service, ActividadesService actividadesService) {
        this.service = service;
        this.actividadesService = actividadesService;
    }

    @GetMapping("/")
    public String index() {
        return "index"; // Busca el archivo index.html en templates
    }

    // Ruta en el navegador: http://localhost:8081/socios
    @GetMapping("/socios")
    public String listarSocios(Model model) {
        // 1. Llamamos al servicio para traer los datos desde la Gateway
        List<Map<String, Object>> listaSocios = service.obtenerTodosLosSocios();
        
        // 2. Pasamos la lista a la vista bajo el nombre "socios"
        model.addAttribute("socios", listaSocios);
        
        // 3. Retornamos el nombre del archivo HTML (socios.html) sin la extensión
        return "socios"; 
    }

    @GetMapping("/socios/nuevo")
    public String mostrarFormularioNuevoSocio() {
        return "nuevo-socio";
    }

    @GetMapping("/socios/{id}")
    public String verInfoSocio(@PathVariable Integer id, Model model) {
        Map<String, Object> socio = service.obtenerSocioPorId(id);
        List<Map<String, Object>> actividades = actividadesService.obtenerTodasLasActividades();

        Integer socioId = id;
        if (socio.get("id") instanceof Number numeroSocioId) {
            socioId = numeroSocioId.intValue();
        }

        List<Map<String, Object>> actividadesInscripto = new ArrayList<>();
        for (Map<String, Object> actividad : actividades) {
            Object inscripcionesObj = actividad.get("inscripciones");
            if (!(inscripcionesObj instanceof List<?> inscripciones)) {
                continue;
            }

            for (Object item : inscripciones) {
                if (!(item instanceof Map<?, ?> inscripcionRaw)) {
                    continue;
                }

                Object socioIdInscripcion = inscripcionRaw.get("socioId");
                if (!Objects.equals(String.valueOf(socioIdInscripcion), String.valueOf(socioId))) {
                    continue;
                }

                Map<String, Object> actividadResumen = new LinkedHashMap<>();
                actividadResumen.put("id", actividad.get("id"));
                actividadResumen.put("titulo", actividad.get("titulo"));
                actividadResumen.put("fecha", actividad.get("fecha"));
                actividadResumen.put("fechaInscripcion", inscripcionRaw.get("fechaInscripcion"));
                actividadesInscripto.add(actividadResumen);
                break;
            }
        }

        model.addAttribute("estaInscripto", !actividadesInscripto.isEmpty());
        model.addAttribute("actividadesInscripto", actividadesInscripto);
        model.addAttribute("totalInscripciones", actividadesInscripto.size());
        model.addAttribute("socio", socio);
        return "socio-detalle";
    }

    @PostMapping("/socios")
    public String crearSocio(@RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {
        Map<String, Object> socio = new LinkedHashMap<>();
        socio.put("nombre", nombre);
        socio.put("email", email);
        socio.put("telefono", telefono);
        socio.put("estado", estado);

        service.crearSocio(socio);
        redirectAttributes.addFlashAttribute("mensaje", "Socio creado correctamente.");
        return "redirect:/socios";
    }
}