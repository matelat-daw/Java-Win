package com.futureprograms.clients.service;

import com.futureprograms.clients.enums.Gender;
import com.futureprograms.clients.util.ValidationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

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
    private Path resolveUploadBasePath() {
        Path basePath = Paths.get(uploadDir);
        if (!basePath.isAbsolute()) {
            basePath = Paths.get(System.getProperty("user.dir")).resolve(basePath);
        }
        return basePath.normalize();
    }

    private Path resolvePathUnderUploadBase(String relativePath) {
        if (!ValidationHelper.isValidFilePath(relativePath)) {
            throw new IllegalArgumentException("Ruta de archivo inválida");
        }

        Path uploadPath = resolveUploadBasePath();
        Path resolved = uploadPath.resolve(relativePath).normalize();
        if (!resolved.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Ruta de archivo inválida");
        }
        return resolved;
    }

    /**
     * Asegura que exista la carpeta base y la carpeta del usuario.
     */
    public Path ensureUserImageDirectory(Integer userId) throws IOException {
        Path uploadPath = resolveUploadBasePath();
        Files.createDirectories(uploadPath);

        Path userPath = uploadPath.resolve(String.valueOf(userId));
        Files.createDirectories(userPath);

        return userPath;
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
            throw new RuntimeException("Género no válido");
        }
    }

    /**
     * Elimina una imagen (solo si no es protegida)
     */
    public boolean deleteImage(String imagePath) {
        // Validar que no es imagen protegida
        if (isProtectedImage(imagePath)) {
            log.warn("Intento de eliminar imagen protegida: {}", imagePath);
            throw new RuntimeException("No se pueden eliminar imágenes por defecto");
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
            throw new RuntimeException("Error al eliminar imagen: " + e.getMessage());
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
            return false;
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
    public String generateSecureImagePath(String fileName, Integer userId) {
        // Validar nombre de archivo
        if (!fileName.matches("^[a-zA-Z0-9._-]+$")) {
            throw new RuntimeException("Nombre de archivo inválido");
        }

        // Crear carpeta por usuario
        String userImageDir = String.format("%d/", userId);
        return userImageDir + System.currentTimeMillis() + "_" + fileName;
    }

    /**
     * Guarda una imagen de perfil de usuario con nombre fijo "profile"
     */
    public String saveProfileImage(MultipartFile file, Integer userId) throws Exception {
        ValidationHelper.validateImageFile(file);
        String extension = resolveOriginalExtension(file);

        try {
            // Verificar y crear el directorio padre y la carpeta del usuario si no existen
            Path userPath = ensureUserImageDirectory(userId);
            log.info("📁 Carpeta lista para usuario ID {}: {}", userId, userPath.toAbsolutePath());

            // El nombre siempre será "profile" con su extensión original
            String fileName = "profile." + extension;
            Path filePath = userPath.resolve(fileName);

            // Guardar archivo (sobrescribirá si ya existe)
            log.info("📝 Guardando archivo en: {}", filePath.toAbsolutePath());
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("✅ Archivo guardado correctamente. Tamaño: {} bytes", file.getSize());
            
            // La ruta que guardamos en BD será relativa a uploadDir: "ID/profile.ext"
            String dbPath = userId + "/" + fileName;
            log.info("📸 Imagen de perfil guardada para usuario {}: {} (Ruta absoluta: {})", userId, dbPath, filePath.toAbsolutePath());
            
            return dbPath;
        } catch (Exception e) {
            log.error("❌ Error al guardar imagen para usuario {}: {} | Directorio configurado: {} | Causa: {}", 
                    userId, e.getMessage(), resolveUploadBasePath(), e.getClass().getSimpleName(), e);
            throw new RuntimeException("Error al guardar imagen: " + e.getMessage() + " (Verifica que el directorio " + resolveUploadBasePath() + " sea accesible)");
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
        int generatedUserId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        return saveProfileImage(file, generatedUserId);
    }

    /**
     * Elimina la imagen de perfil de un usuario específico
     * - Si es imagen protegida (default), no elimina nada
     * - Si es imagen personalizada, elimina todo el directorio del usuario
     */
    public void deleteProfileImage(Integer userId, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            log.warn("Ruta de imagen vacía para usuario: {}", userId);
            return;
        }

        // Si es imagen protegida, no hacer nada
        if (isProtectedImage(imagePath)) {
            log.info("📸 Imagen protegida (por defecto) para usuario {}. No se eliminará.", userId);
            return;
        }

        // Eliminar toda la carpeta del usuario
        deleteUserProfileImages(userId);
    }

    /**
     * Elimina todos los archivos de perfil de un usuario (carpeta completa)
     * Se usa cuando se elimina la cuenta del usuario
     */
    public void deleteUserProfileImages(Integer userId) {
        try {
            Path userPath = resolveUploadBasePath().resolve(userId.toString());
            
            if (!Files.exists(userPath)) {
                log.warn("📁 Carpeta de usuario no encontrada: {}", userPath);
                return;
            }

            // Eliminar recursivamente toda la carpeta
            Files.walk(userPath)
                    .sorted((a, b) -> b.compareTo(a)) // Ordenar en reverso para eliminar primero archivos
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("✅ Eliminado: {}", path);
                        } catch (Exception e) {
                            log.warn("⚠️ No se pudo eliminar: {} - {}", path, e.getMessage());
                        }
                    });

            log.info("✅ Carpeta de imágenes del usuario {} eliminada completamente", userId);
        } catch (Exception e) {
            log.error("❌ Error al eliminar carpeta de imágenes del usuario {}: {}", userId, e.getMessage());
            // No lanzar excepción, solo logging para no interrumpir la eliminación del usuario
        }
    }

    /**
     * Valida el tipo de contenido de la imagen
     */
}
