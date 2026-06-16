package com.asociaciondomitila.projects.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`user`")
public class BeneficiaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 24)
    private String surname1;

    @Column(nullable = true, length = 24)
    private String surname2;

    @Column(nullable = false, length = 10, unique = true)
    private String dni;

    @Column(nullable = false, length = 128)
    private String address;

    @Column(name = "cp", nullable = false)
    private Integer postalCode;

    @Column(nullable = false, unique = true)
    private Long phone;

    @Column(nullable = false, length = 64, unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
        name = "projetc_user",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new LinkedHashSet<>();
}
