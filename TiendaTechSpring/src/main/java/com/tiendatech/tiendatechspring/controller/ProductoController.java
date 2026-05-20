package com.tiendatech.tiendatechspring.controller;

import com.tiendatech.tiendatechspring.model.Producto;
import com.tiendatech.tiendatechspring.service.ProductoService;
import com.tiendatech.tiendatechspring.dto.ResumenDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // GET /productos/añadir?nombre=X&precio=Y&categoria=Z&stock=W&disponible=V&destacado=D
    @GetMapping("/productos/añadir")
    public String añadir(
            @RequestParam String nombre,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam int stock,
            @RequestParam boolean disponible,
            @RequestParam boolean destacado) {
        // TODO 15: llama a service.añadir() con los parámetros
        // La ID se asigna automáticamente en el servicio
        service.añadir(nombre, precio, categoria, stock, disponible, destacado);
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
    public List<Producto> filtrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) boolean destacado) {
        return service.filtrarProductos(nombre, categoria, destacado);
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
    public ResumenDTO resumen() {
        return service.obtenerResumen();
    }

    // ═══ OPERACIONES PUT (RESTful) ═══════════════════════════════════

    @PutMapping("/productos/{id}/vender")
    public String venderProducto(@PathVariable int id) {
        Producto p = service.venderProducto(id, 1);
        if (p != null) {
            return "Venta realizada. Stock de " + p.getNombre() + " actualizado a " + p.getStock();
        } else {
            return "Error: No se pudo realizar la venta";
        }
    }

    @PutMapping("/productos/{id}/aumentar")
    public String aumentarStock(@PathVariable int id) {
        Producto p = service.sumarStock(id, 1);
        if (p != null) {
            return "Stock aumentado. " + p.getNombre() + " ahora tiene " + p.getStock() + " unidades";
        } else {
            return "Error: Producto no encontrado";
        }
    }

    @PutMapping("/productos/{id}/disminuir")
    public String disminuirStock(@PathVariable int id) {
        Producto p = service.restarStock(id, 1);
        if (p != null) {
            return "Stock disminuido. " + p.getNombre() + " ahora tiene " + p.getStock() + " unidades";
        } else {
            return "Error: No se pudo disminuir el stock";
        }
    }
}