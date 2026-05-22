package com.autolavado.autolavadomvc.controller; 
 
import com.autolavado.autolavadomvc.form.ReservaForm; 
import com.autolavado.autolavadomvc.model.ReservaLavado;
import com.autolavado.autolavadomvc.service.ReservaService; 
import jakarta.validation.Valid; 
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model; 
import org.springframework.validation.BindingResult; 
import org.springframework.web.bind.annotation.*; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
 
@Controller
public class ReservaController { 
 
    private final ReservaService service; 
 
    public ReservaController(ReservaService service) { 
        this.service = service; 
    }

     @GetMapping("/") 
    public String inicio() { 
        // TODO 34: devolver la vista de inicio. 
        return "index";
    }
 
    @GetMapping("/reservas") 
    public String listar(Model model) { 
        // TODO 35: añadir reservas al Model. 
        // TODO 36: devolver reservas/lista. 
        model.addAttribute("reservas", service.listarReservas());
        model.addAttribute("matriculaBuscada", "");
        model.addAttribute("soloPendientes", false);
        return "reservas/lista";
    } 
 
    @GetMapping("/reservas/nueva") 
    public String formulario(Model model) { 
        // TODO 37: añadir al Model un objeto ReservaForm vacío. 
        // El nombre debe coincidir con th:object en formulario.html. 
        model.addAttribute("reservaForm", new ReservaForm());
        return "reservas/formulario";
    } 
 
    @PostMapping("/reservas/nueva") 
    public String guardar( 
            @Valid @ModelAttribute("reservaForm") ReservaForm form, 
            BindingResult resultado, 
            RedirectAttributes attr) { 
 
        // TODO 38: si hay errores, volver a reservas/formulario.
        if (!service.esTelefonoValido(form.getTelefono(), form.isTipoTelefono())) {
            resultado.rejectValue("telefono", "telefono.formato", "El teléfono no coincide con el tipo seleccionado");
        }
        if (!service.esMatriculaValida(form.getMatricula(), form.isTipoMatricula())) {
            resultado.rejectValue("matricula", "matricula.formato", "La matrícula no coincide con el tipo seleccionado");
        }
        if (resultado.hasErrors()) {
            return "reservas/formulario";
        }
        // TODO 39: si no hay errores, guardar usando service.crearReserva(form).
        service.crearReserva(form);
        // TODO 40: añadir mensaje de éxito con attr.addFlashAttribute(...).
        attr.addFlashAttribute("mensaje", "Reserva creada con éxito.");
        // TODO 41: redirigir a /reservas. 
        return "redirect:/reservas"; 
    } 
 
    @GetMapping("/reservas/buscar") 
    public String buscar(
            @RequestParam(required = false, defaultValue = "") String matricula,
            @RequestParam(required = false, defaultValue = "false") boolean pendientes,
            Model model) { 
        // TODO 42: buscar por matrícula y mostrar la misma vista de lista.
        List<ReservaLavado> reservasFiltradas = new ArrayList<>();
        if (!matricula.isBlank()) {
            reservasFiltradas.addAll(service.buscarPorMatricula(matricula));
        }
        if (pendientes) {
            reservasFiltradas.addAll(service.buscarPorPendientes());
        }

        if (reservasFiltradas.isEmpty()) {
            reservasFiltradas = service.listarReservas();
        } else {
            LinkedHashMap<Long, ReservaLavado> reservasUnicas = new LinkedHashMap<>();
            for (ReservaLavado reserva : reservasFiltradas) {
                reservasUnicas.put(reserva.getId(), reserva);
            }
            reservasFiltradas = new ArrayList<>(reservasUnicas.values());
        }

        model.addAttribute("reservas", reservasFiltradas);
        model.addAttribute("matriculaBuscada", matricula);
        model.addAttribute("soloPendientes", pendientes);
        return "reservas/lista";
    } 
 
    @GetMapping("/reservas/{id}") 
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes attr) { 
        // TODO 43: buscar reserva por id. 
        // Si no existe, redirigir a /reservas con mensaje de error.
        model.addAttribute("reserva", service.buscarPorId(id));
        return "reservas/detalle";
    } 
 
    @PostMapping("/reservas/{id}/iniciar") 
    public String iniciar(@PathVariable Long id, RedirectAttributes attr) { 
        // TODO 44: intentar iniciar la reserva y redirigir a /reservas.
        service.iniciarReserva(id);
        return "redirect:/reservas"; 
    }

    @PostMapping("/reservas/{id}/finalizar")
    public String finalizar(@PathVariable Long id, RedirectAttributes attr) { 
        // TODO 45: intentar finalizar la reserva y redirigir a /reservas.
        service.finalizarReserva(id);
        return "redirect:/reservas"; 
    } 
 
    @GetMapping("/reservas/resumen") 
    public String resumen(Model model) { 
        // TODO 46: añadir total, pendientes e ingresos al Model. 
        model.addAttribute("total", service.contarReservas());
        model.addAttribute("pendientes", service.contarPendientes());
        model.addAttribute("ingresos", service.calcularIngresosTotales());
        return "reservas/resumen";
    }
}