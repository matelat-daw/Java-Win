package com.asociaciondomitila.projects.service;

import com.asociaciondomitila.projects.enums.Gender;
import com.asociaciondomitila.projects.util.ValidationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import com.asociaciondomitila.projects.util.ApiConstants;

@Service
@Slf4j
public class ImageService {


    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public String getUploadDir() {
        // The uploadDir property from application.properties
        return uploadDir;
    }

    /**
     * Resuelve el directorio base real donde se guardan las imágenes.
     * Si la ruta configurada es relativa, se ancla al directorio de ejecución.
     */
    public Path resolveUploadBasePath() {
        Path basePath = Paths.get(uploadDir);
        if (!basePath.isAbsolute()) {
            basePath = Paths.get(System.getProperty("user.dir")).resolve(basePath);
        }
        return basePath.normalize();
    }

    private Path resolvePathUnderUploadBase(String relativePath) {
        log.warn("Intentando acceder a una ruta de archivo inválida: {}", relativePath);
        if (!ValidationHelper.isValidFilePath(relativePath)) {
            throw new IllegalArgumentException(ApiConstants.ERR_INVALID_FILE_PATH);
        }

        Path uploadPath = resolveUploadBasePath();
        Path resolved = uploadPath.resolve(relativePath).normalize();
        if (!resolved.startsWith(uploadPath)) {
            throw new IllegalArgumentException(ApiConstants.ERR_INVALID_FILE_PATH);
        }
        log.info("Ruta resuelta: {}", resolved);
        return resolved;
    }

    /**
     * Asegura que exista la carpeta base y la carpeta del usuario.
     */
    public Path ensureStaffImageDirectory(Long staffId) throws IOException {
        Path uploadPath = resolveUploadBasePath();
        Files.createDirectories(uploadPath);

        Path staffPath = uploadPath.resolve(String.valueOf(staffId));
        Files.createDirectories(staffPath);

        return staffPath;
    }

    /**
     * Verifica si una imagen es protegida (por defecto según género)
     */
    public boolean isProtectedImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        return Gender.isDefaultImagePath(imagePath);
    }

    /**
     * Obtiene la ruta de imagen por defecto para un género
     */
    public String getDefaultImagePath(String genderDisplayName) {
        try {
            Gender gender = Gender.fromDisplayName(genderDisplayName);
            return gender.getDefaultImagePath();
        } catch (IllegalArgumentException e) {
            log.error("Gender no válido: {}", genderDisplayName);
            throw new RuntimeException(ApiConstants.ERR_INVALID_GENDER);
        }
    }

    /**
     * Elimina una imagen (solo si no es protegida)
     */
    public boolean deleteImage(String imagePath) {
        // Validar que no es imagen protegida
        if (isProtectedImage(imagePath)) {
            log.warn("Intento de eliminar imagen protegida: {}", imagePath);
            throw new RuntimeException(ApiConstants.ERR_PROTECTED_IMAGE);
        }

        try {
            Path path = resolvePathUnderUploadBase(imagePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Imagen eliminada: {}", imagePath);
                return true;
            }
            log.warn("Imagen no encontrada: {}", imagePath);
            return false;
        } catch (Exception e) {
            log.error("Error al eliminar imagen: {}", imagePath, e);
            throw new RuntimeException(ApiConstants.ERR_INTERNAL_ERROR);
        }
    }

    /**
     * Verifica si un archivo de imagen existe
     */
    public boolean imageExists(String imagePath) {
        try {
            Path path = resolvePathUnderUploadBase(imagePath);
            return Files.exists(path);
        } catch (Exception e) {
            log.error("Error al verificar imagen: {}", imagePath, e);
            throw new RuntimeException(ApiConstants.ERR_INTERNAL_ERROR);
        }
    }

    /**
     * Valida si una ruta es válida (no contiene caracteres peligrosos)
     */
    public boolean isValidImagePath(String imagePath) {
        return ValidationHelper.isValidFilePath(imagePath);
    }

    /**
     * Obtiene el nombre de archivo de una ruta
     */
    public String getImageFileName(String imagePath) {
        if (imagePath == null) {
            return null;
        }
        return imagePath.substring(imagePath.lastIndexOf("/") + 1);
    }

    /**
     * Genera una ruta segura para una imagen
     */
    public String generateSecureImagePath(String fileName, Long staffId) {
        // Validar nombre de archivo
        log.warn("Intentando generar ruta segura para archivo: {}", fileName);
        if (!fileName.matches("^[a-zA-Z0-9._-]+$")) {
            log.warn("Nombre de archivo inválido: {}", fileName);
            throw new RuntimeException(ApiConstants.ERR_INVALID_FILE_NAME);
        }

        // Crear carpeta por usuario
        String staffImageDir = String.format("%d/", staffId);
        return staffImageDir + System.currentTimeMillis() + "_" + fileName;
    }

    /**
     * Guarda una imagen de perfil de usuario con nombre fijo "profile"
     */
    public String saveProfileImage(MultipartFile file, Long staffId) throws Exception {
        try {
            ValidationHelper.validateImageFile(file);
            } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación de imagen para usuario {}: {}", staffId, ApiConstants.ERR_INVALID_IMAGE_FILE);
            throw e;
        }
        String extension = resolveOriginalExtension(file);

        try {
            // Verificar y crear el directorio padre y la carpeta del usuario si no existen
            Path staffPath = ensureStaffImageDirectory(staffId);
            log.info("📁 Carpeta lista para usuario ID {}: {}", staffId, staffPath.toAbsolutePath());

            // El nombre siempre será "profile" con su extensión original
            String fileName = "profile." + extension;
            Path filePath = staffPath.resolve(fileName);

            // Guardar archivo (sobrescribirá si ya existe)
            log.info("📝 Guardando archivo en: {}", filePath.toAbsolutePath());
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("✅ Archivo guardado correctamente. Tamaño: {} bytes", file.getSize());
            
            // La ruta que guardamos en BD será relativa a uploadDir: "ID/profile.ext"
            String dbPath = staffId + "/" + fileName;
            log.info("📸 Imagen de perfil guardada para usuario {}: {} (Ruta absoluta: {})", staffId, dbPath, filePath.toAbsolutePath());
            
            return dbPath;
        } catch (Exception e) {
            log.error("❌ Error al guardar imagen para usuario {}: {} | Directorio configurado: {} | Causa: {}", 
                    staffId, e.getMessage(), resolveUploadBasePath(), e.getClass().getSimpleName(), e);
            throw new RuntimeException(ApiConstants.ERR_INTERNAL_ERROR);
        }
    }

    private static String resolveOriginalExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = ValidationHelper.getFileExtension(originalFilename);
        if (ext == null) {
            ext = "jpg";
        }
        ext = ext.toLowerCase();

        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "webp" -> ext;
            default -> {
                String contentType = file.getContentType();
                if ("image/png".equals(contentType)) {
                    yield "png";
                }
                if ("image/gif".equals(contentType)) {
                    yield "gif";
                }
                if ("image/webp".equals(contentType)) {
                    yield "webp";
                }
                yield "jpg";
            }
        };
    }

    /**
     * Guarda una imagen de perfil de usuario (MÉTODO ANTIGUO - DEPRECADO)
     */
    @Deprecated
    public String saveProfileImage(MultipartFile file) throws Exception {
        long generatedUserId = System.currentTimeMillis() % Long.MAX_VALUE; // Generar ID temporal único
        return saveProfileImage(file, generatedUserId);
    }

    /**
     * Elimina la imagen de perfil de un usuario específico
     * - Si es imagen protegida (default), no elimina nada
     * - Si es imagen personalizada, elimina todo el directorio del usuario
     */
    public void deleteProfileImage(Long staffId, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            log.warn("Ruta de imagen vacía para usuario: {}", staffId);
            return;
        }

        // Si es imagen protegida, no hacer nada
        if (isProtectedImage(imagePath)) {
            log.info("📸 Imagen protegida (por defecto) para usuario {}. No se eliminará.", staffId);
            return;
        }

        // Eliminar toda la carpeta del usuario
        deleteStaffProfileImages(staffId);
    }

    /**
     * Elimina todos los archivos de perfil de un usuario (carpeta completa)
     * Se usa cuando se elimina la cuenta del usuario
     */
    public void deleteStaffProfileImages(Long staffId) {
        try {
            Path staffPath = resolveUploadBasePath().resolve(staffId.toString());
            
            if (!Files.exists(staffPath)) {
                log.warn("📁 Carpeta de usuario no encontrada: {}", staffPath);
                return;
            }

            // Eliminar recursivamente toda la carpeta
            Files.walk(staffPath)
                    .sorted((a, b) -> b.compareTo(a)) // Ordenar en reverso para eliminar primero archivos
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("✅ Eliminado: {}", path);
                        } catch (Exception e) {
                            log.warn("⚠️ No se pudo eliminar: {} - {}", path, e.getMessage());
                        }
                    });

            log.info("✅ Carpeta de imágenes del usuario {} eliminada completamente", staffId);
        } catch (Exception e) {
            log.error("❌ Error al eliminar carpeta de imágenes del usuario {}: {}", staffId, e.getMessage());
            // No lanzar excepción, solo logging para no interrumpir la eliminación del usuario
        }
    }
}
