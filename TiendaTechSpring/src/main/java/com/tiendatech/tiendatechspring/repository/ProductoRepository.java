package com.tiendatech.tiendatechspring.repository;

import com.tiendatech.tiendatechspring.model.Producto;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

// TODO 5: añade la anotación @Repository encima de la clase
// Así Spring sabe que debe gestionar este objeto como un Bean.
@Repository
public class ProductoRepository {
    private final List<Producto> productos = new ArrayList<>();

    public List<Producto> findAll() {
        return productos;
    }

    public void save(Producto producto) {
        // TODO 6: añade el producto a la lista
        // Pista: productos.add(producto);
        productos.add(producto);
    }

    public int count() {
        return productos.size();
    }
}