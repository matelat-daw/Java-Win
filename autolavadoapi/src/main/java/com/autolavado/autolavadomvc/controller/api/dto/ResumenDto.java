package com.autolavado.autolavadomvc.controller.api.dto;

import java.math.BigDecimal;

public record ResumenDto(
        long total,
        long pendientes,
        BigDecimal ingresos
) {
}
