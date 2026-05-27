package com.ejemplo.modulo3demo.repository;

import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    private final Map<Long, String> usuarios = Map.of(
            1L, "Ana",
            2L, "Luis",
            3L, "Marta"
    );

    @Override
    public String buscarNombrePorId(Long id) {
        return usuarios.getOrDefault(id, "Usuario desconocido");
    }
}