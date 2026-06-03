package com.futureprograms.clients.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.futureprograms.clients.enums.Gender;
import com.futureprograms.clients.enums.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
        joinColumns = @JoinColumn(name = "userId"),
        inverseJoinColumns = @JoinColumn(name = "roleId")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

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
    public Role getRole() {
        if (roles == null || roles.isEmpty()) {
            return Role.USER;
        }

        boolean hasAdmin = roles.stream()
                .map(RoleEntity::getName)
                .filter(n -> n != null && !n.isBlank())
                .anyMatch(n -> matchesRoleName(n, Role.ADMIN.name()));
        if (hasAdmin) {
            return Role.ADMIN;
        }

        boolean hasPremium = roles.stream()
                .map(RoleEntity::getName)
                .filter(n -> n != null && !n.isBlank())
                .anyMatch(n -> matchesRoleName(n, Role.PREMIUM.name()));
        if (hasPremium) {
            return Role.PREMIUM;
        }

        return Role.USER;
    }

    private static boolean matchesRoleName(String dbRoleName, String roleName) {
        String normalized = dbRoleName.trim();
        if (normalized.regionMatches(true, 0, "ROLE_", 0, 5)) {
            normalized = normalized.substring(5);
        }
        return roleName.equalsIgnoreCase(normalized);
    }
}
