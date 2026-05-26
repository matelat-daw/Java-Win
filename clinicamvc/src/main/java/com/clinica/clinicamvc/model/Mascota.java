package com.clinica.clinicamvc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {
    private int id;
    private String nombre;
    private String especie;
    private String propietario;
    private String telefono;
    private String estado;
}