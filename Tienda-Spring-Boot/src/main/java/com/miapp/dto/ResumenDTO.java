package com.miapp.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene el resumen de datos de la tienda
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDTO implements Serializable {
    private int totalProductos;
    private int totalCategorias;
    private String productoMasCaro;
    private double precioProductoMasCaro;
}
