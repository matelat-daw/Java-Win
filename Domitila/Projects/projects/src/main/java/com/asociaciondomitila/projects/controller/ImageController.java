package com.asociaciondomitila.projects.controller;

import com.asociaciondomitila.projects.util.ApiResponse;
import com.asociaciondomitila.projects.util.ApiResponseBuilder;
import com.asociaciondomitila.projects.util.ApiConstants;
import jakarta.servlet.http.HttpServletRequest;
import com.asociaciondomitila.projects.service.ImageService;
import com.asociaciondomitila.projects.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     * Obtiene una imagen por nombre (soporta subcarpetas de usuario como {staffId}/profile.jpg)
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
            return ApiResponseBuilder.badRequest(ApiConstants.ERR_INVALID_IMAGE_PATH);
        }

        log.info("Solicitud de imagen procesada. Archivo buscado: '{}'", fileName);
        try {
            // Buscar en el directorio de uploads configurado
            // Todas las imágenes (de usuario y por defecto) están centralizadas ahí
            String uploadDirProperty = imageService.getUploadDir();
            Path uploadPath = Paths.get(uploadDirProperty).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();

            if (!filePath.startsWith(uploadPath)) {
                return ApiResponseBuilder.badRequest(ApiConstants.ERR_INVALID_IMAGE_PATH);
            }

            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return serveResource(resource, filePath);
                }
            }

            return ApiResponseBuilder.notFound("Imagen no encontrada");

        } catch (Exception e) {
            log.error("Error al obtener imagen: {}", fileName, e);
            return ApiResponseBuilder.internalServerError("Error al obtener la imagen");
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
    public ResponseEntity<ApiResponse<ImageHealthResponse>> health() {
        return ApiResponseBuilder.success(
                "Servicio de imágenes operativo",
                new ImageHealthResponse("UP", imageService.getUploadDir())
        );
    }

    private record ImageHealthResponse(String status, String uploadDir) {
    }
}
