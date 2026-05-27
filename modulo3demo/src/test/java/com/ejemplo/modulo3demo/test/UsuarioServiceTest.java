package com.ejemplo.modulo3demo.test;

import com.ejemplo.modulo3demo.repository.UsuarioRepository;
import com.ejemplo.modulo3demo.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void saludarUsuario_debeUsarElNombreDelRepositorio() {
        when(usuarioRepository.buscarNombrePorId(1L)).thenReturn("Ana");
        String resultado = usuarioService.saludarUsuario(1L);
        assertEquals("Hola, Ana", resultado);
        verify(usuarioRepository).buscarNombrePorId(1L);
    }
}
