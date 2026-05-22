package com.autolavado.autolavadomvc.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiciosLavado {

	private String nombre;
	private String descripcion;
	private BigDecimal precio;
	private int duracionMinutos;
	private String etiqueta;
	private String imagen;
	private TipoServicio tipoServicio;
}