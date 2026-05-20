package com.tiendatech.tiendatechspring.dto;

public class ProductoSimplifadoDTO {
    private String nombre;
    private double precio;
    private int stock;

    public ProductoSimplifadoDTO() {
    }

    public ProductoSimplifadoDTO(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public ProductoSimplifadoDTO(String nombre, int stock) {
        this.nombre = nombre;
        this.stock = stock;
    }

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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "ProductoSimplifadoDTO{" +
                "nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                '}';
    }
}
