package com.autolavado.autolavadomvc.model;
 
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaLavado { 
 
    // TODO 1: declara los atributos privados: 
    // id, nombreCliente, telefono, matricula, tipoLavado, 
    // fecha, hora, precio, estado y observaciones.
    private Long id;
    private String nombreCliente;
    private String telefono;
    private String matricula;
    private String tipoLavado;
    private LocalDate fecha;
    private String hora;
    private Double precio;
    private String estado;
    private String observaciones;
}