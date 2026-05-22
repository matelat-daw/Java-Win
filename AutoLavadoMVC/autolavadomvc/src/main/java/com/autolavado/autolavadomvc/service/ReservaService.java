package com.autolavado.autolavadomvc.service; 
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.autolavado.autolavadomvc.form.ReservaForm; 
import com.autolavado.autolavadomvc.model.EstadoReserva;
import com.autolavado.autolavadomvc.model.TipoServicio;
import com.autolavado.autolavadomvc.model.ReservaServicio; 
import com.autolavado.autolavadomvc.repository.ReservaRepository; 
import org.springframework.stereotype.Service; 
import java.util.List;
import java.util.Optional;
 
@Service 
public class ReservaService { 
 
    private final ReservaRepository repository; 
 
    public ReservaService(ReservaRepository repository) { 
        this.repository = repository; 
    } 
 
    public List<ReservaServicio> listarReservas() { 
        return repository.findAll();
    } 
 
    public void crearReserva(ReservaForm form) { 
        if (!esTelefonoValido(form.getTelefono(), form.isTipoTelefono())) {
            throw new IllegalArgumentException("El formato del teléfono es incorrecto para el tipo seleccionado.");
        }
        if (!esMatriculaValida(form.getMatricula(), form.isTipoMatricula())) {
            throw new IllegalArgumentException("El formato de la matrícula es incorrecto para el tipo seleccionado.");
        }

        ReservaServicio reserva = new ReservaServicio();
        reserva.setNombreCliente(form.getNombreCliente());
        reserva.setTelefono(form.getTelefono());
        reserva.setMatricula(form.getMatricula().toUpperCase()); // Pasa a mayúsculas directamente
        reserva.setTipoServicio(form.getTipoServicio());
        reserva.setFecha(form.getFecha());
        reserva.setHora(form.getHora());
        reserva.setObservaciones(form.getObservaciones());
        reserva.setPrecio(calcularPrecio(reserva.getTipoServicio()));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        repository.save(reserva);
    } 
 
    private BigDecimal calcularPrecio(TipoServicio tipoServicio) {
        return tipoServicio != null ? tipoServicio.getPrecio() : BigDecimal.ZERO;
    } 
 
    // Filtro optimizado para el método "buscar" del controlador
    public List<ReservaServicio> buscarPorFiltros(String matricula, boolean soloPendientes) {
        if (!matricula.isBlank() && soloPendientes) {
            return repository.findByMatriculaContainingIgnoreCase(matricula).stream()
                    .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE)
                    .toList();
        } else if (!matricula.isBlank()) {
            return repository.findByMatriculaContainingIgnoreCase(matricula);
        } else if (soloPendientes) {
            return repository.findByEstado(EstadoReserva.PENDIENTE);
        }
        return repository.findAll();
    }

    public List<ReservaServicio> buscarPorMatricula(String matricula) { 
        return repository.findByMatriculaContainingIgnoreCase(matricula); 
    } 

    public List<ReservaServicio> buscarPorPendientes() {
        return repository.findByEstado(EstadoReserva.PENDIENTE);
    }
 
    public ReservaServicio buscarPorId(Long id) { 
        return repository.findById(id).orElse(null); 
    } 
 
    public boolean iniciarReserva(Long id) { 
        Optional<ReservaServicio> optionalReserva = repository.findById(id);
        if (optionalReserva.isPresent()) {
            ReservaServicio reserva = optionalReserva.get();
            if (reserva.getEstado() == EstadoReserva.PENDIENTE) {
                reserva.setEstado(EstadoReserva.EN_PROCESO);
                repository.save(reserva); // Descomentado: Impacta directamente el cambio en MariaDB
                return true;
            }
        }
        return false;
    }

    public boolean finalizarReserva(Long id) { 
        Optional<ReservaServicio> optionalReserva = repository.findById(id);
        if (optionalReserva.isPresent()) {
            ReservaServicio reserva = optionalReserva.get();
            if (reserva.getEstado() == EstadoReserva.EN_PROCESO) {
                reserva.setEstado(EstadoReserva.FINALIZADO);
                repository.save(reserva); // Descomentado: Impacta directamente el cambio en MariaDB
                return true;
            }
        }
        return false;
    }

    public long contarReservas() { 
        return repository.count(); // count() de JPA devuelve un tipo primitivo long
    } 
 
    public long contarPendientes() { 
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
        if (matricula == null) return false;
        String normalizada = matricula.replaceAll("[\\s-]", "").toUpperCase();
        
        if (tipoMatricula) {
            boolean sistemaModerno = normalizada.matches("^(?!.*(?:CH|LL))\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");
            boolean provincialConLetras = normalizada.matches("^[A-Z]{1,2}\\d{4}[A-Z]{1,2}$");
            boolean provincialSoloNumeros = normalizada.matches("^[A-Z]{1,2}\\d{1,6}$");
            return sistemaModerno || provincialConLetras || provincialSoloNumeros;
        }
        return normalizada.matches("^[A-Z0-9]{5,12}$");
    }
 
    public BigDecimal calcularIngresosTotales() { 
        return repository.findAll().stream()
            .map(ReservaServicio::getPrecio)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    } 
}