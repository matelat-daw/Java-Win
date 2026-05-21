package com.miapp.config;

import com.miapp.util.ImagesUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inicializador que copia las imágenes estáticas del classpath
 * a la carpeta de imágenes externas al iniciar la aplicación
 */
@Configuration
public class ImagesInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(ImagesInitializer.class);
    
    @Bean
    public CommandLineRunner initializeImages() {
        return args -> {
            try {
                // Imágenes estáticas que deben copiarse
                String[] staticImages = {
                    "combo.webp",
                    "cpu.webp",
                    "headset.webp",
                    "mechanic.webp",
                    "monitor.webp",
                    "mouse.webp",
                    "nvm2.webp",
                    "nvm2_1.webp",
                    "placeholder.webp"
                };
                
                Path uploadDir = ImagesUtil.getUploadDirPath();
                
                // Crear directorio si no existe
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                    logger.info("Directorio de imágenes creado: {}", uploadDir);
                }
                
                // Copiar imágenes estáticas
                java.util.Arrays.stream(staticImages).forEach(imageName -> {
                    try {
                        Resource resource = new ClassPathResource("static/imgs/" + imageName);
                        if (resource.exists()) {
                            Path targetPath = uploadDir.resolve(imageName);
                            
                            // Solo copiar si no existe
                            if (!Files.exists(targetPath)) {
                                Files.copy(
                                    resource.getInputStream(),
                                    targetPath,
                                    StandardCopyOption.REPLACE_EXISTING
                                );
                                logger.info("Imagen copiada: {} -> {}", imageName, targetPath);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("No se pudo copiar la imagen: {}", imageName, e);
                    }
                });
                
                logger.info("Inicialización de imágenes completada. Directorio: {}", uploadDir);
                
            } catch (Exception e) {
                logger.error("Error al inicializar las imágenes", e);
            }
        };
    }
}
