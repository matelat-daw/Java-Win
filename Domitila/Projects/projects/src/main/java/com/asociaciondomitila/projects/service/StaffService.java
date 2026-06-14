package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.dto.RegisterRequest;
import com.asociaciondomitila.projects.entity.Staff;
import com.asociaciondomitila.projects.enums.Gender;
import com.asociaciondomitila.projects.enums.Role;
import com.asociaciondomitila.projects.repository.RoleRepository;
import com.asociaciondomitila.projects.repository.StaffRepository;
import com.asociaciondomitila.projects.dto.UpdateProfileRequest;
import com.asociaciondomitila.projects.util.ApiConstants;
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
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ImageService imageService;

    @Value("${app.email.verification-token-expiration:24h}")
    private Duration emailVerificationTokenExpiration;

    @Transactional
    public Staff registerStaff(RegisterRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(ApiConstants.ERR_EMAIL_EXISTS);
        }

        if (staffRepository.existsByNick(request.getNick())) {
            throw new IllegalArgumentException(ApiConstants.ERR_NICK_EXISTS);
        }

        Gender gender = Gender.fromEnglishName(request.getGender());
        String profileImg = request.getProfileImg();
        if (profileImg == null || profileImg.trim().isEmpty()) {
            profileImg = gender.getDefaultImagePath();
            log.info("Asignando imagen por defecto para gender: {}", gender.getDisplayName());
        }

        Staff staff = Staff.builder()
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

        staff = staffRepository.save(staff);

        log.info("Usuario registrado: {}", staff.getEmail());

        try {
            emailService.sendVerificationEmail(
                    staff.getEmail(),
                    staff.getName(),
                    staff.getVerificationToken()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de verificacion a {}: {}", staff.getEmail(), e.getMessage());
            // No interrumpir el registro si falla el proveedor de correo.
        }

        return staff;
    }

    @Transactional
    public Staff verifyEmail(String token) {
        Staff staff = staffRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException(ApiConstants.ERR_INVALID_VERIFICATION_TOKEN));

        if (staff.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(ApiConstants.ERR_TOKEN_EXPIRED);
        }

        staff.setEmailVerified(true);
        staff.setActive(true);
        staff.setVerificationToken(null);
        staff.setVerificationTokenExpiry(null);
        staff = staffRepository.save(staff);

        log.info("Email verificado para usuario: {}", staff.getEmail());

        // Enviar email de bienvenida
        emailService.sendWelcomeEmail(staff.getEmail(), staff.getName());

        return staff;
    }

    public Optional<Staff> getStaffByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    public Optional<Staff> getStaffByNick(String nick) {
        return staffRepository.findByNick(nick);
    }

    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
    }

    public Staff getRequiredStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ApiConstants.ERR_STAFF_NOT_FOUND));
    }

    public Staff getRequiredStaffByEmail(String email) {
        return staffRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ApiConstants.ERR_STAFF_NOT_FOUND));
    }

    public Staff authenticate(String email, String password) {
        Staff staff = getRequiredStaffByEmail(email);
        validateStaffIsActive(staff);
        if (!passwordEncoder.matches(password, staff.getPassword())) {
                throw new IllegalArgumentException(ApiConstants.ERR_INVALID_CREDENTIALS);
        }
        return staff;
    }

    public boolean validateCredentials(String email, String password) {
        return staffRepository.findByEmail(email)
                .filter(staff -> Boolean.TRUE.equals(staff.getActive()) && Boolean.TRUE.equals(staff.getEmailVerified()))
                .map(staff -> passwordEncoder.matches(password, staff.getPassword()))
                .orElse(false);
    }

    public boolean isPasswordValid(Staff staff, String password) {
        return passwordEncoder.matches(password, staff.getPassword());
    }

    @Transactional
    public Staff updateStaffProfile(Long id, String name, String surname1, String surname2, String phone) {
        Staff staff = getRequiredStaffById(id);
        staff.setName(name);
        staff.setSurname1(surname1);
        staff.setSurname2(surname2);
        staff.setPhone(phone);

        return staffRepository.save(staff);
    }

    @Transactional
    public Staff updateStaffProfileAsAdmin(Long id, UpdateProfileRequest request) {
        Staff staff = getRequiredStaffById(id);
        if (Role.ADMIN.equals(staff.getRole())) {
            throw new IllegalStateException("No se puede actualizar usuarios con rol ADMIN");
        }
        staff.setName(request.getName());
        staff.setSurname1(request.getSurname1());
        staff.setSurname2(request.getSurname2());
        staff.setPhone(request.getPhone());
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff updateProfileImage(Long staffId, String imagePath) {
        Staff staff = getRequiredStaffById(staffId);
        if (staff.getProfileImg() != null
                && !imageService.isProtectedImage(staff.getProfileImg())
                && !staff.getProfileImg().equals(imagePath)) {
            try {
                imageService.deleteImage(staff.getProfileImg());
            } catch (Exception e) {
                log.warn("No se pudo eliminar imagen anterior de usuario {}: {}", staffId, e.getMessage());
            }
        }

        staff.setProfileImg(imagePath);
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff changePassword(Long id, String oldPassword, String newPassword) {
        Staff staff = getRequiredStaffById(id);
        if (!passwordEncoder.matches(oldPassword, staff.getPassword())) {
                throw new IllegalArgumentException(ApiConstants.ERR_INVALID_PASSWORD);
        }

        if (passwordEncoder.matches(newPassword, staff.getPassword())) {
                throw new IllegalArgumentException(ApiConstants.ERR_NEW_PASSWORD_SAME);
        }

        staff.setPassword(passwordEncoder.encode(newPassword));
        staff = staffRepository.save(staff);

        log.info("Contraseña cambiada para usuario: {}", staff.getEmail());
        return staff;
    }

    @Transactional
    public void deleteStaffAccount(Long id, String password) {
        Staff staff = getRequiredStaffById(id);
                if (!passwordEncoder.matches(password, staff.getPassword())) {
                throw new IllegalArgumentException(ApiConstants.ERR_INVALID_PASSWORD);
        }

        deleteStaffAssets(staff);
        staffRepository.delete(staff);
        log.info("Cuenta eliminada permanentemente: {}", staff.getEmail());
    }

    @Transactional
    public void deleteStaff(Long id) {
        Staff staff = getRequiredStaffById(id);
        if (staff.getRole() == Role.ADMIN) {
            throw new IllegalStateException("No se puede eliminar usuarios con rol ADMIN");
        }

        deleteStaffAssets(staff);
        staffRepository.delete(staff);
    }

    @Transactional
    public Staff changeStaffRole(Long staffId, String roleName) {
        Staff staff = getRequiredStaffById(staffId);
        Role role = Role.fromDisplayName(roleName);
        staff.getRoles().clear();
        staff.getRoles().add(getRequiredRole(role));
        return staffRepository.save(staff);
    }

    public Page<Staff> getStaffExcludingRole(Role role, Pageable pageable) {
        return staffRepository.findAllExcludingRoleName(role.name(), pageable);
    }

    public Page<Staff> getStaffExcludingRoleBySurname(Role role, String surname, Pageable pageable) {
        if (surname == null || surname.isBlank()) {
            return getStaffExcludingRole(role, pageable);
        }
        return staffRepository.findAllExcludingRoleNameBySurname(role.name(), surname.trim(), pageable);
    }

    private void validateStaffIsActive(Staff staff) {
        if (!Boolean.TRUE.equals(staff.getEmailVerified())) {
            throw new IllegalStateException(ApiConstants.ERR_EMAIL_NOT_VERIFIED);
        }
        if (!Boolean.TRUE.equals(staff.getActive())) {
            throw new IllegalStateException(ApiConstants.ERR_ACCOUNT_INACTIVE);
        }
    }

    private com.asociaciondomitila.projects.entity.Role getRequiredRole(Role role) {
        return roleRepository.findByName(role.name())
                .orElseThrow(() -> new IllegalStateException(ApiConstants.ERR_ROLE_NOT_FOUND + ": " + role.name()));
    }

    private void deleteStaffAssets(Staff staff) {
        if (staff.getProfileImg() == null || staff.getProfileImg().isEmpty()) {
            return;
        }

        try {
            imageService.deleteProfileImage(staff.getId(), staff.getProfileImg());
        } catch (Exception e) {
            log.warn("Error al eliminar imágenes de perfil del usuario {}: {}", staff.getId(), e.getMessage());
        }
    }
}
