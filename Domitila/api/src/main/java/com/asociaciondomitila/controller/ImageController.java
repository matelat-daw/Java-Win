package com.asociaciondomitila.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.asociaciondomitila.service.ImageService;
import com.asociaciondomitila.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Controlador para servir imágenes subidas por usuarios
 * Maneja la descarga de imágenes de perfil y otros archivos
 */
@RestController
@RequestMapping("/api/images")
@Slf4j
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Obtiene una imagen por nombre (soporta subcarpetas de usuario como {userId}/profile.jpg)
     * GET /api/images/**
     */
    @GetMapping("/**")
    public ResponseEntity<?> getImage(HttpServletRequest request) {
        String pathWithinMapping = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        String fileName = pathMatcher.extractPathWithinPattern(bestMatchPattern, pathWithinMapping);
        if (fileName == null) {
            fileName = "";
        }

        fileName = fileName.replace("\\", "/").replaceAll("/{2,}", "/");

        if (fileName.isBlank() || fileName.equals("health")) {
            return health();
        }

        if (!ValidationHelper.isValidFilePath(fileName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", false,
                            "message", "Ruta de imagen inválida"
                    ));
        }

        log.info("Solicitud de imagen procesada. Archivo buscado: '{}'", fileName);
        try {
            // Buscar en el directorio de uploads configurado
            // Todas las imágenes (de usuario y por defecto) están centralizadas ahí
            String uploadDirProperty = imageService.getUploadDir();
            Path uploadPath = Paths.get(uploadDirProperty).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();

            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "success", false,
                                "message", "Ruta de imagen inválida"
                        ));
            }

            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return serveResource(resource, filePath);
                }
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", false,
                            "message", "Imagen no encontrada"
                    ));

        } catch (Exception e) {
            log.error("Error al obtener imagen: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "success", false,
                            "message", "Error al obtener la imagen"
                    ));
        }
    }

    private ResponseEntity<Resource> serveResource(Resource resource, Path path) throws Exception {
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Health check para servicio de imágenes
     * GET /api/images/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "status", "Image service is running",
                        "upload_dir", imageService.getUploadDir()
                ));
    }
}
