package com.tiendatech.tiendatechspring.model;

public class Producto {
    // TODO 1: declara tres atributos privados:
    // nombre -> String
    // precio -> double
    // categoria -> String
    private String nombre;
    private double precio;
    private String categoria;
    // TODO 2: crea el constructor vacío
    public Producto() { }
    // TODO 3: crea el constructor con los tres campos
    public Producto(String nombre, double precio, String categoria) { 
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
    }
    // TODO 4: crea getters y setters para los tres atributos
    // Ejemplo del primero:
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}