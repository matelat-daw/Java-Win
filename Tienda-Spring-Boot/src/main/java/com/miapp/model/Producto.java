package com.miapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @Positive(message = "El precio debe ser mayor que 0")
    private BigDecimal precio;
    
    @Column(nullable = false)
    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;
    
    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    @PositiveOrZero(message = "El stock debe ser 0 o mayor")
    private int stock;
    
    @Column(nullable = true)
    private String imagen;
}