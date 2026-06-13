package com.asociaciondomitila.projects.util;

import com.asociaciondomitila.projects.util.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utilidad de validación centralizada
 * Sigue el principio DRY (Don't Repeat Yourself)
 */
@Slf4j
public final class ValidationHelper {

    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024; // 20MB

    private ValidationHelper() {
        throw new AssertionError("No se puede instanciar esta clase");
    }

    /**
     * Valida que un archivo de imagen sea válido
     */
    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Se intentó subir una imagen vacía");
            throw new IllegalArgumentException(ApiConstants.ERR_NO_IMAGE);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            log.warn("El archivo excede el tamaño máximo permitido: {}MB", MAX_IMAGE_SIZE / (1024 * 1024));
            throw new IllegalArgumentException(ApiConstants.ERR_IMAGE_SIZE_INVALID + " " + MAX_IMAGE_SIZE / (1024 * 1024));
        }

        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            log.warn("Tipo de imagen no válido: {}", contentType);
            throw new IllegalArgumentException(ApiConstants.ERR_IMAGE_TYPE_INVALID);
        }
    }

    /**
     * Valida que el tipo de contenido sea una imagen válida
     */
    public static boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/gif")
                || contentType.equals("image/webp");
    }

    /**
     * Valida que un string sea válido (no null ni vacío)
     */
    public static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Valida que una ruta de archivo sea segura (sin path traversal)
     */
    public static boolean isValidFilePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        return !path.contains("..") && !path.contains("//") && path.matches("^[a-zA-Z0-9/_.-]+$");
    }

    /**
     * Extrae la extensión de un archivo
     */
    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Valida email básico
     */
    public static boolean isValidEmail(String email) {
        return isValidString(email) && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Valida que una contraseña sea segura (mínimo 8 caracteres)
     */
    public static boolean isValidPassword(String password) {
        return isValidString(password) && password.length() >= 8;
    }
}