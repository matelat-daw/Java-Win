package com.tiendatech.tiendatechspring.service;

import com.tiendatech.tiendatechspring.model.Producto;
import com.tiendatech.tiendatechspring.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

// TODO 7: añade la anotación @Service encima de la clase
@Service
public class ProductoService {
    // TODO 8: declara el atributo final del repositorio
    private final ProductoRepository repository;
    // TODO 9: crea el constructor con el repositorio como parámetro
    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }
    public List<Producto> obtenerTodos() {
        return repository.findAll();
    }

    public void añadir(String nombre, double precio, String categoria, int stock) {
        // TODO 10: crea un objeto Producto con los tres parámetros
        // y guárdalo llamando a repository.save()
        // Pista:
        Producto p = new Producto(nombre, precio, categoria, stock);
        repository.save(p);
    }

    public int total() {
        return repository.count();
    }
}