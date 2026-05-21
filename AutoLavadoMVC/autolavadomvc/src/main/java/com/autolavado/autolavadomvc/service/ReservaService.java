package com.autolavado.autolavadomvc.service; 
 
import com.autolavado.autolavadomvc.form.ReservaForm; 
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
            reserva.setTipoTelefono(esTelefonoEspagnol(form.getTelefono()));
            reserva.setMatricula(form.getMatricula());
            reserva.setTipoMatricula(esMatriculaNacional(form.getMatricula()));
            reserva.setTipoLavado(form.getTipoLavado());
            reserva.setFecha(form.getFecha());
            reserva.setHora(form.getHora());
            reserva.setObservaciones(form.getObservaciones());
        // TODO 22: pasar la matrícula a mayúsculas. 
        reserva.setMatricula(reserva.getMatricula().toUpperCase());
        // TODO 23: calcular el precio según el tipo de lavado. 
        reserva.setPrecio(calcularPrecio(reserva.getTipoLavado()));
        // TODO 24: asignar estado inicial "PENDIENTE". 
        reserva.setEstado("PENDIENTE");
        // TODO 25: guardar usando repository.save(reserva). 
        repository.save(reserva);
    } 
 
    private double calcularPrecio(String tipoLavado) { 
        // TODO 26: devolver 8.00, 15.00 o 25.00 según el tipo. 
        // Si el tipo no es válido, devolver 0 o lanzar una excepción controlada.
        return switch (tipoLavado) {
            case "BÁSICO" -> 8.00;
            case "COMPLETO" -> 15.00;
            case "PREMIUM" -> 25.00;
            default -> 0.00;
        };
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
                .filter(reserva -> reserva.getId().equals(id) && reserva.getEstado().equals("PENDIENTE"))
                .findFirst()
                .map(reserva -> {
                    reserva.setEstado("EN_PROCESO");
                    return true;
                })
                .orElse(false);
    }

    public boolean finalizarReserva(Long id) { 
        // TODO 30: solo se puede pasar de EN_PROCESO a FINALIZADO. 
        // Devuelve true si se pudo cambiar, false si no. 
        return repository.findAll().stream()
                .filter(reserva -> reserva.getId().equals(id) && reserva.getEstado().equals("EN_PROCESO"))
                .findFirst()
                .map(reserva -> {
                    reserva.setEstado("FINALIZADO");
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
        return repository.findByEstado("PENDIENTE").size(); 
    } 

    private boolean esTelefonoEspagnol(String telefono) {
        String normalizado = telefono.replaceAll("[\\s-]", "");
        return normalizado.matches("^(?:[67]\\d{8}|(?:\\+34|0034)[67]\\d{8})$");
    }

    private boolean esMatriculaNacional(String matricula) {
        String normalizada = matricula.replaceAll("[\\s-]", "").toUpperCase();
        return normalizada.matches("^\\d{4}[B-DF-HJ-NP-TV-Z]{3}$");
    }
 
    public double calcularIngresosTotales() { 
        // TODO 33: sumar el precio de todas las reservas. 
        return repository.findAll().stream()
                .mapToDouble(ReservaLavado::getPrecio)
                .sum();
    } 
}