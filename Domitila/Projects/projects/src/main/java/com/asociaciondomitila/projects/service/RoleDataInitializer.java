package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.enums.Role;
import com.asociaciondomitila.projects.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleDataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        //If not, use Role.valueOf(role.name()) instead of role.getDescription()
        for (Role role : Role.values()) {
            roleRepository.findByName(role.name())
                    .orElseGet(() -> {
                        log.info("Creando rol base '{}'", role.name());
                        return roleRepository.save(com.asociaciondomitila.projects.entity.Role.builder()
                                .name(role.name())
                                .description(role.getDescription())
                                .build());
                    });
        }
    }
}
