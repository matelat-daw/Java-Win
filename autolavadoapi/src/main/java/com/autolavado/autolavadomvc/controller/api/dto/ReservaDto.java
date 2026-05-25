package com.autolavado.autolavadomvc.controller.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaDto(
        Long id,
        String nombreCliente,
        String telefono,
        String matricula,
        String estado,
        LocalDate fecha,
        LocalTime hora,
        String observaciones,
        ServicioDto servicio,
        BigDecimal total
) {
}
