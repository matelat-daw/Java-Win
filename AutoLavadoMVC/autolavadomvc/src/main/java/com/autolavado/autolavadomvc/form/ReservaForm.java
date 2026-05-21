package com.autolavado.autolavadomvc.form; 
 
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaForm { 
 
    // TODO 5: nombreCliente debe ser obligatorio y tener entre 3 y 80 caracteres.
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    private String nombreCliente;

    private boolean tipoTelefono = true; // true para móvil español, false para internacional.
 
    // TODO 6: telefono debe ser obligatorio. 
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^(?:[67](?:[\\s-]?\\d){8}|(?:\\+|00)(?:[\\s-]?\\d){7,15})$",
        message = "El teléfono debe ser un móvil español de 9 cifras que empiece por 6 o 7, o un teléfono internacional con prefijo"
    )
    private String telefono;

    private boolean tipoMatricula = true; // true para nacional, false para extranjera
 
    // TODO 7: matricula debe ser obligatoria y tener entre 6 y 10 caracteres. 
    @NotBlank(message = "La matrícula es obligatoria")
    @Size(min = 6, max = 12, message = "La matrícula debe tener entre 6 y 12 caracteres")
    @Pattern(
        regexp = "^(?i)(?:\\d{4}[ -]?[A-Z]{3}|[A-Z]{1,2}[ -]?\\d{4}[ -]?[A-Z]{0,2}|[A-Z]{1,3}[ -]?[A-Z]{1,2}[ -]?\\d{1,4})$",
        message = "La matrícula no tiene un formato válido"
    )
    private String matricula; 
 
    // TODO 8: tipoLavado debe ser obligatorio. 
    @NotBlank(message = "El tipo de lavado es obligatorio")
    private String tipoLavado; 
 
    // TODO 9: fecha debe ser obligatoria y no puede ser anterior a hoy. 
    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
    private LocalDate fecha; 
 
    // TODO 10: hora debe ser obligatoria. 
    @NotBlank(message = "La hora es obligatoria")
    private String hora; 
 
    // TODO 11: observaciones puede estar vacío, pero no debe superar 200 caracteres. 
    @Size(max = 200, message = "Las observaciones no pueden superar 200 caracteres")
    private String observaciones; 
}