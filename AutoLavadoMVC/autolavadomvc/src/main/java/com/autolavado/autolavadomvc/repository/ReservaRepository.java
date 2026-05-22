package com.autolavado.autolavadomvc.repository;
 
import com.autolavado.autolavadomvc.model.EstadoReserva;
import com.autolavado.autolavadomvc.model.ReservaLavado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository 
// public class ReservaRepository {
public interface ReservaRepository extends JpaRepository<ReservaLavado, Long> {

    List<ReservaLavado> findByMatriculaContainingIgnoreCase(String texto); 
 
    // TODO 19: Devolver reservas con ese estado
    List<ReservaLavado> findByEstado(EstadoReserva estado);
} 