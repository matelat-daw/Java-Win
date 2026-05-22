package com.autolavado.autolavadomvc.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public enum TipoLavado {

    BASICO("BÁSICO", new BigDecimal("8.00")),
    COMPLETO("COMPLETO", new BigDecimal("15.00")),
    PREMIUM("PREMIUM", new BigDecimal("25.00"));

    private final String descripcion;
    private final BigDecimal precio;

    TipoLavado(String descripcion, BigDecimal precio) {
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

    public static Optional<TipoLavado> fromDescripcion(String descripcion) {
        if (descripcion == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(tipoLavado -> tipoLavado.descripcion.equalsIgnoreCase(descripcion.trim()))
                .findFirst();
    }
}