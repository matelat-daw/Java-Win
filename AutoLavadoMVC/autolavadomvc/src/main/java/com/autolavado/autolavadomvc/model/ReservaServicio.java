package com.autolavado.autolavadomvc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reserva")
public class ReservaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @Column(name = "nombre_cliente", length = 80, nullable = false)
    private String nombreCliente;

    @Column(name = "telefono", length = 12, nullable = false)
    private String telefono;

    @Column(name = "matricula", length = 15, nullable = false)
    private String matricula;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "observaciones", length = 200)
    private String observaciones;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Facturacion> facturaciones = new ArrayList<>();

    public void addFacturacion(Facturacion facturacion) {
        if (facturacion == null) {
            return;
        }
        facturaciones.add(facturacion);
        facturacion.setReserva(this);
    }

    @Transient
    public Facturacion getFacturacionPrincipal() {
        return facturaciones == null || facturaciones.isEmpty() ? null : facturaciones.get(0);
    }

    @Transient
    public String getNombreServicio() {
        Facturacion facturacion = getFacturacionPrincipal();
        return facturacion != null && facturacion.getServicio() != null ? facturacion.getServicio().getServicio() : null;
    }

    @Transient
    public BigDecimal getTotal() {
        if (facturaciones == null || facturaciones.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return facturaciones.stream()
                .filter(facturacion -> facturacion.getServicio() != null && facturacion.getServicio().getPrecio() != null)
                .map(facturacion -> facturacion.getServicio().getPrecio().multiply(BigDecimal.valueOf(facturacion.getCantidad() == null ? 0 : facturacion.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}