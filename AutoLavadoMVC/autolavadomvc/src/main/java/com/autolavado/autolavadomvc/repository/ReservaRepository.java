package com.autolavado.autolavadomvc.repository; 
 
import com.autolavado.autolavadomvc.model.ReservaLavado; 
import org.springframework.stereotype.Repository; 
import java.util.ArrayList; 
import java.util.List;

@Repository 
public class ReservaRepository { 
 
    private final List<ReservaLavado> reservas = new ArrayList<>(); 
    private Long siguienteId = 1L; 
 
    public List<ReservaLavado> findAll() { 
        // TODO 13: devolver la lista de reservas. 
        return reservas; 
    } 
 
    public void save(ReservaLavado reserva) { 
        // TODO 14: si la reserva no tiene id, asignar siguienteId y aumentar siguienteId.
        if (reserva.getId() == 0) {
            reserva.setId(siguienteId);
            siguienteId++;
        }
        // TODO 15: añadir la reserva a la lista. 
        reservas.add(reserva);
    } 
 
    public ReservaLavado findById(Long id) { 
        // TODO 16: recorrer la lista y devolver la reserva cuyo id coincida. 
        // Si no existe, devolver null.
        return reservas.stream()
                .filter(reserva -> reserva.getId().equals(id))
                .findFirst()
                .orElse(null);
    } 
 
    public List<ReservaLavado> findByMatricula(String texto) { 
        // TODO 17: buscar reservas cuya matrícula contenga el texto recibido. 
        // La búsqueda debe ignorar mayúsculas y minúsculas.
        return reservas.stream()
                .filter(reserva -> reserva.getMatricula().toLowerCase().contains(texto.toLowerCase()))
                .toList();
    } 
 
    public int count() { 
        // TODO 18: devolver el número total de reservas.
        return reservas.size();
    } 
 
    public List<ReservaLavado> findByEstado(String estado) { 
        // TODO 19: reto. Devolver reservas con ese estado.
        return reservas.stream()
                .filter(reserva -> reserva.getEstado().equalsIgnoreCase(estado))
                .toList();
    } 
} 