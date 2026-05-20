package com.tiendatech.tiendatechspring.controller;

import com.tiendatech.tiendatechspring.model.Producto;
import com.tiendatech.tiendatechspring.service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

// TODO 11: añade la anotación @RestController encima de la clase
@RestController
@RequestMapping("/api")
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

    // GET /productos/añadir?nombre=X&precio=Y&categoria=Z&stock=W&disponible=V
    @GetMapping("/productos/añadir")
    public String añadir(
            @RequestParam String nombre,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam int stock,
            @RequestParam boolean disponible) {
        // TODO 15: llama a service.añadir() con los parámetros
        // La ID se asigna automáticamente en el servicio
        service.añadir(nombre, precio, categoria, stock, disponible);
        return "Producto añadido: " + nombre;
    }

    // GET /info — información general de la app
    @GetMapping("/productos/info")
    public String info() {
        // TODO 16: devuelve un String con el total de productos.
        // Pista: "TiendaTechSpring · Productos en catálogo: " + service.total()
        return "TiendaTechSpring · Productos en catálogo: " + service.total();
    }

    @GetMapping("/productos/filtrar")
    public Producto filtrar(
            @RequestParam String nombre,
            @RequestParam String categoria,
            @RequestParam boolean soloDestacados) {
        return service.filtrarProducto(nombre, categoria, soloDestacados);
    }

    @GetMapping("/productos/stock-bajo")
    public Producto stockBajo(@RequestParam int umbral) {
        return service.obtenerStockBajo(umbral);
    }

    @GetMapping("/productos/vender")
    public Producto vender(
            @RequestParam int id,
            @RequestParam int cantidad) {
        return service.venderProducto(id, cantidad);
    }

    @GetMapping("/productos/sumar-stock")
    public Producto sumarStock(
            @RequestParam int id,
            @RequestParam int cantidad) {
        return service.sumarStock(id, cantidad);
    }

    @GetMapping("/productos/restar-stock")
    public Producto restarStock(
            @RequestParam int id,
            @RequestParam int cantidad) {
        return service.restarStock(id, cantidad);
    }

    @GetMapping("/resumen")
    public Producto resumen() {
        return service.obtenerResumen();
    }
}