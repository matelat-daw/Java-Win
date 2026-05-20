package com.miapp;

import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("productoBean")
@RequestScoped

 public class Producto implements Serializable {

 // TODO 1: crea los atributos privados:
 // nombre -> String
 // precio -> double
 // categoria -> String
 // disponible -> boolean
    private String nombre;
    private double precio;
    private String categoria;
    private boolean disponible;
     private int stock;

 // TODO 2: crea el constructor vacío
 // Pista: public Producto() { }
    public Producto() {
    }

 // TODO 3: crea el constructor con todos los campos
    public Producto(String nombre, double precio, String categoria, boolean disponible, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.disponible = disponible;
        this.stock = stock;
    }

 // TODO 4: crea getters y setters
 // Pista para el primer atributo:
 // public String getNombre() { return nombre; }
 // public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}