package com.autolavado.autolavadomvc.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.autolavado.autolavadomvc.model.Estado;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    Optional<Estado> findByEstadoIgnoreCase(String estado);
}