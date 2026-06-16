package com.asociaciondomitila.projects.util;

/**
 * Constantes centralizadas para la API
 * Evita strings duplicados y facilita mantenimiento
 */
public final class ApiConstants {

    // ==================== MENSAJES DE ÉXITO ====================
    public static final String MSG_LOGIN_SUCCESS = "Login exitoso";
    public static final String MSG_REGISTER_SUCCESS = "Usuario registrado exitosamente. Revisa tu correo para verificar la cuenta.";
    public static final String MSG_PROFILE_UPDATED = "Perfil actualizado exitosamente";
    public static final String MSG_PASSWORD_UPDATED = "Contraseña actualizada exitosamente";
    public static final String MSG_PROFILE_PICTURE_UPDATED = "Foto de perfil actualizada exitosamente";
    public static final String MSG_PROFILE_DELETED = "Perfil eliminado exitosamente";
    public static final String MSG_EMAIL_VERIFIED = "Email verificado con éxito";
    public static final String MSG_TOKEN_REFRESHED = "Token refrescado exitosamente";
    public static final String MSG_PROFILE_FETCHED = "Perfil obtenido exitosamente";
    public static final String MSG_STAFF_FETCHED = "Lista de usuarios obtenida exitosamente";
    public static final String MSG_PASSWORD_VALIDATION = "Validación de contraseña completada";

    // ==================== MENSAJES DE ERROR ====================
    public static final String ERR_INVALID_CREDENTIALS = "Email o contraseña incorrectos";
    public static final String ERR_UNAUTHORIZED = "No autenticado";
    public static final String ERR_FORBIDDEN = "Acceso denegado";
    public static final String ERR_STAFF_NOT_FOUND = "Usuario no encontrado";
    public static final String ERR_USER_NOT_ACTIVE = "Usuario no verificado o inactivo";
    public static final String ERR_EMAIL_EXISTS = "El email ya está registrado";
    public static final String ERR_NICK_EXISTS = "El nick ya está en uso";
    public static final String ERR_INVALID_TOKEN = "Token inválido o expirado";
    public static final String ERR_VERIFICATION_FAILED = "Verificación fallida";
    public static final String ERR_NO_IMAGE = "Por favor selecciona una imagen";
    public static final String ERR_IMAGE_TYPE_INVALID = "Tipo de archivo no permitido. Solo se permiten imágenes (jpg, png, gif, webp)";
    public static final String ERR_IMAGE_SAVE_FAILED = "Error al guardar imagen";
    public static final String ERR_IMAGE_DELETE_FAILED = "Error al eliminar imagen";
    public static final String ERR_INTERNAL_ERROR = "Error interno del servidor";
    public static final String ERR_INVALID_FILE_PATH = "Ruta de archivo inválida";
    public static final String ERR_VALIDATION_ERROR = "Error de validación";
    public static final String ERR_INVALID_GENDER = "Género inválido";
    public static final String ERR_INVALID_IMAGE_PATH = "Ruta de imagen inválida";
    public static final String ERR_PASSWORD_REQUIRED = "La contraseña es requerida";
    public static final String ERR_PASSWORD_MISMATCH = "Las nuevas contraseñas no coinciden";
    public static final String ERR_PASSWORD_VALIDATION = "Validación de contraseña fallida";
    public static final String MSG_PASSWORD_VALID = "Contraseña válida";
    public static final String MSG_PASSWORD_INVALID = "Contraseña inválida";
    public static final String ERR_INVALID_PASSWORD = "Contraseña inválida";
    public static final String ERR_NEW_PASSWORD_SAME = "La nueva contraseña no puede ser igual a la actual";
    public static final String ERR_STAFF_NOT_FOUND_BY_EMAIL = "Usuario no encontrado por email";
    public static final String ERR_INVALID_DATE_FORMAT = "Formato de fecha inválido. Use YYYY-MM-DD o DD/MM/YYYY";
    public static final String ERR_INVALID_VERIFICATION_TOKEN = "Token de verificación inválido";
    public static final String ERR_TOKEN_EXPIRED = "El token de verificación ha expirado";
    public static final String ERR_EMAIL_NOT_VERIFIED = "Tu cuenta todavia no ha sido verificada. Revisa tu correo electronico y haz clic en el enlace de confirmacion antes de iniciar sesion.";
    public static final String ERR_ACCOUNT_INACTIVE = "Tu cuenta esta inactiva. Contacta con soporte para reactivarla.";
    public static final String ERR_ROLE_NOT_FOUND = "Rol no encontrado en la base de datos";

    // ==================== RUTAS Y PATHS ====================
    public static final String API_PREFIX = "/api";
    public static final String AUTH_ENDPOINT = API_PREFIX + "/auth";
    public static final String STAFF_ENDPOINT = API_PREFIX + "/staff";
    public static final String PROFILE_ENDPOINT = API_PREFIX + "/profile";
    public static final String IMAGES_ENDPOINT = API_PREFIX + "/images";

    // ==================== CONFIGURACIÓN ROLES ====================
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_PREMIUM = "PREMIUM";

    // ==================== REGEXP Y VALIDACIONES ====================
    public static final String FILENAME_PATTERN = "^[a-zA-Z0-9._-]+$";
    public static final String IMAGE_PATH_PATTERN = "^[a-zA-Z0-9/_.-]+$";

    // ==================== ERRORES HTTP ====================
    public static final String ERR_PROJECT_NOT_FOUND = "Proyecto no encontrado";
    public static final String ERR_PROJECT_NAME_REQUIRED = "Nombre del proyecto es obligatorio";
    public static final String ERR_PROJECT_NAME_MAX_LENGTH = "El nombre del proyecto no puede superar los 100 caracteres";
    public static final String ERR_PROJECT_DESCRIPTION_MAX_LENGTH = "La descripción no puede superar los 500 caracteres";
    public static final String ERR_PROJECT_START_DATE_REQUIRED = "La fecha de inicio es obligatoria";
    public static final String ERR_PROJECT_END_DATE_REQUIRED = "La fecha de fin es obligatoria";
    public static final String ERR_PROJECT_STATUS_REQUIRED = "El estado del proyecto es obligatorio";
    public static final String ERR_METHOD_NOT_ALLOWED = "Método HTTP no permitido";
    public static final String ERR_NOT_FOUND = "Recurso no encontrado";
    public static final String ERR_PROTECTED_IMAGE = "Imagen protegida, no se puede acceder";
    public static final String ERR_INVALID_FILE_NAME = "Nombre de archivo inválido";
    public static final String ERR_INVALID_IMAGE_FILE = "Archivo de imagen inválido";
    public static final String ERR_IMAGE_SIZE_INVALID = "Tamaño de imagen inválido";

    private ApiConstants() {
        throw new AssertionError("No se puede instanciar esta clase");
    }
}
