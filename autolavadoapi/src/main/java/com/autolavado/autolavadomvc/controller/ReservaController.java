package com.autolavado.autolavadomvc.controller; 
 
import com.autolavado.autolavadomvc.form.ReservaForm; 
import com.autolavado.autolavadomvc.model.ReservaServicio;
import com.autolavado.autolavadomvc.service.ReservaService; 
import jakarta.validation.Valid; 
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*; 
import com.autolavado.autolavadomvc.controller.api.ApiDtoMapper;
import com.autolavado.autolavadomvc.controller.api.dto.PageDto;
import com.autolavado.autolavadomvc.controller.api.dto.ReservaDto;
import com.autolavado.autolavadomvc.controller.api.dto.ResumenDto;
 
@RestController
@RequestMapping("/api/reservas")
public class ReservaController { 
 
    private final ReservaService service; 
 
    public ReservaController(ReservaService service) { 
        this.service = service; 
    }
 
    @GetMapping
    public PageDto<ReservaDto> listar(
            @RequestParam(required = false, defaultValue = "") String matricula,
            @RequestParam(required = false, defaultValue = "false") boolean pendientes,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "asc") String dir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "8") int size) {
        var pageResult = service.buscarPagina(matricula, pendientes, sort, dir, page, size);
        List<ReservaDto> items = pageResult.getContent().stream()
                .map(ApiDtoMapper::toDto)
                .toList();
        return new PageDto<>(items, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    @GetMapping("/{id}")
    public ReservaDto detalle(@PathVariable Long id) {
        ReservaServicio reserva = service.buscarPorId(id);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva solicitada no existe.");
        }
        return ApiDtoMapper.toDto(reserva);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDto crear(@Valid @RequestBody ReservaForm form) {
        ReservaServicio reserva = service.crearReserva(form);
        return ApiDtoMapper.toDto(reserva);
    }

    @PostMapping("/{id}/iniciar")
    public ReservaDto iniciar(@PathVariable Long id) {
        boolean ok = service.iniciarReserva(id);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo iniciar el lavado con el estado actual.");
        }
        return detalle(id);
    }

    @PostMapping("/{id}/finalizar")
    public ReservaDto finalizar(@PathVariable Long id) {
        boolean ok = service.finalizarReserva(id);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo finalizar el lavado con el estado actual.");
        }
        return detalle(id);
    }

    @GetMapping("/resumen")
    public ResumenDto resumen() {
        return new ResumenDto(service.contarReservas(), service.contarPendientes(), service.calcularIngresosTotales());
    }
}
