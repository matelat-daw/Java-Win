package com.miapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactoRequestDto(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank String mensaje
) {
}
