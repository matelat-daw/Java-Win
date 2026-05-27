package com.ejemplo.modulo3demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
// ⚠️ IMPORT MANDATORIO: Sin esta línea, el compilador de Java 25 aborta a los 12 segundos
import com.ejemplo.modulo3demo.Modulo3demoApplication; 

@SpringBootTest(classes = Modulo3demoApplication.class) 
class Modulo3demoApplicationTests {
    
    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Security y controladores levante limpio
    }
}
