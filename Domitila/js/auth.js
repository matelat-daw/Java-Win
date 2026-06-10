const TOKEN_KEY = 'shop.token';
const USER_KEY = 'shop.user';

export function getToken() {
    return localStorage.getItem(TOKEN_KEY) || '';
}

export function getUser() {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
}

export function isAuthenticated() {
    return Boolean(getToken());
}

export function isAdmin() {
    const user = getUser();
    return String(user?.role || '').toUpperCase() === 'ADMIN';
}

export function setSession({ token, user }) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

export async function authFetch(input, init = {}, { auth = true } = {}) {
    const headers = new Headers(init.headers || {});
    if (auth) {
        const token = getToken();
        if (token) headers.set('Authorization', `Bearer ${token}`);
    }
    const res = await fetch(input, {
        ...init,
        headers,
        credentials: init.credentials ?? 'include'
    });
    if (res.status === 401) {
        clearSession();
    }
    return res;
}
