package com.miapp.repository;

import com.miapp.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoria(Long categoria);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    Page<Producto> findByCategoria(Long categoria, Pageable pageable);
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    Page<Producto> findByCategoriaAndNombreContainingIgnoreCase(Long categoria, String nombre, Pageable pageable);

    @Query("SELECT DISTINCT p.categoria FROM Producto p")
    List<Long> findAllCategorias();
}
