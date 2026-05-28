import { renderShell } from './components/shell/shell.js';
import { renderAuthPanel } from './components/auth-panel/auth-panel.js';
import { renderDashboard } from './components/dashboard/dashboard.js';
import { renderApiConsole } from './components/api-console/api-console.js';

const appRoot = document.getElementById('app');
const apiBaseUrl = resolveApiBaseUrl();

const state = {
    session: null,
    activeSection: 'inicio',
    lastResponse: {
        title: 'No se ha ejecutado ninguna petición todavía.',
        status: 'Pendiente',
        ok: null,
        body: 'Usa el panel de API para llamar a /api/publico, /api/privado o /api/admin.',
    },
    working: false,
};

const shell = await renderShell(appRoot, {
    onNavigate: navigateTo,
    onLogout: handleLogout,
});

await refreshSession();
await renderCurrentView();

async function refreshSession() {
    try {
        const response = await apiFetch('/api/auth/me', { credentials: 'include' });
        if (!response.ok) {
            state.session = null;
            return;
        }

        state.session = await response.json();
    } catch {
        state.session = null;
    }
}

async function renderCurrentView() {
    shell.setNavigation(state.activeSection, state.session);
    shell.setSession(state.session);
    shell.setLastResponse(state.lastResponse);
    shell.setWorking(state.working);

    if (!state.session) {
        shell.setBanner({
            eyebrow: 'Acceso a la API',
            title: 'Entra con tu usuario y navega el backend desde una sola pantalla.',
            description: 'La sesión se crea en el servidor para que el logout realmente cierre el acceso autenticado.',
        });

        await renderAuthPanel(shell.mainSlot, {
            onLogin: handleLogin,
            working: state.working,
        });
        shell.consoleSlot.innerHTML = '';
        return;
    }

    shell.setBanner({
        eyebrow: 'Sesión activa',
        title: `Bienvenido, ${state.session.username}.`,
        description: 'Desde este panel puedes probar los endpoints públicos, privados y de administración.',
    });

    const dashboardView = await renderDashboard(shell.mainSlot, {
        session: state.session,
    });

    await renderApiConsole(shell.consoleSlot, {
        lastResponse: state.lastResponse,
        working: state.working,
        onRequest: handleRequest,
        onQuickRequest: handleQuickRequest,
    });

    dashboardView.syncSession(state.session);
}

function navigateTo(section) {
    state.activeSection = section;
    void renderCurrentView();
}

async function handleLogin(credentials) {
    state.working = true;
    await renderCurrentView();

    const response = await apiFetch('/api/auth/login', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
    });

    const payload = await readResponseBody(response);

    if (!response.ok) {
        state.working = false;
        await renderAuthPanel(shell.mainSlot, {
            onLogin: handleLogin,
            working: state.working,
            error: payload?.message ?? 'No se pudo iniciar sesión.',
        });
        return;
    }

    state.session = payload;
    state.working = false;
    state.activeSection = 'inicio';
    await renderCurrentView();
}

async function handleLogout() {
    if (!state.session) {
        return;
    }

    state.working = true;
    await renderCurrentView();

    try {
        await apiFetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include',
        });
    } finally {
        state.session = null;
        state.working = false;
        state.activeSection = 'inicio';
        await renderCurrentView();
    }
}

async function handleRequest(endpoint) {
    state.working = true;
    shell.setWorking(state.working);

    const response = await apiFetch(endpoint, {
        credentials: 'include',
    });

    const body = await readResponseBody(response);

    state.lastResponse = {
        title: endpoint,
        status: `${response.status} ${response.statusText}`.trim(),
        ok: response.ok,
        body: typeof body === 'string' ? body : JSON.stringify(body, null, 2),
    };

    if (response.status === 401) {
        state.session = null;
    }

    state.working = false;
    await renderCurrentView();
}

async function handleQuickRequest(endpoint) {
    await handleRequest(endpoint);
}

async function readResponseBody(response) {
    const contentType = response.headers.get('content-type') ?? '';
    if (contentType.includes('application/json')) {
        return response.json();
    }

    return response.text();
}

function resolveApiBaseUrl() {
    const explicitBaseUrl = window.__API_BASE_URL__;
    if (typeof explicitBaseUrl === 'string' && explicitBaseUrl.trim() !== '') {
        return explicitBaseUrl.replace(/\/$/, '');
    }

    if (window.location.protocol === 'file:') {
        return 'http://127.0.0.1:8085';
    }

    if (window.location.port === '5500' || window.location.port === '5501') {
        return 'http://127.0.0.1:8085';
    }

    return window.location.origin;
}

function apiFetch(path, options = {}) {
    const url = new URL(path, apiBaseUrl);
    return fetch(url, options);
}