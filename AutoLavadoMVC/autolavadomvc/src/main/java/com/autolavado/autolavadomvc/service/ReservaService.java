package com.autolavado.autolavadomvc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.autolavado.autolavadomvc.form.ReservaForm;
import com.autolavado.autolavadomvc.model.Estado;
import com.autolavado.autolavadomvc.model.Facturacion;
import com.autolavado.autolavadomvc.model.ReservaServicio;
import com.autolavado.autolavadomvc.model.Servicio;
import com.autolavado.autolavadomvc.repository.EstadoRepository;
import com.autolavado.autolavadomvc.repository.ReservaRepository;
import com.autolavado.autolavadomvc.repository.ServicioRepository;

@Service
public class ReservaService {

    private final ReservaRepository repository;
    private final ServicioRepository servicioRepository;
    private final EstadoRepository estadoRepository;

    public ReservaService(ReservaRepository repository, ServicioRepository servicioRepository, EstadoRepository estadoRepository) {
        this.repository = repository;
        this.servicioRepository = servicioRepository;
        this.estadoRepository = estadoRepository;
    }

    public void crearReserva(ReservaForm form) {
        if (!esTelefonoValido(form.getTelefono(), form.isTipoTelefono())) {
            throw new IllegalArgumentException("El formato del teléfono es incorrecto para el tipo seleccionado.");
        }
        if (!esMatriculaValida(form.getMatricula(), form.isTipoMatricula())) {
            throw new IllegalArgumentException("El formato de la matrícula es incorrecto para el tipo seleccionado.");
        }

        Servicio servicio = servicioRepository.findById(form.getServicioId())
                .orElseThrow(() -> new IllegalArgumentException("El servicio seleccionado no existe."));
        Estado estadoPendiente = estadoRepository.findByEstadoIgnoreCase("PENDIENTE")
                .orElseThrow(() -> new IllegalStateException("No existe el estado PENDIENTE en la base de datos."));

        ReservaServicio reserva = new ReservaServicio();
        reserva.setNombreCliente(form.getNombreCliente());
        reserva.setTelefono(form.getTelefono());
        reserva.setMatricula(form.getMatricula().toUpperCase());
        reserva.setFecha(form.getFecha());
        reserva.setHora(form.getHora());
        reserva.setObservaciones(form.getObservaciones());
        reserva.setEstado(estadoPendiente);

        Facturacion facturacion = new Facturacion();
        facturacion.setServicio(servicio);
        facturacion.setCantidad(1);
        reserva.addFacturacion(facturacion);

        repository.save(reserva);
    }

    public List<ReservaServicio> buscarPorFiltros(String matricula, boolean soloPendientes) {
        List<ReservaServicio> reservas = matricula != null && !matricula.isBlank()
                ? repository.findByMatriculaContainingIgnoreCase(matricula)
                : repository.findAll();

        if (soloPendientes) {
            return reservas.stream()
                    .filter(reserva -> reserva.getEstado() != null
                            && "PENDIENTE".equalsIgnoreCase(reserva.getEstado().getEstado()))
                    .toList();
        }

        return reservas;
    }

    public List<ReservaServicio> buscarPorFiltros(String matricula, boolean soloPendientes, String sortField, String dir) {
        List<ReservaServicio> list = buscarPorFiltros(matricula, soloPendientes);
        return sortList(list, sortField, dir);
    }

    private List<ReservaServicio> sortList(List<ReservaServicio> list, String sortField, String dir) {
        if (sortField == null || sortField.isBlank()) return list;
        Comparator<ReservaServicio> cmp;
        boolean desc = "desc".equalsIgnoreCase(dir);

        switch (sortField) {
            case "nombre":
                cmp = Comparator.comparing(ReservaServicio::getNombreCliente, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "fecha":
                cmp = Comparator.comparing(ReservaServicio::getFecha, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "precio":
                cmp = Comparator.comparing(ReservaServicio::getTotal, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "estado":
                cmp = Comparator.comparing(reserva -> reserva.getEstado() != null ? reserva.getEstado().getEstado() : "", Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            default:
                return list;
        }

        if (desc) cmp = cmp.reversed();
        return list.stream().sorted(cmp).collect(Collectors.toList());
    }

    public ReservaServicio buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public boolean iniciarReserva(Long id) {
        Optional<ReservaServicio> optionalReserva = repository.findById(id);
        if (optionalReserva.isPresent()) {
            ReservaServicio reserva = optionalReserva.get();
            if (reserva.getEstado() != null && "PENDIENTE".equalsIgnoreCase(reserva.getEstado().getEstado())) {
                reserva.setEstado(obtenerEstado("EN_PROCESO"));
                repository.save(reserva);
                return true;
            }
        }
        return false;
    }

    public boolean finalizarReserva(Long id) {
        Optional<ReservaServicio> optionalReserva = repository.findById(id);
        if (optionalReserva.isPresent()) {
            ReservaServicio reserva = optionalReserva.get();
            if (reserva.getEstado() != null && "EN_PROCESO".equalsIgnoreCase(reserva.getEstado().getEstado())) {
                reserva.setEstado(obtenerEstado("FINALIZADO"));
                repository.save(reserva);
                return true;
            }
        }
        return false;
    }

    public long contarReservas() {
        return repository.count();
    }

    public long contarPendientes() {
        return repository.findAll().stream()
                .filter(reserva -> reserva.getEstado() != null && "PENDIENTE".equalsIgnoreCase(reserva.getEstado().getEstado()))
                .count();
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
                .map(ReservaServicio::getTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Estado obtenerEstado(String nombreEstado) {
        return estadoRepository.findByEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new IllegalStateException("No existe el estado " + nombreEstado + " en la base de datos."));
    }
}