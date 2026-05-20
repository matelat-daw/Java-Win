package com.tiendatech.tiendatechspring.controller;

import com.tiendatech.tiendatechspring.model.Producto;
import com.tiendatech.tiendatechspring.service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

// TODO 11: añade la anotación @RestController encima de la clase
@RestController
public class ProductoController {
    // TODO 12: declara el atributo final del servicio
    private final ProductoService service;
    // TODO 13: crea el constructor con el servicio como parámetro
    // (igual que hiciste en ProductoService con el repositorio)
    // GET /productos — devuelve todos los productos
    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping("/productos")
    public List<Producto> listar() {
        // TODO 14: devuelve la lista llamando a service.obtenerTodos()
        return service.obtenerTodos();
    }

    // GET /productos/añadir?nombre=X&precio=Y&categoria=Z
    @GetMapping("/productos/añadir")
    public String añadir(
            @RequestParam String nombre,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam int stock) {
        // TODO 15: llama a service.añadir() con los tres parámetros
        // y devuelve un mensaje de confirmación.
        // Pista:
        service.añadir(nombre, precio, categoria, stock);
        return "Producto añadido: " + nombre;
    }

    // GET /info — información general de la app
    @GetMapping("/info")
    public String info() {
        // TODO 16: devuelve un String con el total de productos.
        // Pista: "TiendaTechSpring · Productos en catálogo: " + service.total()
        return "TiendaTechSpring · Productos en catálogo: " + service.total();
    }
}