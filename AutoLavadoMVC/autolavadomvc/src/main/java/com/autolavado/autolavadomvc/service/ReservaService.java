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
        // Si el tipo no es válido, devolver 0 o lanzar una excepción controlada.
        return tipoLavado != null ? tipoLavado.getPrecio() : BigDecimal.ZERO;
    } 
 
    public List<ReservaLavado> buscarPorMatricula(String matricula) { 
        // TODO 27: delegar en repository.findByMatricula(...). 
        return repository.findByMatricula(matricula); 
    } 
 
    public ReservaLavado buscarPorId(Long id) { 
        // TODO 28: buscar por id. 
        return repository.findById(id); 
    } 
 
    public boolean iniciarReserva(Long id) { 
        // TODO 29: solo se puede pasar de PENDIENTE a EN_PROCESO. 
        // Devuelve true si se pudo cambiar, false si no. 
        return repository.findAll().stream()
                .filter(reserva -> reserva.getId().equals(id) && reserva.getEstado() == EstadoReserva.PENDIENTE)
                .findFirst()
                .map(reserva -> {
                    reserva.setEstado(EstadoReserva.EN_PROCESO);
                    return true;
                })
                .orElse(false);
    }

    public boolean finalizarReserva(Long id) { 
        // TODO 30: solo se puede pasar de EN_PROCESO a FINALIZADO. 
        // Devuelve true si se pudo cambiar, false si no. 
        return repository.findAll().stream()
                .filter(reserva -> reserva.getId().equals(id) && reserva.getEstado() == EstadoReserva.EN_PROCESO)
                .findFirst()
                .map(reserva -> {
                    reserva.setEstado(EstadoReserva.FINALIZADO);
                    return true;
                })
                .orElse(false);
    }

    public int contarReservas() { 
        // TODO 31: devolver el total de reservas. 
        return repository.count();
    } 
 
    public int contarPendientes() { 
        // TODO 32: contar reservas con estado PENDIENTE. 
        return repository.findByEstado(EstadoReserva.PENDIENTE).size(); 
    } 

    public boolean esTelefonoValido(String telefono, boolean tipoTelefono) {
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
            // Formato moderno corregido en Java: Añadido (?!.*(?:CH|LL)) de seguridad
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