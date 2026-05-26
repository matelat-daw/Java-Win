package com.clinica.clinicamvc.controller;

import com.clinica.clinicamvc.form.MascotaForm;
import com.clinica.clinicamvc.service.MascotaService;
import com.clinica.clinicamvc.model.Mascota;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MascotaController {
        private final MascotaService service;
        public MascotaController(MascotaService service) {
        this.service = service;
    }
    @GetMapping("/")
    public String inicio() {
            return "index";
    }

    @GetMapping("/mascotas")
    public String listar(Model model) {
    // TODO 21: añadir la lista de mascotas al Model con el nombre "mascotas".
    // TODO 22: devolver la vista "mascotas/lista".
        model.addAttribute("mascotas", service.listar());
        return "mascotas/lista";
    }
    @GetMapping("/mascotas/nueva")
    public String formulario(Model model) {
    // TODO 23: añadir un MascotaForm vacío al Model con nombre "mascotaForm".
    // TODO 24: devolver la vista "mascotas/formulario".
        model.addAttribute("mascotaForm", new MascotaForm());
        return "mascotas/formulario";
    }
    @PostMapping("/mascotas/nueva")
    public String guardar(
    @Valid @ModelAttribute("mascotaForm") MascotaForm form,
    BindingResult resultado,
    RedirectAttributes attr) {
    // TODO 25: si hay errores de validación, volver a "mascotas/formulario".
    // TODO 26: si no hay errores, llamar a service.registrar(form).
    // TODO 27: añadir mensaje de éxito: attr.addFlashAttribute("exito", "Mascota registrada.").
    // TODO 28: redirigir a /mascotas con return "redirect:/mascotas".
    if (resultado.hasErrors()) {
        return "mascotas/formulario";
    }
    service.registrar(form);
    attr.addFlashAttribute("exito", "Mascota registrada.");
    return "redirect:/mascotas";
    }
    @GetMapping("/mascotas/{id}")
    public String detalle(@PathVariable int id, Model model, RedirectAttributes attr) {
    // TODO 29: buscar la mascota con service.buscarPorId(id).
    // TODO 30: si es null, añadir mensaje de error y redirigir a /mascotas.
    // TODO 31: si existe, añadirla al Model y devolver "mascotas/detalle".
    Mascota mascota = service.buscarPorId(id);
    if (mascota == null) {
        attr.addFlashAttribute("error", "Mascota no encontrada.");
        return "redirect:/mascotas";
    }
    model.addAttribute("mascota", mascota);
    return "mascotas/detalle";
    }
    @PostMapping("/mascotas/{id}/atender")
    public String atender(@PathVariable int id, RedirectAttributes attr) {
    // TODO 32: llamar a service.atender(id).
    // TODO 33: redirigir a /mascotas.
    service.atender(id);
    return "redirect:/mascotas";
    }
}