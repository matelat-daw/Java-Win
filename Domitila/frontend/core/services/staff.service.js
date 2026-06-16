/**
 * staff.service.js - Servicio para gestionar usuarios
 */

class StaffService {
    /**
     * Registra un nuevo usuario
     * @param {Staff} staff - Objeto usuario
     * @param {File} profilePicture - Archivo de imagen de perfil (opcional)
     * @returns {Promise<Object>}
     */
    static async register(staff, profilePicture = null) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.REGISTER;
            
            // Crear FormData para enviar datos + archivo
            const formData = new FormData();
            
            // Agregar todos los campos del usuario
            formData.append('nick', staff.nick);
            formData.append('name', staff.name);
            formData.append('surname1', staff.surname1);
            if (staff.surname2) formData.append('surname2', staff.surname2);
            formData.append('email', staff.email);
            formData.append('phone', staff.phone);
            formData.append('password', staff.password);
            formData.append('gender', staff.gender);
            if (staff.bday) formData.append('bday', staff.bday);
            
            // Agregar archivo de imagen si existe
            if (profilePicture) {
                formData.append('profilePicture', profilePicture, profilePicture.name);
            }
            // Enviar como FormData
            const response = await Utils.makeRequestWithFormData('POST', url, formData);
            return response;
        } catch (error) {
            throw error;
        }
    }

    /**
     * Obtiene todos los usuarios con paginación
     * @param {number} page - Número de página (default 0)
     * @param {number} size - Tamaño de página (default 8)
     * @param {{surname?: string, sortBy?: string, sortDir?: 'asc'|'desc'}} options
     * @returns {Promise<Object>}
     */
    static async getStaff(page = 0, size = 8, options = {}) {
        try {
            const params = new URLSearchParams();
            params.set('page', String(page));
            params.set('size', String(size));

            const surname = typeof options?.surname === 'string' ? options.surname.trim() : '';
            if (surname) {
                params.set('surname', surname);
            }

            const sortBy = typeof options?.sortBy === 'string' ? options.sortBy.trim() : '';
            if (sortBy) {
                params.set('sortBy', sortBy);
            }

            const sortDir = (options?.sortDir === 'desc') ? 'desc' : 'asc';
            params.set('sortDir', sortDir);

            const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.STAFF}?${params.toString()}`;
            // Usar cache para la lista de staff (útil si se navega entre detalles y lista)
            const response = await Utils.makeRequest('GET', url, null, true);
            return response;
        } catch (error) {
            throw error;
        }
    }

    /**
     * Obtiene un usuario por ID
     * @param {number} id - ID del usuario
     * @returns {Promise<Object>}
     */
    static async getStaffById(id) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.STAFF_BY_ID.replace(':id', id);
            // Usar cache para detalles de usuario
            const response = await Utils.makeRequest('GET', url, null, true);
            return response;
        } catch (error) {
            throw error;
        }
    }

    /**
     * Actualiza un usuario
     * @param {number} id - ID del usuario
     * @param {Staff} staff - Objeto usuario con datos actualizados
     * @returns {Promise<Object>}
     */
    static async updateStaff(id, staff) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.UPDATE_STAFF.replace(':id', id);
            const body = {
                name: staff?.name ?? '',
                surname1: staff?.surname1 ?? '',
                surname2: staff?.surname2 ?? '',
                phone: staff?.phone ?? ''
            };
            const response = await Utils.makeRequest('PUT', url, body);
            
            // Limpiar cache tras actualizar
            Utils.clearCache();
            
            return response;
        } catch (error) {
            throw error;
        }
    }

    /**
     * Elimina un usuario
     * @param {number} id - ID del usuario
     * @returns {Promise<Object>}
     */
    static async deleteStaff(id) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.DELETE_STAFF.replace(':id', id);
            const response = await Utils.makeRequest('DELETE', url);
            
            // Limpiar cache tras eliminar
            Utils.clearCache();
            
            return response;
        } catch (error) {
            throw error;
        }
    }
}

// Exponer globalmente para la verificación de carga
window.StaffService = StaffService;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('staff.service');

