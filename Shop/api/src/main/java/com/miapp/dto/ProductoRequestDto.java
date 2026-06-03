package com.miapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoRequestDto {
    @NotBlank
    @Size(min = 2, max = 80)
    private String nombre;

    @PositiveOrZero
    private Integer stock;

    @NotNull
    @Positive
    private BigDecimal precio;

    @NotNull
    @Positive
    private Long categoria;

    private String descripcion;
    private String imagen;
    private Boolean activo;

    @JsonSetter("categoria")
    public void setCategoria(Object categoria) {
        if (categoria == null) {
            this.categoria = null;
            return;
        }

        if (categoria instanceof Number numero) {
            this.categoria = numero.longValue();
            return;
        }

        if (categoria instanceof Map<?, ?> mapa) {
            Object id = mapa.get("id");
            if (id instanceof Number numero) {
                this.categoria = numero.longValue();
                return;
            }
            if (id != null) {
                String valorId = id.toString().trim();
                if (!valorId.isEmpty()) {
                    try {
                        this.categoria = Long.parseLong(valorId);
                        return;
                    } catch (NumberFormatException ignored) {
                        this.categoria = null;
                        return;
                    }
                }
            }
            this.categoria = null;
            return;
        }

        String valor = categoria.toString().trim();
        if (valor.isEmpty()) {
            this.categoria = null;
            return;
        }

        try {
            this.categoria = Long.parseLong(valor);
        } catch (NumberFormatException ignored) {
            this.categoria = null;
        }
    }
}
