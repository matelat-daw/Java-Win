package com.autolavado.autolavadomvc.controller; 
 
import com.autolavado.autolavadomvc.form.ReservaForm; 
import com.autolavado.autolavadomvc.model.ReservaServicio;
import com.autolavado.autolavadomvc.model.TipoServicio;
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
    public String listar(Model model,
                         @RequestParam(required = false, defaultValue = "") String matricula,
                         @RequestParam(required = false, defaultValue = "false") boolean pendientes,
                         @RequestParam(required = false) String sort,
                         @RequestParam(required = false, defaultValue = "asc") String dir) {
        model.addAttribute("reservas", service.buscarPorFiltros(matricula, pendientes, sort, dir));
        model.addAttribute("matriculaBuscada", matricula);
        model.addAttribute("soloPendientes", pendientes);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        return "reservas/lista";
    } 
 
    @GetMapping("/reservas/nueva") 
    public String formulario(Model model, @RequestParam(required = false) String tipoServicio) { 
        ReservaForm form = new ReservaForm();

        if (tipoServicio != null && !tipoServicio.isBlank()) {
            try {
                form.setTipoServicio(TipoServicio.valueOf(tipoServicio));
            } catch (IllegalArgumentException ex) {
                // Ignorar si el valor no coincide con el enum.
            }
        }

        model.addAttribute("reservaForm", form);
        model.addAttribute("tipoServicioLocked", form.getTipoServicio() != null);
        return "reservas/formulario";
    } 
 
    @PostMapping("/reservas/nueva") 
    public String guardar( 
            @Valid @ModelAttribute("reservaForm") ReservaForm form, 
            BindingResult resultado, 
            Model model,
            RedirectAttributes attr) { 
 
        if (!service.esTelefonoValido(form.getTelefono(), form.isTipoTelefono())) {
            resultado.rejectValue("telefono", "telefono.formato", "El teléfono no coincide con el tipo seleccionado");
        }
        if (!service.esMatriculaValida(form.getMatricula(), form.isTipoMatricula())) {
            resultado.rejectValue("matricula", "matricula.formato", "La matrícula no coincide con el tipo seleccionado");
        }
        if (resultado.hasErrors()) {
            model.addAttribute("tipoServicioLocked", form.getTipoServicio() != null);
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
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "asc") String dir,
            Model model) {
        
        List<ReservaServicio> reservasFiltradas = service.buscarPorFiltros(matricula, pendientes, sort, dir);

        model.addAttribute("reservas", reservasFiltradas);
        model.addAttribute("matriculaBuscada", matricula);
        model.addAttribute("soloPendientes", pendientes);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        return "reservas/lista";
    } 
 
    // Cambiado de Long a BigInteger para coincidir con tu ID de MariaDB
    @GetMapping("/reservas/{id}") 
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes attr) { 
        ReservaServicio reserva = service.buscarPorId(id);
        
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