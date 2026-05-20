package com.miapp.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene información general de la tienda
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TiendaInfoDTO implements Serializable {
    private String nombreTienda;
    private String direccion;
    private String telefono;
    private String email;
    private String horario;
}
