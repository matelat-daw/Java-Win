/**
 * main.js - Punto de entrada de la aplicaci�n
 * Inicializa la aplicaci�n cuando todos los scripts se han cargado
 * Sistema de registro eficiente sin polling
 */

async function initializeApp() {
    try {
        const requiredScripts = [
            'constants',
            'utils',
            'client.model',
            'auth.service',
            'client.service',
            'customer.service',
            'navbar',
            'register',
            'login',
            'dashboard',
            'profile',
            'users',
            'user-details',
            'customers',
            'customer-details',
            'app'
        ];

        window.AppScripts.required = requiredScripts;

        const waitForScripts = (timeoutMs) => new Promise((resolve, reject) => {
            if (window.AppScripts && window.AppScripts.allReady()) {
                resolve();
                return;
            }

            const onLoaded = () => {
                if (window.AppScripts && window.AppScripts.allReady()) {
                    cleanup();
                    resolve();
                }
            };

            const timeoutId = setTimeout(() => {
                cleanup();
                reject(new Error('Timeout esperando scripts'));
            }, timeoutMs);

            const cleanup = () => {
                clearTimeout(timeoutId);
                window.removeEventListener('appscript:loaded', onLoaded);
            };

            window.addEventListener('appscript:loaded', onLoaded);
        });

        await waitForScripts(10000);

        const missing = requiredScripts.filter(script => !AppScripts.isReady(script));
        if (missing.length > 0) {
            throw new Error(`Scripts faltantes: ${missing.join(', ')}`);
        }

        // Verificar que Bootstrap est� disponible
        if (!window.bootstrap) {
            throw new Error('Bootstrap no se pudo cargar desde CDN');
        }

        if (typeof AuthService !== 'undefined' && typeof AuthService.bootstrapSession === 'function') {
            await AuthService.bootstrapSession();
        }

        App.init();
    } catch (error) {
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="container mt-5">
                    <div class="alert alert-danger" role="alert">
                        <h4 class="alert-heading">Error de inicializaci�n</h4>
                        <p>${error.message}</p>
                        <hr>
                        <p class="mb-0 small">Por favor recarga la p�gina. Si el problema persiste, verifica la consola del navegador.</p>
                        <button class="btn btn-primary mt-3" onclick="location.reload()">Recargar p�gina</button>
                    </div>
                </div>
            `;
        }
    }
}

// Esperar a que el DOM est� listo antes de inicializar
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeApp);
} else {
    // El DOM ya est� listo
    initializeApp();
}


