package com.miapp.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class ImagesUtil {
    private static final String IMAGES_PATH = "/api/imgs/";
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    private static final long MAX_FILE_SIZE = resolveMaxFileSizeBytes();

    public static Path getUploadDirPath() {
        String configured = Optional.ofNullable(System.getProperty("tienda.images.dir"))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(System.getenv("TIENDA_IMAGES_DIR"))
                        .filter(s -> !s.isBlank())
                        .orElseGet(() -> Paths.get(System.getProperty("user.home"), "tienda-api", "imgs").toString()));

        return Paths.get(configured);
    }

    public static Path resolveImagePath(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }
        if (!filename.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }

        Path base = getUploadDirPath().toAbsolutePath().normalize();
        Path resolved = base.resolve(filename).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }
        return resolved;
    }

    public static String getImagenURL(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) {
            return getImagenURL("placeholder.webp");
        }
        return IMAGES_PATH + nombreImagen;
    }

    public static boolean esImagenValida(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) {
            return false;
        }
        String extension = getExtension(nombreImagen).toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public static String guardarImagen(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido");
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) throw new IllegalArgumentException("El nombre del archivo es inválido");

        String extension = getExtension(originalFilename).toLowerCase();
        if (!esImagenValida(originalFilename)) throw new IllegalArgumentException("Extensión de archivo no permitida.");

        Path uploadDirPath = getUploadDirPath();
        Files.createDirectories(uploadDirPath);

        String nombreSanitizado = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (nombreSanitizado.contains(".")) {
            nombreSanitizado = nombreSanitizado.substring(0, nombreSanitizado.lastIndexOf("."));
        }
        nombreSanitizado = nombreSanitizado.trim();
        if (nombreSanitizado.isEmpty()) throw new IllegalArgumentException("El nombre del archivo es inválido");

        String nombreArchivo = nombreSanitizado + "." + extension;
        Path filepath = uploadDirPath.resolve(nombreArchivo);
        int contador = 1;
        while (Files.exists(filepath)) {
            nombreArchivo = nombreSanitizado + "_" + contador + "." + extension;
            filepath = uploadDirPath.resolve(nombreArchivo);
            contador++;
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filepath, StandardCopyOption.REPLACE_EXISTING);
        }
        return nombreArchivo;
    }

    private static String getExtension(String nombreArchivo) {
        int lastDot = nombreArchivo.lastIndexOf('.');
        if (lastDot > 0) return nombreArchivo.substring(lastDot + 1);
        return "";
    }

    private static long resolveMaxFileSizeBytes() {
        String configured = Optional.ofNullable(System.getProperty("tienda.images.maxMb"))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(System.getenv("TIENDA_IMAGES_MAX_MB"))
                        .filter(s -> !s.isBlank())
                        .orElse("20"));

        try {
            int mb = Integer.parseInt(configured.trim());
            if (mb <= 0) return 20L * 1024 * 1024;
            return (long) mb * 1024 * 1024;
        } catch (NumberFormatException e) {
            return 20L * 1024 * 1024;
        }
    }

    public static boolean eliminarImagen(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty() || nombreImagen.equals("placeholder.webp")) return false;
        try {
            Path filepath = getUploadDirPath().resolve(nombreImagen);
            Files.deleteIfExists(filepath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}