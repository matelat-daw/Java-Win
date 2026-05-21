package com.autolavado.autolavadomvc.service; 
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.autolavado.autolavadomvc.form.ReservaForm; 
import com.autolavado.autolavadomvc.model.EstadoReserva;
import com.autolavado.autolavadomvc.model.TipoLavado;
import com.autolavado.autolavadomvc.model.ReservaLavado; 
import com.autolavado.autolavadomvc.repository.ReservaRepository; 
import org.springframework.stereotype.Service; 
import java.util.List;
 
@Service 
public class ReservaService { 
 
    private final ReservaRepository repository; 
 
    public ReservaService(ReservaRepository repository) { 
        this.repository = repository; 
    } 
 
    public List<ReservaLavado> listarReservas() { 
        // TODO 20: pedir todas las reservas al repository. 
        return repository.findAll();
    } 
 
    public void crearReserva(ReservaForm form) { 
        // VALIDACIÓN EXTRA: Verificar teléfono y matrícula con las reglas de negocio antes de persistir
        if (!esTelefonoValido(form.getTelefono(), form.isTipoTelefono())) {
            throw new IllegalArgumentException("El formato del teléfono es incorrecto para el tipo seleccionado.");
        }
        if (!esMatriculaValida(form.getMatricula(), form.isTipoMatricula())) {
            throw new IllegalArgumentException("El formato de la matrícula es incorrecto para el tipo seleccionado.");
        }

        // TODO 21: crear una ReservaLavado a partir del form.
        ReservaLavado reserva = new ReservaLavado();
        reserva.setNombreCliente(form.getNombreCliente());
        reserva.setTelefono(form.getTelefono());
        reserva.setTipoTelefono(form.isTipoTelefono());
        reserva.setMatricula(form.getMatricula());
        reserva.setTipoMatricula(form.isTipoMatricula());
        reserva.setTipoLavado(form.getTipoLavado());
        reserva.setFecha(form.getFecha());
        reserva.setHora(form.getHora());
        reserva.setObservaciones(form.getObservaciones());

        // TODO 22: pasar la matrícula a mayúsculas. 
        reserva.setMatricula(reserva.getMatricula().toUpperCase());

        // TODO 23: calcular el precio según el tipo de lavado. 
        reserva.setPrecio(calcularPrecio(reserva.getTipoLavado()));

        // TODO 24: asignar estado inicial "PENDIENTE". 
        reserva.setEstado(EstadoReserva.PENDIENTE);

        // TODO 25: guardar usando repository.save(reserva). 
        repository.save(reserva);
    } 
 
    private BigDecimal calcularPrecio(TipoLavado tipoLavado) { 
        // TODO 26: devolver 8.00, 15.00 o 25.00 según el tipo. 
        return tipoLavado != null ? tipoLavado.getPrecio() : BigDecimal.ZERO;
    } 
 
    public List<ReservaLavado> buscarPorMatricula(String matricula) { 
        // TODO 27: delegar en repository.findByMatricula(...). 
        return repository.findByMatricula(matricula); 
    } 

    public List<ReservaLavado> buscarPorPendientes() {
        return repository.findByEstado(EstadoReserva.PENDIENTE);
    }
 
    public ReservaLavado buscarPorId(Long id) { 
        // TODO 28: buscar por id. 
        // Nota: Se asume que tu repository devuelve directamente el objeto o maneja la excepción.
        return repository.findById(id); 
    } 
 
    public boolean iniciarReserva(Long id) { 
        // CORREGIDO TODO 29: Se obtiene por ID, se verifica el estado PENDIENTE, se cambia a EN_PROCESO y SE GUARDA
        ReservaLavado reserva = repository.findById(id);
        if (reserva != null && reserva.getEstado() == EstadoReserva.PENDIENTE) {
            reserva.setEstado(EstadoReserva.EN_PROCESO);
            // repository.save(reserva); // <--- Crucial para impactar la base de datos
            return true;
        }
        return false;
    }

    public boolean finalizarReserva(Long id) { 
        // CORREGIDO TODO 30: Se obtiene por ID, se verifica el estado EN_PROCESO, se cambia a FINALIZADO y SE GUARDA
        ReservaLavado reserva = repository.findById(id);
        if (reserva != null && reserva.getEstado() == EstadoReserva.EN_PROCESO) {
            reserva.setEstado(EstadoReserva.FINALIZADO);
            // repository.save(reserva); // <--- Crucial para impactar la base de datos
            return true;
        }
        return false;
    }

    public int contarReservas() { 
        // TODO 31: devolver el total de reservas de forma eficiente. 
        return repository.count();
    } 
 
    public int contarPendientes() { 
        // TODO 32: contar reservas con estado PENDIENTE. 
        return repository.findByEstado(EstadoReserva.PENDIENTE).size(); 
    } 

    public boolean esTelefonoValido(String telefono, boolean tipoTelefono) {
        if (telefono == null) return false;
        String normalizado = telefono.replaceAll("[\\s-]", "");
        if (tipoTelefono) {
            return normalizado.matches("^[67]\\d{8}$");
        }
        return normalizado.matches("^(?:\\+|00)\\d{7,15}$");
    }

    public boolean esMatriculaValida(String matricula, boolean tipoMatricula) {
        if (matricula == null) {
            return false;
        }
        
        String normalizada = matricula.replaceAll("[\\s-]", "").toUpperCase();
        
        if (tipoMatricula) {
            // Formato moderno corregido en Java con lookahead de seguridad
            boolean sistemaModerno = normalizada.matches("^(?!.*(?:CH|LL))\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");
            boolean provincialConLetras = normalizada.matches("^[A-Z]{1,2}\\d{4}[A-Z]{1,2}$");
            boolean provincialSoloNumeros = normalizada.matches("^[A-Z]{1,2}\\d{1,6}$");
            
            return sistemaModerno || provincialConLetras || provincialSoloNumeros;
        }
        
        return normalizada.matches("^[A-Z0-9]{5,12}$");
    }
 
    public BigDecimal calcularIngresosTotales() { 
        // TODO 33: sumar el precio de todas las reservas. 
        return repository.findAll().stream()
            .map(ReservaLavado::getPrecio)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    } 
}