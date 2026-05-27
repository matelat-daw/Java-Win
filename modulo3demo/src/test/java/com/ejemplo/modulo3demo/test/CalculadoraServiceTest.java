package com.ejemplo.modulo3demo.test;

import org.junit.jupiter.api.Test;
import com.ejemplo.modulo3demo.service.CalculadoraService;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraServiceTest {
    @Test
    void calcularTotalConIva_debeSumarEl21PorCiento() {
        CalculadoraService servicio = new CalculadoraService();
        double resultado = servicio.calcularTotalConIva(100);
        assertEquals(121.0, resultado, 0.0001);
    }
}
