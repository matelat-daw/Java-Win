package com.asociaciondomitila.projects.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.asociaciondomitila.projects.enums.Gender;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "`user`")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nick;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname1;

    @Column(nullable = true)
    private String surname2;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = true)
    private LocalDate bday;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<com.asociaciondomitila.projects.entity.Role> roles = new HashSet<>();

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String profileImg;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = true)
    private String verificationToken;

    @Column(nullable = true)
    private LocalDateTime verificationTokenExpiry;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Compatibilidad temporal: mantiene el contrato actual de la API que espera un unico rol.
     */
    public com.asociaciondomitila.projects.enums.Role getRole() {
        if (roles == null || roles.isEmpty()) {
            return com.asociaciondomitila.projects.enums.Role.USER;
        }

        boolean hasAdmin = roles.stream().anyMatch(
                r -> com.asociaciondomitila.projects.enums.Role.ADMIN.name().equalsIgnoreCase(r.getName())
        );
        if (hasAdmin) {
            return com.asociaciondomitila.projects.enums.Role.ADMIN;
        }

        boolean hasPremium = roles.stream().anyMatch(
                r -> com.asociaciondomitila.projects.enums.Role.PREMIUM.name().equalsIgnoreCase(r.getName())
        );
        if (hasPremium) {
            return com.asociaciondomitila.projects.enums.Role.PREMIUM;
        }

        return com.asociaciondomitila.projects.enums.Role.USER;
    }
}
