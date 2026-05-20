package com.miapp.config;

import com.miapp.util.ImagesUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;

/**
 * Configuración de recursos web para servir imágenes desde la carpeta externa
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtener la ruta del directorio de imágenes
        Path imagesPath = ImagesUtil.getUploadDirPath();
        
        // Construir la URL de archivo correctamente para Windows y Unix
        String absolutePath = imagesPath.toAbsolutePath().toString();
        String fileLocation = "file:///" + absolutePath.replace("\\", "/");
        
        // Mapear /imgs/** a la carpeta de imágenes externas
        registry.addResourceHandler("/imgs/**")
                .addResourceLocations(fileLocation + "/")
                .setCachePeriod(0)  // Sin caché para ver cambios inmediatos
                .resourceChain(true);
    }
}
