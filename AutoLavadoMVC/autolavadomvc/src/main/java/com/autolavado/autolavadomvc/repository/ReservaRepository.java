package com.autolavado.autolavadomvc.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.autolavado.autolavadomvc.model.ReservaServicio;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaServicio, Long> {

    List<ReservaServicio> findByMatriculaContainingIgnoreCase(String texto);
}