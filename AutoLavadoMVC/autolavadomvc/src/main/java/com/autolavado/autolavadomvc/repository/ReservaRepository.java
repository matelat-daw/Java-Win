package com.autolavado.autolavadomvc.repository;
 
import com.autolavado.autolavadomvc.model.EstadoReserva;
import com.autolavado.autolavadomvc.model.ReservaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository 
// public class ReservaRepository {
public interface ReservaRepository extends JpaRepository<ReservaServicio, Long> {

    List<ReservaServicio> findByMatriculaContainingIgnoreCase(String texto); 
 
    // TODO 19: Devolver reservas con ese estado
    List<ReservaServicio> findByEstado(EstadoReserva estado);
} 