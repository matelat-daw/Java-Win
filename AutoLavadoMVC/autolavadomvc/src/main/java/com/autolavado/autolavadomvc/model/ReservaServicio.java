package com.autolavado.autolavadomvc.model;
 
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import jakarta.persistence.*; // <- Importación de todas las anotaciones de persistencia
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity // 1. Indica que esta clase es una entidad de Base de Datos
@Table(name = "reserva") // 2. REEMPLAZA por el nombre real de tu tabla en MariaDB
public class ReservaServicio { 
 
    @Id // 3. Define la Clave Primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. Mapea el AUTO_INCREMENT de tu base de datos
    @Column(name = "id", columnDefinition = "INT UNSIGNED")
    private Long id;
    
    @Column(name = "nombre_cliente", length = 64, nullable = false) // 5. Mapea varchar(64) snake_case
    private String nombreCliente;
    
    @Column(name = "telefono", length = 15, nullable = false)
    private String telefono;
    
    @Column(name = "matricula", length = 12, nullable = false)
    private String matricula;
    
    // 6. Al no estar en la base de datos, @Transient evita que JPA intente buscar las columnas
    @Transient 
    private boolean tipoTelefono;
    
    @Transient 
    private boolean tipoMatricula;
    
    // 7. Mapea el ENUM como String en la base de datos para que coincida con enum('BÁSICO'...)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false)
    private TipoServicio tipoServicio;
    
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    
    // 8. Cambiado a LocalTime para que Hibernate entienda de forma nativa el tipo 'time' de MariaDB
    @Column(name = "hora", nullable = false)
    private LocalTime hora;
    
    @Column(name = "precio", precision = 10, scale = 2, nullable = false) // Mapea decimal(10,2)
    private BigDecimal precio;
    
    @Enumerated(EnumType.STRING) // Mapea el enum('PENDIENTE'...)
    @Column(name = "estado", nullable = false)
    private EstadoReserva estado;
    
    @Column(name = "observaciones", length = 256, nullable = false)
    private String observaciones;
}