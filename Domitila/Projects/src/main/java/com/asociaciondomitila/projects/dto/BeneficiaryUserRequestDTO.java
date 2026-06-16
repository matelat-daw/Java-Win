package com.asociaciondomitila.projects.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.asociaciondomitila.projects.validation.ValidDniNie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryUserRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 32, message = "El nombre no puede superar los 32 caracteres")
    private String name;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 24, message = "El primer apellido no puede superar los 24 caracteres")
    private String surname1;

    @NotBlank(message = "El segundo apellido es obligatorio")
    @Size(max = 24, message = "El segundo apellido no puede superar los 24 caracteres")
    private String surname2;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 10, message = "El DNI no puede superar los 10 caracteres")
    @ValidDniNie
    private String dni;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 128, message = "La direccion no puede superar los 128 caracteres")
    private String address;

    @NotNull(message = "El codigo postal es obligatorio")
    private Integer postalCode;

    @NotNull(message = "El telefono es obligatorio")
    private Long phone;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es valido")
    @Size(max = 64, message = "El email no puede superar los 64 caracteres")
    private String email;
}
