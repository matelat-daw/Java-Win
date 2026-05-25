package com.asociacion.socios_service.repository;

import java.util.List;
import com.asociacion.socios_service.model.Socios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocioRepository extends JpaRepository<Socios, Integer> {

    List<Socios> findByNombreContainingIgnoreCase(String texto);
}