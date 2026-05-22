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
import java.util.List;
 
@Controller
public class ReservaController { 
 
    private final ReservaService service; 
 
    public ReservaController(ReservaService service) { 
        this.service = service; 
    }

    @GetMapping("/") 
    public String inicio() { 
        return "index";
    }
 
    @GetMapping("/reservas") 
    public String listar(Model model) { 
        model.addAttribute("reservas", service.listarReservas());
        model.addAttribute("matriculaBuscada", "");
        model.addAttribute("soloPendientes", false);
        return "reservas/lista";
    } 
 
    @GetMapping("/reservas/nueva") 
    public String formulario(Model model) { 
        model.addAttribute("reservaForm", new ReservaForm());
        return "reservas/formulario";
    } 
 
    @PostMapping("/reservas/nueva") 
    public String guardar( 
            @Valid @ModelAttribute("reservaForm") ReservaForm form, 
            BindingResult resultado, 
            RedirectAttributes attr) { 
 
        if (!service.esTelefonoValido(form.getTelefono(), form.isTipoTelefono())) {
            resultado.rejectValue("telefono", "telefono.formato", "El teléfono no coincide con el tipo seleccionado");
        }
        if (!service.esMatriculaValida(form.getMatricula(), form.isTipoMatricula())) {
            resultado.rejectValue("matricula", "matricula.formato", "La matrícula no coincide con el tipo seleccionado");
        }
        if (resultado.hasErrors()) {
            return "reservas/formulario";
        }

        service.crearReserva(form);
        attr.addFlashAttribute("mensaje", "Reserva creada con éxito.");
        return "redirect:/reservas"; 
    } 
 
    @GetMapping("/reservas/buscar") 
    public String buscar(
            @RequestParam(required = false, defaultValue = "") String matricula,
            @RequestParam(required = false, defaultValue = "false") boolean pendientes,
            Model model) { 
        
        // Optimización: delegamos los filtros combinados directamente al Service para que use la DB.
        // Esto evita iteraciones pesadas de bucles for, duplicados y LinkedHashMap en memoria Java.
        List<ReservaLavado> reservasFiltradas = service.buscarPorFiltros(matricula, pendientes);

        model.addAttribute("reservas", reservasFiltradas);
        model.addAttribute("matriculaBuscada", matricula);
        model.addAttribute("soloPendientes", pendientes);
        return "reservas/lista";
    } 
 
    // Cambiado de Long a BigInteger para coincidir con tu ID de MariaDB
    @GetMapping("/reservas/{id}") 
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes attr) { 
        ReservaLavado reserva = service.buscarPorId(id);
        
        if (reserva == null) {
            attr.addFlashAttribute("error", "La reserva solicitada no existe.");
            return "redirect:/reservas";
        }
        
        model.addAttribute("reserva", reserva);
        return "reservas/detalle";
    } 
 
    // Cambiado de Long a BigInteger
    @PostMapping("/reservas/{id}/iniciar") 
    public String iniciar(@PathVariable Long id, RedirectAttributes attr) { 
        try {
            service.iniciarReserva(id);
            attr.addFlashAttribute("mensaje", "Lavado iniciado correctamente.");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "No se pudo iniciar el lavado: " + e.getMessage());
        }
        return "redirect:/reservas"; 
    }

    // Cambiado de Long a BigInteger
    @PostMapping("/reservas/{id}/finalizar")
    public String finalizar(@PathVariable Long id, RedirectAttributes attr) { 
        try {
            service.finalizarReserva(id);
            attr.addFlashAttribute("mensaje", "Lavado finalizado correctamente.");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "No se pudo finalizar el lavado: " + e.getMessage());
        }
        return "redirect:/reservas"; 
    } 
 
    @GetMapping("/reservas/resumen") 
    public String resumen(Model model) { 
        model.addAttribute("total", service.contarReservas());
        model.addAttribute("pendientes", service.contarPendientes());
        model.addAttribute("ingresos", service.calcularIngresosTotales());
        return "reservas/resumen";
    }
}