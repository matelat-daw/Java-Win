package com.asociaciondomitila.projects.config;

import com.asociaciondomitila.projects.repository.StaffRepository;
import com.asociaciondomitila.projects.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseStaffDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return staffRepository.findByEmail(username)
                .map(staff -> {
                    List<GrantedAuthority> authorities = staff.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(ApiConstants.ROLE_PREFIX + role.getName()))
                            .map(GrantedAuthority.class::cast)
                            .toList();

                    return org.springframework.security.core.userdetails.User.builder()
                            .username(staff.getEmail())
                            .password(staff.getPassword())
                            .authorities(authorities)
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(!Boolean.TRUE.equals(staff.getActive()))
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario de email no encontrado: " + username));
    }
}
