package com.ejemplo.modulo3demo.test;

import org.junit.jupiter.api.Test;
import com.ejemplo.modulo3demo.service.CalculadoraService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraServiceTest {
    @Test
    void calcularTotalConIva_debeSumarEl21PorCiento() {
        // Arrange: preparo el escenario
        CalculadoraService servicio = new CalculadoraService();
        // Act: ejecuto el método que quiero probar
        double resultado = servicio.calcularTotalConIva(100);
        // Assert: compruebo el resultado esperado
        assertEquals(121, resultado);
    }
}