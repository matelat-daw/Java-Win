package com.tiendatech.tiendatechspring.service;

import com.tiendatech.tiendatechspring.model.Producto;
import com.tiendatech.tiendatechspring.repository.ProductoRepository;
import com.tiendatech.tiendatechspring.dto.ResumenDTO;
import com.tiendatech.tiendatechspring.dto.ProductoSimplifadoDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void añadir(String nombre, double precio, String categoria, int stock, boolean disponible, boolean destacado) {
        // TODO 10: crea un objeto Producto con los parámetros
        // La ID se asigna automáticamente sin pasar desde el frontend
        Producto p = new Producto(proximaId, nombre, precio, categoria, stock, disponible, destacado);
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

    public List<Producto> filtrarProductos(String nombre, String categoria, boolean destacado) {
        List<Producto> todos = repository.findAll();
        
        // Filtrar por nombre si se proporciona
        if (nombre != null && !nombre.isEmpty()) {
            todos = todos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // Filtrar por categoría si se proporciona
        if (categoria != null && !categoria.isEmpty()) {
            todos = todos.stream()
                    .filter(p -> p.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
        }
        
        // Filtrar solo destacados si está marcado
        if (destacado) {
            todos = todos.stream()
                    .filter(Producto::isDestacado)
                    .collect(Collectors.toList());
        }
        
        return todos;
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

    public ResumenDTO obtenerResumen() {
        List<Producto> todos = repository.findAll();
        
        if (todos.isEmpty()) {
            return new ResumenDTO(0, 0, 0, 0, 0, Map.of());
        }

        // Total de productos
        int totalProductos = todos.size();

        // Total de unidades (suma de stock)
        int totalUnidades = todos.stream().mapToInt(Producto::getStock).sum();

        // Valor total del stock (suma de precio * stock)
        double valorStock = todos.stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum();

        // Productos con stock bajo (stock < 5)
        int productosStockBajo = (int) todos.stream()
                .filter(p -> p.getStock() > 0 && p.getStock() < 5)
                .count();

        // Productos destacados
        int productosDestacados = (int) todos.stream()
                .filter(Producto::isDestacado)
                .count();

        // Resumen por categoría (cantidad total de unidades)
        Map<String, Integer> resumenPorCategoria = todos.stream()
                .collect(Collectors.groupingBy(
                        Producto::getCategoria,
                        Collectors.summingInt(Producto::getStock)
                ));

        return new ResumenDTO(
                totalProductos,
                totalUnidades,
                valorStock,
                productosStockBajo,
                productosDestacados,
                resumenPorCategoria
        );
    }
}