package com.miapp;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("tiendaBean")
@SessionScoped
public class TiendaBean implements Serializable {
    
    private Producto productoForm = new Producto();  // Objeto para el formulario
    private List<Producto> productos = new ArrayList<>();

    public String guardarProducto() {
        // Crear una copia del producto del formulario
        Producto nuevo = new Producto(
            productoForm.getNombre(),
            productoForm.getPrecio(),
            productoForm.getCategoria(),
            productoForm.isDisponible(),
            productoForm.getStock()
        );
        productos.add(nuevo);
        productoForm = new Producto();  // Limpiar formulario
        return "listado-productos?faces-redirect=true";
    }

    public String volverAlFormulario() {
        return "alta-producto?faces-redirect=true";
    }

    // Getters y setters simples
    public Producto getProductoForm() {
        return productoForm;
    }

    public void setProductoForm(Producto productoForm) {
        this.productoForm = productoForm;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}