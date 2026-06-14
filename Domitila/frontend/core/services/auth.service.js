/**
 * auth.service.js - Servicio de autenticación
 */

class AuthService {
    /**
     * Información del usuario autenticado en sesión
     */
    static staffSession = null;
    
    /**
     * Token JWT del usuario
     */
    static jwtToken = null;

    /**
     * Promise reutilizable para hidratar la sesión desde el backend.
     */
    static bootstrapPromise = null;

    /**
     * Indica si se está cerrando sesión en este momento.
     */
    static logoutInProgress = false;

    /**
     * Login del usuario
     * @param {string} email - Email del usuario
     * @param {string} password - Contraseña del usuario
     * @returns {Promise<Object>}
     */
    static async login(email, password) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.LOGIN;
            const data = {
                email: email,
                password: password
            };
            const response = await Utils.makeRequest('POST', url, data);
            // Detectar si el login fue exitoso - flexible para diferentes formatos de API
            let isSuccess = false;
            let staffData = null;
            let token = null;

            const sessionData = response?.data && typeof response.data === 'object' ? response.data : null;
            const nestedStaff = sessionData?.staff || null;
            const nestedToken = sessionData?.accessToken || sessionData?.token || null;

            // Formato 1: response.success
            if (response.success === true) {
                isSuccess = true;
                staffData = nestedStaff || response.staff || response.data || response;
                token = response.token || nestedToken;
            }
            // Formato 2: response.code === 200 con data
            else if (response.code === 200 && (response.data || response.staff)) {
                isSuccess = true;
                staffData = nestedStaff || response.staff || response.data;
                token = response.token || nestedToken;
            }
            // Formato 3: respuesta directa con token (API .NET)
            else if (response.token) {
                isSuccess = true;
                staffData = response.data || response.staff || response;
                token = response.token;
            }
            // Formato 4: HTTP 200 sin estructura específica (asumir éxito con token)
            else if (!response.error && !response.message?.includes('failed') && !response.message?.includes('invalid')) {
                isSuccess = true;
                staffData = nestedStaff || response.data || response.staff || response;
                token = response.token || nestedToken || response.access_token;
            }

            if (isSuccess && token) {
                // Guardar datos del usuario en sessionStorage
                this.setStaffSession(staffData);
                
                // Guardar token JWT
                this.setJwtToken(token);
                return {
                    success: true,
                    message: response.message || 'Login exitoso',
                    staff: staffData,
                    token: token
                };
            } else if (isSuccess) {
                // Login exitoso sin token en body: la API puede estar operando solo con cookie
                this.setStaffSession(staffData);
                this.setJwtToken(null);
                return {
                    success: true,
                    message: response.message || 'Login exitoso',
                    staff: staffData,
                    token: null
                };
            }

            // Si llegamos aquí, algo salió mal
            const errorMsg = response.message || response.error || 'Error en login';
            return {
                success: false,
                message: errorMsg
            };
        } catch (error) {
            throw error;
        }
    }

    /**
     * Hidrata la sesión local desde la cookie del backend.
     * Permite que la app sobreviva a recargas sin depender solo de sessionStorage.
     * @returns {Promise<Object|null>}
     */
    static async bootstrapSession(force = false) {
        if (this.logoutInProgress) {
            this.clearLocalSession();
            return null;
        }

        if (!force) {
            const currentStaff = this.getStaffSession();
            if (currentStaff) {
                return currentStaff;
            }
            if (this.bootstrapPromise) {
                return this.bootstrapPromise;
            }
        }

        const profileUrl = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.PROFILE}`;

        this.bootstrapPromise = (async () => {
            try {
                const response = await fetch(profileUrl, {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json'
                    }
                });

                if (!response.ok) {
                    if (response.status === 401 || response.status === 403) {
                        this.clearLocalSession();
                        return null;
                    }
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }

                const payload = await response.json().catch(() => null);
                const staffData = payload?.data || payload?.staff || null;

                if (!staffData) {
                    this.clearLocalSession();
                    return null;
                }

                this.setStaffSession(staffData);
                return staffData;
            } catch (error) {
                // Si el backend no está disponible, mantener la mejor información local posible.
                return this.getStaffSession();
            } finally {
                this.bootstrapPromise = null;
            }
        })();

        return this.bootstrapPromise;
    }

    /**
     * Guarda los datos del usuario en sesión
     * @param {Object} staffData - Datos del usuario
     */
    static setStaffSession(staffData) {
        this.staffSession = staffData;
        sessionStorage.setItem('staff_session', JSON.stringify(staffData));
    }

    /**
     * Obtiene los datos del usuario de sesión
     * @returns {Object|null}
     */
    static getStaffSession() {
        if (!this.staffSession) {
            const stored = sessionStorage.getItem('staff_session');
            if (stored) {
                try {
                    this.staffSession = JSON.parse(stored);
                } catch (_) {
                    sessionStorage.removeItem('staff_session');
                    this.staffSession = null;
                }
            }
        }
        return this.staffSession;
    }

    /**
     * Verifica si el usuario está autenticado
     * @returns {boolean}
     */
    static isAuthenticated() {
        const staff = this.getStaffSession();
        return staff !== null && staff !== undefined;
    }

    /**
     * Indica si existe una sesión local en memoria o storage.
     * @returns {boolean}
     */
    static hasLocalSession() {
        return !!this.getStaffSession();
    }

    /**
     * Limpia el estado local de autenticación sin llamar al backend.
     */
    static clearLocalSession() {
        this.staffSession = null;
        this.jwtToken = null;
        this.bootstrapPromise = null;
        sessionStorage.removeItem('staff_session');
        sessionStorage.removeItem('jwt_token');
        localStorage.removeItem('staff_session');
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('auth_token');
    }

    /**
     * Cierra la sesión del usuario (logout)
     */
    static async logout(redirectTo = (ROUTES && ROUTES.LOGIN ? ROUTES.LOGIN : (APP_BASE_PATH + 'login'))) {
        this.logoutInProgress = true;

        try {
            // Limpiar primero el estado local para que la UI deje de intentar hidratar perfil.
            this.clearLocalSession();

            // Limpiar caché si existe
            if (Utils && typeof Utils.clearCache === 'function') {
                Utils.clearCache();
            }

            // Intentar llamar al endpoint de logout del backend para limpiar la cookie
            if (API_CONFIG && API_CONFIG.BASE_URL && API_CONFIG.ENDPOINTS.LOGOUT) {
                const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.LOGOUT;
                
                try {
                    await fetch(url, {
                        method: 'POST',
                        credentials: 'include',
                        headers: { 'Content-Type': 'application/json' }
                    });
                    console.log('🚪 Backend logout exitoso');
                } catch (e) {
                    console.warn('⚠️ Backend logout falló:', e);
                }
            }
        } finally {
            if (redirectTo) {
                window.location.href = redirectTo;
                return;
            }

            this.logoutInProgress = false;
        }
    }

    static isLogoutInProgress() {
        return this.logoutInProgress;
    }

    /**
     * Obtiene el nombre completo del usuario
     * @returns {string}
     */
    static getFullName() {
        const staff = this.getStaffSession();
        if (!staff) return '';
        
        const name = staff.name || '';
        const surname1 = staff.surname1 || '';
        const surname2 = staff.surname2 || '';
        
        return `${name} ${surname1} ${surname2}`.trim();
    }

    /**
     * Obtiene la URL de la foto de perfil del usuario
     * @returns {string|null} - La URL de la imagen o null si no existe
     */
    static getProfilePictureUrl() {
        const staff = this.getStaffSession();
        if (!staff) {
            return null;
        }
        
        // Si no tiene foto, devolver null (el frontend decide qué mostrar)
        if (!staff.profileImg || String(staff.profileImg).trim() === '') {
            return null;
        }
        
        let imgPath = String(staff.profileImg).trim();
        
        // Limpiar ruta: remover "images/" si existe (para compatibilidad)
        if (imgPath.startsWith('images/')) {
            imgPath = imgPath.replace('images/', '');
        }
        
        // Construcción de URL
        if (!API_CONFIG || !API_CONFIG.BASE_URL) {
            return null;
        }
        
        const fullUrl = `${API_CONFIG.BASE_URL}/images/${imgPath}`;
        return fullUrl;
    }

    /**
     * Obtiene el nickname del usuario
     * @returns {string}
     */
    static getNick() {
        const staff = this.getStaffSession();
        return staff ? staff.nick : '';
    }

    /**
     * Obtiene el email del usuario
     * @returns {string}
     */
    static getEmail() {
        const staff = this.getStaffSession();
        return staff ? staff.email : '';
    }

    /**
     * Obtiene el rol del usuario
     * @returns {string}
     */
    static getRole() {
        const staff = this.getStaffSession();
        const rawRole = staff?.role || staff?.data?.role || staff?.staff?.role || null;
        if (!rawRole) {
            return 'USER';
        }

        const role = String(rawRole).toUpperCase().trim();
        if (role.startsWith('ROLE_')) {
            return role.substring(5);
        }
        return role;
    }

    /**
     * Guarda el token JWT
     * @param {string} token - Token JWT
     */
    static setJwtToken(token) {
        this.jwtToken = token || null;
        if (token) {
            sessionStorage.setItem('jwt_token', token);
        } else {
            sessionStorage.removeItem('jwt_token');
        }
    }

    /**
     * Obtiene el token JWT
     * @returns {string|null}
     */
    static getJwtToken() {
        if (!this.jwtToken) {
            const stored = sessionStorage.getItem('jwt_token');
            if (stored) {
                this.jwtToken = stored;
            }
        }
        return this.jwtToken;
    }

    /**
     * Obtiene el header Authorization con el token JWT
     * @returns {Object}
     */
    static getAuthorizationHeader() {
        const token = this.getJwtToken();
        if (token) {
            return {
                'Authorization': `Bearer ${token}`
            };
        }
        return {};
    }

    /**
     * Cierra la sesión del usuario (logout) - Actualizado para limpiar también el JWT
     */
    static logoutWithJwt() {
        this.staffSession = null;
        this.jwtToken = null;
        sessionStorage.removeItem('staff_session');
        sessionStorage.removeItem('jwt_token');
}
}

// Exponer globalmente para la verificación de carga
window.AuthService = AuthService;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('auth.service');



