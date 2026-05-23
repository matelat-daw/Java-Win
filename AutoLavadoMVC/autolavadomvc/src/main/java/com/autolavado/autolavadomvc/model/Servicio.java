package com.autolavado.autolavadomvc.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED")
    private Integer id;

    @Column(name = "servicio", length = 32, nullable = false)
    private String servicio;

    @Column(name = "precio", precision = 11, scale = 2, nullable = false)
    private BigDecimal precio;

    @Column(name = "descripcion", length = 80)
    private String descripcion;

    @Column(name = "imagen", length = 120)
    private String imagen;
}