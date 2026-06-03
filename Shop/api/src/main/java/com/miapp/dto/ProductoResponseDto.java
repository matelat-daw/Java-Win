package com.miapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDto {
    private Long id;
    private String nombre;
    private Integer stock;
    private BigDecimal precio;
    private String categoria;
    private String descripcion;
    private String imagen;
    private Boolean activo;
}
