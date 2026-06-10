/**
 * utils.js - Utilidades generales de la aplicación
 */

const Utils = {
    /**
     * Valida un campo según un patrón
     * @param {string} value - Valor a validar
     * @param {RegExp} pattern - Patrón de validación
     * @returns {boolean}
     */
    validateField: (value, pattern) => {
        return pattern.test(value);
    },

    /**
     * Valida un formulario
     * @param {Object} data - Datos del formulario
     * @param {Object} rules - Reglas de validación
     * @returns {Object} {valid: boolean, errors: Object}
     */
    validateForm: (data, rules) => {
        const errors = {};
        
        for (const field in rules) {
            if (rules.hasOwnProperty(field)) {
                const rule = rules[field];
                const value = data[field] || '';
                
                if (rule.required && !value.trim()) {
                    errors[field] = `${rule.label} es requerido`;
                    continue;
                }
                
                if (value && rule.pattern && !Utils.validateField(value, rule.pattern)) {
                    errors[field] = rule.message || `${rule.label} no es válido`;
                }
                
                if (value && rule.minLength && value.length < rule.minLength) {
                    errors[field] = `${rule.label} debe tener al menos ${rule.minLength} caracteres`;
                }
            }
        }
        
        return {
            valid: Object.keys(errors).length === 0,
            errors
        };
    },

    /**
     * Muestra un modal de mensaje
     * @param {string} title - Título del mensaje
     * @param {string} body - Contenido del mensaje
     * @param {string} type - Tipo de mensaje: 'success', 'error', 'info', 'warning'
     */
    showMessage: (title, body, type = 'info') => {
        const existingModals = document.querySelectorAll('.modal.show');
        existingModals.forEach(modal => {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) {
                bsModal.hide();
            }
        });

        const messageModal = document.getElementById('messageModal');
        const messageTitle = document.getElementById('messageTitle');
        const messageBody = document.getElementById('messageBody');
        
        // Limpiar clases anteriores
        messageTitle.className = '';
        
        // Agregar icono y clase según el tipo
        let icon = '';
        switch(type) {
            case 'success':
                icon = '✓ ';
                messageTitle.className = 'text-success';
                break;
            case 'error':
                icon = '✕ ';
                messageTitle.className = 'text-danger';
                break;
            case 'warning':
                icon = '⚠ ';
                messageTitle.className = 'text-warning';
                break;
            case 'info':
                icon = 'ℹ ';
                messageTitle.className = 'text-info';
                break;
        }
        
        messageTitle.textContent = icon + title;
        messageBody.innerHTML = body;
        
        // Mostrar modal con opciones mejoradas
        const modal = new bootstrap.Modal(messageModal, {
            backdrop: true,  // Permitir cerrar con backdrop
            keyboard: true   // Permitir cerrar con ESC
        });
        modal.show();
    },

    /**
     * Limpia los campos de un formulario
     * @param {string} formId - ID del formulario
     */
    clearForm: (formId) => {
        const form = document.getElementById(formId);
        if (form) {
            form.reset();
            // Limpiar clases de validación
            form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        }
    },

    /**
     * Muestra errores en un formulario
     * @param {string} formId - ID del formulario
     * @param {Object} errors - Errores a mostrar
     */
    showFormErrors: (formId, errors) => {
        const form = document.getElementById(formId);
        if (!form) return;
        
        // Limpiar errores anteriores
        form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        form.querySelectorAll('.invalid-feedback').forEach(el => el.remove());
        
        // Mostrar nuevos errores
        for (const field in errors) {
            if (errors.hasOwnProperty(field)) {
                const input = form.querySelector(`[name="${field}"]`);
                if (input) {
                    input.classList.add('is-invalid');
                    const feedbackDiv = document.createElement('div');
                    feedbackDiv.className = 'invalid-feedback d-block';
                    feedbackDiv.textContent = errors[field];
                    input.parentNode.appendChild(feedbackDiv);
                }
            }
        }
    },

    /**
     * Cache para peticiones GET
     */
    _cache: new Map(),
    _templateCache: new Map(),

    /**
     * Tiempo de vida del cache en milisegundos (5 minutos)
     */
    _cacheTTL: 5 * 60 * 1000,

    _getAuthorizationHeader: () => {
        if (typeof AuthService === 'undefined') {
            return {};
        }
        if (typeof AuthService.getAuthorizationHeader !== 'function') {
            return {};
        }
        return AuthService.getAuthorizationHeader() || {};
    },

    _buildHttpError: async (response) => {
        const contentType = response.headers.get('content-type') || '';
        let backendPayload = null;
        let backendMessage = response.statusText || 'Error';

        try {
            if (contentType.includes('application/json')) {
                backendPayload = await response.json();
            } else {
                const errorText = await response.text();
                try {
                    backendPayload = JSON.parse(errorText);
                } catch (_) {
                    backendPayload = null;
                }
            }
        } catch (_) {
            backendPayload = null;
        }

        if (backendPayload) {
            backendMessage = backendPayload.message || backendPayload.error || backendMessage;
        }

        const error = new Error(`HTTP ${response.status}: ${backendMessage}`);
        error.status = response.status;
        error.backendMessage = backendMessage;
        error.backendErrors = backendPayload?.errors;
        error.backendPayload = backendPayload;
        return error;
    },

    _isNetworkError: (error) => {
        const message = String(error?.message || '');
        return message.includes('Failed to fetch') || message.includes('NetworkError');
    },

    _isPublicAuthEndpoint: (url) => {
        try {
            const parsed = new URL(url, window.location.origin);
            const path = parsed.pathname;
            return [
                '/api/auth/login',
                '/api/auth/register',
                '/api/auth/verify'
            ].some(endpoint => path === endpoint || path.startsWith(`${endpoint}/`));
        } catch (_) {
            return false;
        }
    },

    _handleUnauthorized: (requestUrl = '') => {
        if (typeof AuthService === 'undefined') {
            return;
        }
        if (Utils._isPublicAuthEndpoint(requestUrl)) {
            return;
        }
        if (!AuthService.hasLocalSession()) {
            return;
        }

        const currentPath = window.location.pathname || '';
        const isAuthScreen = currentPath.endsWith('/login') || currentPath.endsWith('/register');

        if (!isAuthScreen) {
            AuthService.logout();
        }
    },

    /**
     * Realiza una llamada HTTP con cache opcional para GET
     * @param {string} method - Método HTTP
     * @param {string} url - URL
     * @param {Object} data - Datos a enviar
     * @param {boolean} useCache - Si debe intentar usar el cache (solo para GET)
     * @returns {Promise}
     */
    makeRequest: async (method, url, data = null, useCache = false) => {
        // Solo aplicar cache para GET
        if (method.toUpperCase() === 'GET' && useCache) {
            const cached = Utils._cache.get(url);
            if (cached && (Date.now() - cached.timestamp < Utils._cacheTTL)) {
                console.debug(`🚀 Cache hit: ${url}`);
                return cached.data;
            }
        }

        try {
            const headers = {
                'Accept': 'application/json',
                ...Utils._getAuthorizationHeader()
            };

            const options = {
                method: method,
                headers,
                mode: 'cors',
                credentials: 'include'
            };
            
            if (data) {
                options.headers['Content-Type'] = 'application/json';
                options.body = JSON.stringify(data);
            }
            
            const response = await fetch(url, options);
            
            // Interceptor global para error 401 (Token expirado o inválido)
            if (response.status === 401) {
                const error = await Utils._buildHttpError(response);
                Utils._handleUnauthorized(url);
                throw error;
            }

            if (!response.ok) {
                throw await Utils._buildHttpError(response);
            }
            
            if (response.status === 204) {
                return null;
            }

            const responseData = await response.json();
            
            // Guardar en cache si es GET y se solicitó cache
            if (method.toUpperCase() === 'GET' && useCache) {
                Utils._cache.set(url, {
                    data: responseData,
                    timestamp: Date.now()
                });
            }
            
            return responseData;
        } catch (error) {
            if (Utils._isNetworkError(error)) {
                throw new Error('CORS_ERROR: No API available. Make sure the API is running.');
            }
            throw error;
        }
    },

    /**
     * Limpia el cache de peticiones
     */
    clearCache: () => {
        Utils._cache.clear();
        console.debug('🧹 Cache de peticiones limpiado');
    },

    /**
     * Realiza una llamada HTTP con FormData (para archivos)
     * @param {string} method - Método HTTP (GET, POST, etc.)
     * @param {string} url - URL
     * @param {FormData} formData - Datos en FormData
     * @returns {Promise}
     */
    makeRequestWithFormData: async (method, url, formData) => {
        try {
            const headers = {
                ...Utils._getAuthorizationHeader()
            };

            const options = {
                method: method,
                // NO establecer Content-Type - el navegador lo hará automáticamente con boundary
                headers,
                mode: 'cors',
                credentials: 'include',
                body: formData
            };

            const response = await fetch(url, options);
            if (response.status === 401) {
                const error = await Utils._buildHttpError(response);
                Utils._handleUnauthorized(url);
                throw error;
            }

            if (!response.ok) {
                throw await Utils._buildHttpError(response);
            }

            if (response.status === 204) {
                return null;
            }

            return await response.json();
        } catch (error) {
            if (Utils._isNetworkError(error)) {
                throw new Error('CORS_ERROR: No API available. Make sure the backend API is running.');
            }
            
            throw error;
        }
    },

    loadHtml: async (selector, url, useCache = true) => {
        const container = document.querySelector(selector);
        if (!container) {
            return null;
        }

        if (useCache && Utils._templateCache.has(url)) {
            container.innerHTML = Utils._templateCache.get(url);
            return container.innerHTML;
        }

        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        const html = await response.text();
        if (useCache) {
            Utils._templateCache.set(url, html);
        }
        container.innerHTML = html;
        return html;
    },

    /**
     * Obtiene la ruta actual
     * @returns {string}
     */
    getCurrentRoute: () => {
        return window.location.pathname || '/';
    },

    /**
     * Navega a una ruta
     * @param {string} route - Ruta a la que navegar
     */
    navigate: (route) => {
        const app = App.getInstance();
        app.navigateTo(route);
    }
};

// Exponer globalmente para la verificación de carga
window.Utils = Utils;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('utils');


