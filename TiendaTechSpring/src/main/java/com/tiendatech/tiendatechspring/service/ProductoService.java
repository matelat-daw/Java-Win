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
    private int proximaId = 1; // Contador para asignar IDs automáticamente
    
    // TODO 9: crea el constructor con el repositorio como parámetro
    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public void añadir(String nombre, double precio, String categoria, int stock, boolean disponible) {
        // TODO 10: crea un objeto Producto con los parámetros
        // La ID se asigna automáticamente
        Producto p = new Producto(proximaId, nombre, precio, categoria, stock, disponible, false);
        proximaId++; // Incrementar para el siguiente producto
        repository.save(p);
    }

    public List<Producto> obtenerTodos() {
        return repository.findAll();
    }

    public int total() {
        return repository.count();
    }

    /**
     * Valida que la cantidad sea mayor o igual a 0
     * @param cantidad la cantidad a validar
     * @return true si la cantidad es válida, false si es negativa
     */
    private boolean esCantidadValida(int cantidad) {
        return cantidad >= 0;
    }

    public Producto filtrarProducto(String nombre, String categoria, boolean soloDestacados) {
        return repository.findByNombreIgnoreCase(nombre);
    }

    public Producto obtenerStockBajo(int umbral) {
        return repository.findStockBajo(umbral).stream().findFirst().orElse(null);
    }

    public Producto venderProducto(int id, int cantidad) {
        if (!esCantidadValida(cantidad)) {
            return null; // No se puede vender una cantidad negativa
        }
        Producto p = repository.findById(id);
        if (p != null && p.getStock() >= cantidad) {
            p.setStock(p.getStock() - cantidad);
            return p;
        }
        return null; // No se pudo vender (producto no encontrado o stock insuficiente)
    }

    public Producto sumarStock(int id, int cantidad) {
        if (!esCantidadValida(cantidad)) {
            return null; // No se puede sumar una cantidad negativa
        }
        Producto p = repository.findById(id);
        if (p != null) {
            p.setStock(p.getStock() + cantidad);
            return p;
        }
        return null; // No se pudo sumar stock (producto no encontrado)
    }

    public Producto restarStock(int id, int cantidad) {
        if (!esCantidadValida(cantidad)) {
            return null; // No se puede restar una cantidad negativa
        }
        Producto p = repository.findById(id);
        if (p != null && p.getStock() >= cantidad) {
            p.setStock(p.getStock() - cantidad);
            return p;
        }
        return null; // No se pudo restar stock (producto no encontrado o stock insuficiente)
    }

    public Producto obtenerResumen() {
        String nombre = "Resumen";
        int totalUnidades = repository.contarUnidadesStock();
        double valorTotal = repository.calcularValorStock();
        return new Producto(0, nombre, valorTotal, "N/A", totalUnidades, true, false);
    }
}