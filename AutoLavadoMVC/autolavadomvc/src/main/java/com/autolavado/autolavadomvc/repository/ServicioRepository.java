package com.autolavado.autolavadomvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autolavado.autolavadomvc.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
}