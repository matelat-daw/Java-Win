package com.tiendatech.tiendatechspring.dto;

import java.util.Map;

public class ResumenDTO {
    private int totalProductos;
    private int totalUnidades;
    private double valorStock;
    private int productosStockBajo;
    private int productosDestacados;
    private Map<String, Integer> resumenPorCategoria;

    public ResumenDTO() {
    }

    public ResumenDTO(int totalProductos, int totalUnidades, double valorStock,
                      int productosStockBajo, int productosDestacados,
                      Map<String, Integer> resumenPorCategoria) {
        this.totalProductos = totalProductos;
        this.totalUnidades = totalUnidades;
        this.valorStock = valorStock;
        this.productosStockBajo = productosStockBajo;
        this.productosDestacados = productosDestacados;
        this.resumenPorCategoria = resumenPorCategoria;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(int totalProductos) {
        this.totalProductos = totalProductos;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(int totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public double getValorStock() {
        return valorStock;
    }

    public void setValorStock(double valorStock) {
        this.valorStock = valorStock;
    }

    public int getProductosStockBajo() {
        return productosStockBajo;
    }

    public void setProductosStockBajo(int productosStockBajo) {
        this.productosStockBajo = productosStockBajo;
    }

    public int getProductosDestacados() {
        return productosDestacados;
    }

    public void setProductosDestacados(int productosDestacados) {
        this.productosDestacados = productosDestacados;
    }

    public Map<String, Integer> getResumenPorCategoria() {
        return resumenPorCategoria;
    }

    public void setResumenPorCategoria(Map<String, Integer> resumenPorCategoria) {
        this.resumenPorCategoria = resumenPorCategoria;
    }

    @Override
    public String toString() {
        return "ResumenDTO{" +
                "totalProductos=" + totalProductos +
                ", totalUnidades=" + totalUnidades +
                ", valorStock=" + valorStock +
                ", productosStockBajo=" + productosStockBajo +
                ", productosDestacados=" + productosDestacados +
                ", resumenPorCategoria=" + resumenPorCategoria +
                '}';
    }
}
