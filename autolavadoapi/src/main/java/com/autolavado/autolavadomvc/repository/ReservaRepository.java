package com.autolavado.autolavadomvc.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.autolavado.autolavadomvc.model.ReservaServicio;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaServicio, Long> {

    List<ReservaServicio> findByMatriculaContainingIgnoreCase(String texto);

    Page<ReservaServicio> findByMatriculaContainingIgnoreCase(String texto, Pageable pageable);

    Page<ReservaServicio> findByEstado_EstadoIgnoreCase(String estado, Pageable pageable);

    Page<ReservaServicio> findByMatriculaContainingIgnoreCaseAndEstado_EstadoIgnoreCase(String texto, String estado, Pageable pageable);
}
