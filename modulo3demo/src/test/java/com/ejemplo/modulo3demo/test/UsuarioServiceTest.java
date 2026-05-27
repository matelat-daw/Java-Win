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
    
    // CORREGIDO: Añadimos 'private' para cumplir con las reglas estrictas de Java 25
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void saludarUsuario_debeUsarElNombreDelRepositorio() {
        // Arrange
        when(usuarioRepository.buscarNombrePorId(1L)).thenReturn("Ana");
        // Act
        String resultado = usuarioService.saludarUsuario(1L);
        // Assert
        assertEquals("Hola, Ana", resultado);
        verify(usuarioRepository).buscarNombrePorId(1L);
    }
}