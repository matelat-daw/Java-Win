package com.miapp.repository;

import com.miapp.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    List<Producto> findByCategoria(String categoria);

    Page<Producto> findByCategoria(String categoria, Pageable pageable);
    
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    @Query("SELECT DISTINCT p.categoria FROM Producto p")
    List<String> findAllCategorias();
}