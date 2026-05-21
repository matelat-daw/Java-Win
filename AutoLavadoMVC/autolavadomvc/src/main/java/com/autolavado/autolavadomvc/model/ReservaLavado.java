package com.autolavado.autolavadomvc.model;
 
import java.time.LocalDate;
import java.math.BigDecimal;
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
    private boolean tipoTelefono;
    private boolean tipoMatricula;
    private TipoLavado tipoLavado;
    private LocalDate fecha;
    private String hora;
    private BigDecimal precio;
    private EstadoReserva estado;
    private String observaciones;
}