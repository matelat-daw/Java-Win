package com.asociacion.web_ui_thymeleaf.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.asociacion.web_ui_thymeleaf.service.ActividadesService;
import com.asociacion.web_ui_thymeleaf.service.SociosService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ActividadesController {

	private final ActividadesService service;
	private final SociosService sociosService;

	public ActividadesController(ActividadesService service, SociosService sociosService) {
		this.service = service;
		this.sociosService = sociosService;
	}

	@GetMapping("/actividades")
	public String listarActividades(Model model) {
		List<Map<String, Object>> listaActividades = service.obtenerTodasLasActividades();
		model.addAttribute("actividades", listaActividades);
		return "actividades";
	}

	@GetMapping("/actividades/nueva")
	public String mostrarFormularioNuevaActividad() {
		return "nueva-actividad";
	}

	@PostMapping("/actividades")
	public String crearActividad(@RequestParam String titulo,
			@RequestParam String descripcion,
			@RequestParam LocalDate fecha,
			@RequestParam int plazas,
			RedirectAttributes redirectAttributes) {
		Map<String, Object> actividad = new LinkedHashMap<>();
		actividad.put("titulo", titulo);
		actividad.put("descripcion", descripcion);
		actividad.put("fecha", fecha);
		actividad.put("plazas", plazas);

		service.crearActividad(actividad);
		redirectAttributes.addFlashAttribute("mensaje", "Actividad creada correctamente.");
		return "redirect:/actividades";
	}

	@GetMapping("/actividades/{id}/inscribir")
	public String mostrarInscripcion(@PathVariable Integer id, Model model) {
		Map<String, Object> actividad = service.obtenerActividadPorId(id);
		List<Map<String, Object>> socios = sociosService.obtenerTodosLosSocios();
		model.addAttribute("actividad", actividad);
		model.addAttribute("socios", socios);
		return "inscribir-socio-actividad";
	}

	@PostMapping("/actividades/{id}/inscribir")
	public String inscribirSocio(@PathVariable Integer id,
			@RequestParam Integer socioId,
			RedirectAttributes redirectAttributes) {
		Map<String, Object> socio = sociosService.obtenerSocioPorId(socioId);
		Map<String, Object> inscripcion = new LinkedHashMap<>();
		inscripcion.put("socioId", socioId);
		inscripcion.put("nombreSocio", socio.get("nombre"));
		inscripcion.put("fechaInscripcion", LocalDate.now());

		service.inscribirSocioEnActividad(id, inscripcion);
		redirectAttributes.addFlashAttribute("mensaje", "Socio inscrito correctamente en la actividad.");
		return "redirect:/actividades";
	}
}