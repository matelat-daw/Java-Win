package com.autolavado.autolavadomvc.controller.api.dto;

import java.math.BigDecimal;

public record ServicioDto(
        Integer id,
        String servicio,
        BigDecimal precio,
        String descripcion,
        String imagen
) {
}
