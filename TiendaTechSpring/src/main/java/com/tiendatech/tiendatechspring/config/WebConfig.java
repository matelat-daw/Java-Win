package com.tiendatech.tiendatechspring.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    // Extrae los orígenes desde application.properties (ej. app.cors.allowed-origins=http://localhost:3000)
    // Si no está definido, por defecto permite patrones de localhost en desarrollo
    @Value("${app.cors.allowed-origins:http://localhost:[*],http://127.0.0.1:[*]}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Asigna los patrones de origen de forma dinámica
        configuration.setAllowedOriginPatterns(allowedOrigins);
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Cabeceras permitidas
        configuration.setAllowedHeaders(List.of("*"));
        
        // Credenciales desactivadas (si necesitas JWT en cookies o sesiones, cámbialo a true)
        configuration.setAllowCredentials(false);
        
        // Tiempo en segundos que el navegador recordará esta respuesta CORS (mejora rendimiento)
        configuration.setMaxAge(3600L); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return new CorsFilter(source);
    }
}