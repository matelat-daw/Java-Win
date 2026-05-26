package com.clinica.clinicamvc.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaForm {
    // TODO 4: nombre — obligatorio, entre 2 y 60 caracteres.
// Anotaciones: @NotBlank y @Size(min=2, max=60)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String nombre;
// TODO 5: especie — obligatoria.
// Anotación: @NotBlank
    @NotBlank(message = "La especie es obligatoria")
private String especie;
// TODO 6: propietario — obligatorio, entre 2 y 80 caracteres.
    @NotBlank(message = "El propietario es obligatorio")
    @Size(min = 2, max = 80, message = "El propietario debe tener entre 2 y 80 caracteres")
    private String propietario;
// TODO 7: telefono — obligatorio.
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;
// TODO 8: constructor vacío, getters y setters de los cuatro campos.
// Use Lombok para generar automáticamente el constructor vacío, El completo, getters y setters.
}