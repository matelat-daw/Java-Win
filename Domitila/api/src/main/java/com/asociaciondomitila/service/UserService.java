package com.asociaciondomitila.service;

import com.asociaciondomitila.dto.RegisterRequest;
import com.asociaciondomitila.entity.RoleEntity;
import com.asociaciondomitila.entity.User;
import com.asociaciondomitila.enums.Gender;
import com.asociaciondomitila.enums.Role;
import com.asociaciondomitila.repository.RoleRepository;
import com.asociaciondomitila.repository.UserRepository;
import com.asociaciondomitila.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ImageService imageService;

    @Value("${app.email.verification-token-expiration:24h}")
    private Duration emailVerificationTokenExpiration;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if (userRepository.existsByNick(request.getNick())) {
            throw new IllegalArgumentException("El nick ya está en uso");
        }

        Gender gender = Gender.fromEnglishName(request.getGender());
        String profileImg = request.getProfileImg();
        if (profileImg == null || profileImg.trim().isEmpty()) {
            profileImg = gender.getDefaultImagePath();
            log.info("Asignando imagen por defecto para gender: {}", gender.getDisplayName());
        }

        User user = User.builder()
                .nick(request.getNick())
                .name(request.getName())
                .surname1(request.getSurname1())
                .surname2(request.getSurname2())
                .phone(request.getPhone())
                .gender(gender)
                .bday(request.getBday())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImg(profileImg)
                .roles(new HashSet<>(Set.of(getRequiredRole(Role.USER))))
                .active(false)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiry(LocalDateTime.now().plus(emailVerificationTokenExpiration))
                .build();

        user = userRepository.save(user);

        log.info("Usuario registrado: {}", user.getEmail());

        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getName(),
                    user.getVerificationToken()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de verificacion a {}: {}", user.getEmail(), e.getMessage());
            // No interrumpir el registro si falla el proveedor de correo.
        }

        return user;
    }

    @Transactional
    public User verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de verificación inválido"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token de verificación ha expirado");
        }

        user.setEmailVerified(true);
        user.setActive(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user = userRepository.save(user);

        log.info("Email verificado para usuario: {}", user.getEmail());

        // Enviar email de bienvenida
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        return user;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByNick(String nick) {
        return userRepository.findByNick(nick);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User getRequiredUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public User getRequiredUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public User authenticate(String email, String password) {
        User user = getRequiredUserByEmail(email);
        validateUserIsActive(user);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }
        return user;
    }

    public boolean validateCredentials(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> Boolean.TRUE.equals(user.getActive()) && Boolean.TRUE.equals(user.getEmailVerified()))
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    public boolean isPasswordValid(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public User updateUserProfile(Long id, String name, String surname1, String surname2, String phone) {
        User user = getRequiredUserById(id);
        user.setName(name);
        user.setSurname1(surname1);
        user.setSurname2(surname2);
        user.setPhone(phone);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserProfileAsAdmin(Long id, UpdateProfileRequest request) {
        User user = getRequiredUserById(id);
        if (Role.ADMIN.equals(user.getRole())) {
            throw new IllegalStateException("No se puede actualizar usuarios con rol ADMIN");
        }
        user.setName(request.getName());
        user.setSurname1(request.getSurname1());
        user.setSurname2(request.getSurname2());
        user.setPhone(request.getPhone());
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfileImage(Long userId, String imagePath) {
        User user = getRequiredUserById(userId);
        if (user.getProfileImg() != null
                && !imageService.isProtectedImage(user.getProfileImg())
                && !user.getProfileImg().equals(imagePath)) {
            try {
                imageService.deleteImage(user.getProfileImg());
            } catch (Exception e) {
                log.warn("No se pudo eliminar imagen anterior de usuario {}: {}", userId, e.getMessage());
            }
        }

        user.setProfileImg(imagePath);
        return userRepository.save(user);
    }

    @Transactional
    public User changePassword(Long id, String oldPassword, String newPassword) {
        User user = getRequiredUserById(id);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user = userRepository.save(user);

        log.info("Contraseña cambiada para usuario: {}", user.getEmail());
        return user;
    }

    @Transactional
    public void deleteUserAccount(Long id, String password) {
        User user = getRequiredUserById(id);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        deleteUserAssets(user);
        userRepository.delete(user);
        log.info("Cuenta eliminada permanentemente: {}", user.getEmail());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getRequiredUserById(id);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("No se puede eliminar usuarios con rol ADMIN");
        }

        deleteUserAssets(user);
        userRepository.delete(user);
    }

    @Transactional
    public User changeUserRole(Long userId, String roleName) {
        User user = getRequiredUserById(userId);
        Role role = Role.fromDisplayName(roleName);
        user.getRoles().clear();
        user.getRoles().add(getRequiredRole(role));
        return userRepository.save(user);
    }

    public Page<User> getUsersExcludingRole(Role role, Pageable pageable) {
        return userRepository.findAllExcludingRoleName(role.name(), pageable);
    }

    public Page<User> getUsersExcludingRoleBySurname(Role role, String surname, Pageable pageable) {
        if (surname == null || surname.isBlank()) {
            return getUsersExcludingRole(role, pageable);
        }
        return userRepository.findAllExcludingRoleNameBySurname(role.name(), surname.trim(), pageable);
    }

    private void validateUserIsActive(User user) {
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Tu cuenta todavia no ha sido verificada. Revisa tu correo electronico y haz clic en el enlace de confirmacion antes de iniciar sesion.");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException("Tu cuenta esta inactiva. Contacta con soporte para reactivarla.");
        }
    }

    private RoleEntity getRequiredRole(Role role) {
        return roleRepository.findByName(role.name())
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado en la base de datos: " + role.name()));
    }

    private void deleteUserAssets(User user) {
        if (user.getProfileImg() == null || user.getProfileImg().isEmpty()) {
            return;
        }

        try {
            imageService.deleteProfileImage(user.getId(), user.getProfileImg());
        } catch (Exception e) {
            log.warn("Error al eliminar imágenes de perfil del usuario {}: {}", user.getId(), e.getMessage());
        }
    }
}
