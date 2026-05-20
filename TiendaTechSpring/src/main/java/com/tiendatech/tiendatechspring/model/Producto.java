package com.tiendatech.tiendatechspring.model;

public class Producto {
    // TODO 1: declara tres atributos privados:
    // nombre -> String
    // precio -> double
    // categoria -> String
    private int id;
    private String nombre;
    private double precio;
    private String categoria;
    private int stock;
    private boolean disponible;
    private boolean destacado;
    // TODO 2: crea el constructor vacío
    public Producto() { }
    // TODO 3: crea el constructor con los tres campos
    public Producto(int id, String nombre, double precio, String categoria, int stock, boolean disponible, boolean destacado) { 
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.stock = stock;
        this.disponible = disponible;
        this.destacado = destacado;
    }
    // TODO 4: crea getters y setters para los tres atributos
    // Ejemplo del primero:
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }
}