package com.autolavado.autolavadomvc.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public enum TipoServicio {

    BÁSICO("BÁSICO", new BigDecimal("8.00")),
    COMPLETO("COMPLETO", new BigDecimal("15.00")),
    PREMIUM("PREMIUM", new BigDecimal("25.00")),
    INTERIOR("Limpieza interior", new BigDecimal("12.00")),
    PULIDO_FAROS("Pulido de faros", new BigDecimal("18.00")),
    ASPIRADO("Aspirado profesional", new BigDecimal("6.00"));

    private final String descripcion;
    private final BigDecimal precio;

    TipoServicio(String descripcion, BigDecimal precio) {
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public static Optional<TipoServicio> fromDescripcion(String descripcion) {
        if (descripcion == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(tipoServicio -> tipoServicio.descripcion.equalsIgnoreCase(descripcion.trim()))
                .findFirst();
    }
}