package com.asociaciondomitila.service;

import com.asociaciondomitila.entity.RoleEntity;
import com.asociaciondomitila.enums.Role;
import com.asociaciondomitila.repository.RoleRepository;
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
        for (Role role : Role.values()) {
            roleRepository.findByName(role.name())
                    .orElseGet(() -> {
                        log.info("Creando rol base '{}'", role.name());
                        return roleRepository.save(RoleEntity.builder()
                                .name(role.name())
                                .description(role.getDescription())
                                .build());
                    });
        }
    }
}
