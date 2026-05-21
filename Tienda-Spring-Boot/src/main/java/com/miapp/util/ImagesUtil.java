package com.miapp.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ImagesUtil {

    // Ruta base de las imágenes en el servidor web
    private static final String IMAGES_PATH = "/imgs/";
    
    // Extensiones permitidas
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    
    // Tamaño máximo de archivo en bytes (20MB)
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    public static Path getUploadDirPath() {
        String configured = Optional.ofNullable(System.getProperty("tienda.images.dir"))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(System.getenv("TIENDA_IMAGES_DIR"))
                        .filter(s -> !s.isBlank())
                        .orElseGet(() -> {
                            String catalinaBase = System.getProperty("catalina.base");
                            if (catalinaBase != null && !catalinaBase.isBlank()) {
                                return Paths.get(catalinaBase, "tienda-api", "imgs").toString();
                            }
                            return Paths.get(System.getProperty("user.home"), "tienda-api", "imgs").toString();
                        }));

        return Paths.get(configured);
    }

    /**
     * Obtiene la URL completa de una imagen a partir de su nombre
     *
     * @param nombreImagen Nombre del archivo de imagen (ej: "combo.webp")
     * @return URL completa de la imagen
     */
    public static String getImagenURL(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) {
            return getImagenURL("placeholder.webp");
        }
        return IMAGES_PATH + nombreImagen;
    }

    /**
     * Obtiene la ruta relativa para usar en JSP con pageContext.request.contextPath
     *
     * @param contextPath El contextPath de la aplicación
     * @param nombreImagen Nombre del archivo de imagen
     * @return Ruta completa con contextPath
     */
    public static String getImagenURLConContextPath(String contextPath, String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) {
            nombreImagen = "placeholder.webp";
        }
        return contextPath + "/imgs/" + nombreImagen;
    }

    /**
     * Valida que el nombre de la imagen tenga una extensión válida
     *
     * @param nombreImagen Nombre del archivo a validar
     * @return true si la extensión es válida
     */
    public static boolean esImagenValida(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty()) {
            return false;
        }
        String extension = getExtension(nombreImagen).toLowerCase();
        return java.util.Arrays.stream(ALLOWED_EXTENSIONS)
                .anyMatch(ext -> ext.equals(extension));
    }
    
    /**
     * Guarda un archivo de imagen en el servidor manteniendo el nombre original
     *
     * @param file El archivo MultipartFile a guardar
     * @return El nombre del archivo guardado (nombre original + extensión)
     * @throws IOException Si ocurre un error al guardar
     * @throws IllegalArgumentException Si el archivo no es válido
     */
    public static String guardarImagen(MultipartFile file) throws IOException {
        // Validar que el archivo no sea vacío
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        }
        
        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (20MB)");
        }
        
        // Obtener nombre original y extensión
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo es inválido");
        }
        
        String extension = getExtension(originalFilename).toLowerCase();
        
        // Validar extensión
        if (!esImagenValida(originalFilename)) {
            throw new IllegalArgumentException("Extensión de archivo no permitida. Extensiones válidas: jpg, jpeg, png, gif, webp");
        }
        
        Path uploadDirPath = getUploadDirPath();
        File uploadDir = uploadDirPath.toFile();
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // Sanitizar nombre: eliminar caracteres especiales y espacios
        String nombreSanitizado = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Eliminar la extensión del nombre sanitizado si la tiene
        if (nombreSanitizado.contains(".")) {
            nombreSanitizado = nombreSanitizado.substring(0, nombreSanitizado.lastIndexOf("."));
        }
        
        // Usar el nombre original sanitizado con la extensión
        String nombreArchivo = nombreSanitizado + "." + extension;
        Path filepath = uploadDirPath.resolve(nombreArchivo);
        
        // Si el archivo ya existe, agregar un contador
        int contador = 1;
        while (Files.exists(filepath)) {
            nombreArchivo = nombreSanitizado + "_" + contador + "." + extension;
            filepath = uploadDirPath.resolve(nombreArchivo);
            contador++;
        }
        
        // Guardar archivo
        Files.write(filepath, file.getBytes());
        
        return nombreArchivo;
    }
    
    /**
     * Obtiene la extensión de un archivo
     *
     * @param nombreArchivo Nombre del archivo
     * @return La extensión sin el punto
     */
    private static String getExtension(String nombreArchivo) {
        int lastDot = nombreArchivo.lastIndexOf('.');
        if (lastDot > 0) {
            return nombreArchivo.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * Elimina una imagen del servidor
     *
     * @param nombreImagen Nombre del archivo a eliminar
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    public static boolean eliminarImagen(String nombreImagen) {
        if (nombreImagen == null || nombreImagen.isEmpty() || nombreImagen.equals("placeholder.webp")) {
            return false;
        }
        try {
            Path filepath = getUploadDirPath().resolve(nombreImagen);
            Files.deleteIfExists(filepath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
