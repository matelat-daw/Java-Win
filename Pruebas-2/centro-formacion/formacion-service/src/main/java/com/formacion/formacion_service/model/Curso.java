package com.formacion.formacion_service.model;

import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "curso")
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @ToString.Include
    private String titulo;
    private String modalidad;
    private int duracion;

    @ManyToMany(mappedBy = "cursos", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JsonIgnoreProperties(value = "cursos")
    private Set<Alumno> alumnos = new LinkedHashSet<>();

    @PrePersist
    @PreUpdate
    private void normalizeTitulo() {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
