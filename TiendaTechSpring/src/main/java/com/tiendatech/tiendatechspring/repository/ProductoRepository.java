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

    public Producto findById(int id) {
        return productos.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Producto findByNombreIgnoreCase(String nombre) {
        return productos.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
    }

    public Producto findByCategoriaIgnoreCase(String categoria) {
        return productos.stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase(categoria))
                .findFirst()
                .orElse(null);
    }

    public List<Producto> filtrarAvanzado(String texto, String categoria, boolean soloDestacados) {
        return productos.stream().filter(
            p -> (texto == null || p.getNombre().toLowerCase().contains(texto.toLowerCase())) &&
            (categoria == null || p.getCategoria().equalsIgnoreCase(categoria)) &&
            (!soloDestacados || p.isDestacado())).toList();
    }

    public List<Producto> findStockBajo(int umbral) {
        return productos.stream()
                .filter(p -> p.getStock() < umbral)
                .toList();
    }

    public int contarUnidadesStock() {
        return productos.stream()
                .mapToInt(Producto::getStock)
                .sum();
    }

    public double calcularValorStock() {
        return productos.stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum();
    }

    public Producto productoMasCaro() {
        return productos.stream()
                .max((p1, p2) -> Double.compare(p1.getPrecio(), p2.getPrecio()))
                .orElse(null);
    }

    public Producto productoMenosStock() {
        return productos.stream()
                .min((p1, p2) -> Integer.compare(p1.getStock(), p2.getStock()))
                .orElse(null);
    }

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