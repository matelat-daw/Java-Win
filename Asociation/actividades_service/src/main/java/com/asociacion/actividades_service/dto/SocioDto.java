package com.asociacion.actividades_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocioDto {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String estado;
}