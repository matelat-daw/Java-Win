package com.asociaciondomitila.projects.config;

import com.asociaciondomitila.projects.repository.UserRepository;
import com.asociaciondomitila.projects.util.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> {
                    List<GrantedAuthority> authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(ApiConstants.ROLE_PREFIX + role.getName()))
                            .map(GrantedAuthority.class::cast)
                            .toList();

                    // 2. CORRECCIÓN: Usamos la ruta completa de Spring Security aquí
                    return org.springframework.security.core.userdetails.User.builder()
                            .username(user.getEmail())
                            .password(user.getPassword())
                            .authorities(authorities)
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(!Boolean.TRUE.equals(user.getActive()))
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario de email no encontrado: " + username));
    }
}