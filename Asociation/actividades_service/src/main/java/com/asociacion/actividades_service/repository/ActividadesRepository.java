package com.asociacion.actividades_service.repository;

import org.springframework.stereotype.Repository;
import com.asociacion.actividades_service.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface ActividadesRepository extends JpaRepository<Actividad, Integer> {
    List<Actividad> findByTituloContainingIgnoreCase(String texto);
}