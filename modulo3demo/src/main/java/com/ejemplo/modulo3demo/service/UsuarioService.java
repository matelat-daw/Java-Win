package com.ejemplo.modulo3demo.service;

import com.ejemplo.modulo3demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String saludarUsuario(Long id) {
        String nombre = usuarioRepository.buscarNombrePorId(id);
        return "Hola, " + nombre;
    }
}