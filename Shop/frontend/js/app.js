import { spinnerHtml } from '../components/shared/spinner.js';
import { homeHtml } from '../components/home/home.js';
import { catalogoHtml } from '../components/catalogo/catalogo.js';
import { detalleHtml } from '../components/detalle/detalle.js';
import { loginHtml } from '../components/auth/login.js';
import { registerHtml } from '../components/auth/register.js';
import { profileHtml } from '../components/profile/profile.js';
import { productoFormHtml } from '../components/producto-form/form.js';
import { resumenHtml } from '../components/resumen/resumen.js';
import { contactoHtml } from '../components/contacto/contacto.js';
import { deleteModalHtml, successModalHtml } from '../components/shared/modals.js';
import { escapeHtml } from '../components/shared/template.js';
import { authFetch, clearSession, isAdmin, isAuthenticated, setSession, getUser } from './auth.js';

function resolveApiOrigin() {
    const { protocol, hostname, port, origin } = window.location;
    if (port === '8080') return origin;
    return `${protocol}//${hostname}:8080`;
}

const API_ORIGIN = resolveApiOrigin();
const API_BASE_URL = `${API_ORIGIN}/api`;
const IMG_BASE_URL = `${API_ORIGIN}/api/imgs`;

const app = document.getElementById('app');
const modalContainer = document.getElementById('modal-container');

let categoriasCache = null;
const MAX_IMAGE_BYTES = 25 * 1024 * 1024;

function normalizeCategoriaItem(item) {
    if (item == null) return null;

    if (typeof item === 'string') {
        const nombre = item.trim();
        if (!nombre) return null;
        return { id: nombre, nombre };
    }

    if (typeof item === 'number') {
        return { id: item, nombre: String(item) };
    }

    if (typeof item !== 'object') {
        const nombre = String(item).trim();
        if (!nombre) return null;
        return { id: nombre, nombre };
    }

    const id = item.id ?? item.value ?? item.key ?? null;
    const rawNombre = item.nombre ?? item.name ?? item.label ?? null;

    let nombre;
    if (typeof rawNombre === 'string') {
        nombre = rawNombre.trim();
    } else if (rawNombre && typeof rawNombre === 'object') {
        const nested = rawNombre.nombre ?? rawNombre.name ?? rawNombre.label ?? null;
        nombre = typeof nested === 'string' ? nested.trim() : '';
    } else {
        nombre = rawNombre == null ? '' : String(rawNombre).trim();
    }

    const finalNombre = nombre || (id == null ? '' : String(id));
    if (!finalNombre) return null;

    return { id: id ?? finalNombre, nombre: finalNombre };
}

function productoCategoriaId(producto) {
    const cat = producto?.categoria;
    if (typeof cat === 'number') return cat;
    if (typeof cat === 'string') return null;
    if (cat && typeof cat === 'object') {
        const id = cat.id ?? cat.value ?? null;
        return typeof id === 'number' ? id : null;
    }
    return null;
}

async function fetchCategorias() {
    if (Array.isArray(categoriasCache)) return categoriasCache;
    const res = await authFetch(`${API_BASE_URL}/categorias`);
    if (!res.ok) throw new Error(`Error al cargar categorías (${res.status})`);
    const data = await res.json();
    categoriasCache = (Array.isArray(data) ? data : [])
        .map(normalizeCategoriaItem)
        .filter(Boolean);
    return categoriasCache;
}

const BASE_PATH = (document.querySelector('base')?.getAttribute('href') || '/').replace(/\/$/, '');

function pathFor(route, params = {}) {
    switch (route) {
        case 'home':
            return `${BASE_PATH}/`;
        case 'catalogo':
            if (params.page && Number(params.page) > 1) {
                return `${BASE_PATH}/catalogo?page=${encodeURIComponent(String(params.page))}`;
            }
            return `${BASE_PATH}/catalogo`;
        case 'nuevo':
            return `${BASE_PATH}/nuevo`;
        case 'resumen':
            return `${BASE_PATH}/resumen`;
        case 'contacto':
            return `${BASE_PATH}/contacto`;
        case 'detalle':
            return `${BASE_PATH}/detalle/${params.id}`;
        case 'editar':
            return `${BASE_PATH}/editar/${params.id}`;
        case 'login':
            return `${BASE_PATH}/login`;
        case 'register':
            return `${BASE_PATH}/register`;
        case 'logout':
            return `${BASE_PATH}/logout`;
        case 'profile':
            return `${BASE_PATH}/profile`;
        default:
            return `${BASE_PATH}/`;
    }
}

function routeFromLocation() {
    const pathname = window.location.pathname;
    const withoutBase = (BASE_PATH && pathname.startsWith(BASE_PATH))
        ? pathname.slice(BASE_PATH.length)
        : pathname;
    const clean = withoutBase.replace(/^\/+/, '').replace(/\/+$/, '');

    if (!clean) return { route: 'home', params: {} };

    const parts = clean.split('/');
    const route = parts[0];
    const id = parts[1];

    if (route === 'detalle' && id) return { route: 'detalle', params: { id } };
    if (route === 'editar' && id) return { route: 'editar', params: { id } };
    if (route === 'catalogo') {
        const page = Number(new URLSearchParams(window.location.search).get('page')) || 1;
        return { route: 'catalogo', params: { page } };
    }
    if (route === 'nuevo') return { route: 'nuevo', params: {} };
    if (route === 'resumen') return { route: 'resumen', params: {} };
    if (route === 'contacto') return { route: 'contacto', params: {} };
    if (route === 'login') return { route: 'login', params: {} };
    if (route === 'register') return { route: 'register', params: {} };
    if (route === 'logout') return { route: 'logout', params: {} };
    if (route === 'profile') return { route: 'profile', params: {} };

    return { route: 'home', params: {} };
}

function navigate(route, params = {}) {
    const path = pathFor(route, params);
    window.history.pushState({ route, params }, '', path);
    render(route, params);
}

// Routes
async function render(route, params = {}) {
    app.innerHTML = await spinnerHtml();

    updateNavbar();

    const publicRoutes = new Set(['home', 'login', 'register', 'detalle']);
    if (!isAuthenticated() && !publicRoutes.has(route)) {
        navigate('login');
        return;
    }

    const adminOnlyRoutes = new Set(['catalogo', 'nuevo', 'editar', 'resumen']);
    if (isAuthenticated() && !isAdmin() && adminOnlyRoutes.has(route)) {
        navigate('home');
        return;
    }
    
    switch(route) {
        case 'home':
            await renderHome();
            break;
        case 'catalogo':
            await renderCatalogo(params.page);
            break;
        case 'detalle':
            await renderDetalle(params.id);
            break;
        case 'nuevo':
            await renderFormulario();
            break;
        case 'editar':
            await renderFormulario(params.id);
            break;
        case 'resumen':
            await renderResumen();
            break;
        case 'contacto':
            await renderContacto();
            break;
        case 'login':
            await renderLogin();
            break;
        case 'register':
            await renderRegister();
            break;
        case 'logout':
            await renderLogout();
            break;
        case 'profile':
            await renderProfile();
            break;
        default:
            await renderHome();
    }
}

// Views
async function renderHome() {
    const homeActions = !isAuthenticated()
        ? `
            <a class="btn btn-primary btn-lg px-4" onclick="navigate('login')">🔐 Login</a>
            <a class="btn btn-outline-primary btn-lg px-4" onclick="navigate('register')">🧾 Registro</a>
        `
        : (
            isAdmin()
                ? `
                    <a class="btn btn-primary btn-lg px-4" onclick="navigate('catalogo')">📦 Gestionar productos</a>
                    <a class="btn btn-outline-primary btn-lg px-4" onclick="navigate('resumen')">📊 Resumen</a>
                    <a class="btn btn-link btn-lg px-2 text-decoration-none" onclick="navigate('contacto')">Contactar soporte →</a>
                `
                : ''
        );

    try {
        const response = await authFetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error(`Error ${response.status}`);
        const products = await response.json();
        const featured = (Array.isArray(products) ? products : []).slice(0, 6);

        const cards = featured.map((p) => `
            <div class="col-12 col-md-4">
                <div class="card h-100 shadow-sm border-0">
                    <img src="${IMG_BASE_URL}/${escapeHtml(p.imagen)}" class="card-img-top" style="height: 180px; object-fit: cover;">
                    <div class="card-body d-flex flex-column">
                        <div class="d-flex align-items-start justify-content-between gap-2">
                            <h3 class="h6 mb-2">${escapeHtml(p.nombre)}</h3>
                            <span class="badge bg-info">${escapeHtml(p.categoria)}</span>
                        </div>
                        <div class="fw-bold text-success mb-2">€ ${Number(p.precio).toFixed(2)}</div>
                        <div class="mt-auto d-flex gap-2">
                            <button class="btn btn-sm btn-primary" onclick="navigate('detalle', {id: '${escapeHtml(p.id)}'})">Ver detalles</button>
                            <button class="btn btn-sm btn-success" onclick="alert('Carrito pendiente de implementar')">Agregar al carrito</button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');

        const featuredCta = '';
        const featuredContent = featured.length
            ? `<div class="row g-3">${cards}</div>`
            : '<div class="alert alert-warning mb-0">No hay productos para mostrar.</div>';

        app.innerHTML = await homeHtml({ homeActions, featuredCta, featuredContent });
    } catch (error) {
        app.innerHTML = await homeHtml({
            homeActions,
            featuredCta: '',
            featuredContent: '<div class="alert alert-danger mb-0">Error al cargar productos destacados.</div>'
        });
    }
}

async function renderCatalogo(page = 1) {
    try {
        const response = await authFetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error(`Error ${response.status}`);
        const products = await response.json();
        app.innerHTML = await catalogoHtml({ products, imgBaseUrl: IMG_BASE_URL, page, pageSize: 8, isAdmin: isAdmin() });
    } catch (error) {
        app.innerHTML = '<div class="alert alert-danger">Error al cargar el catálogo</div>';
    }
}

async function renderDetalle(id) {
    try {
        const response = await authFetch(`${API_BASE_URL}/products/${id}`);
        if (!response.ok) throw new Error(`Error ${response.status}`);
        const p = await response.json();
        app.innerHTML = await detalleHtml({ producto: p, imgBaseUrl: IMG_BASE_URL, isAdmin: isAdmin() });
    } catch (error) {
        app.innerHTML = '<div class="alert alert-danger">Error al cargar el producto</div>';
    }
}

async function renderFormulario(id = null) {
    let p = { nombre: '', precio: 0, stock: 0, categoria: '', descripcion: '', imagen: '', activo: true };
    const isEdit = id !== null;
    
    if (isEdit) {
        try {
            const response = await authFetch(`${API_BASE_URL}/products/${id}`);
            p = await response.json();
        } catch (error) {
            app.innerHTML = '<div class="alert alert-danger">Error al cargar datos</div>';
            return;
        }
    }

    let categorias = [];
    try {
        categorias = await fetchCategorias();
    } catch (e) {
        categorias = [];
    }

    const productoCategoriaNumericId = productoCategoriaId(p);
    const categoriaSeleccionadaId = (productoCategoriaNumericId != null)
        ? productoCategoriaNumericId
        : (
            categorias.find((c) =>
                String(c?.nombre || '').toLowerCase() === String(p.categoria || '').toLowerCase()
            )?.id ?? ''
        );

    const opcionesCategorias = [
        `<option value="" disabled ${!categoriaSeleccionadaId ? 'selected' : ''}>Selecciona una categoría</option>`,
        ...categorias.map((c) => {
            const selected = String(categoriaSeleccionadaId) === String(c.id) ? 'selected' : '';
            return `<option value="${escapeHtml(String(c.id))}" ${selected}>${escapeHtml(c.nombre)}</option>`;
        })
    ].join('');

    app.innerHTML = await productoFormHtml({
        producto: p,
        isEdit,
        id,
        opcionesCategoriasHtml: opcionesCategorias,
        imgBaseUrl: IMG_BASE_URL
    });

    document.getElementById('productForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const imagenActual = (formData.get('imagenActual') || '').toString();
        const nombre = (formData.get('nombre') || '').toString().trim();
        if (nombre.length < 2 || nombre.length > 80) {
            alert('El nombre es obligatorio y debe tener entre 2 y 80 caracteres.');
            return;
        }
        const categoriaRaw = (formData.get('categoria') || '').toString().trim();
        const categoriaId = categoriaRaw ? Number(categoriaRaw) : null;
        if (categoriaRaw && !Number.isFinite(categoriaId)) {
            alert('Categoría inválida. Selecciona una categoría válida.');
            return;
        }
        const productData = {
            nombre,
            precio: parseFloat(formData.get('precio')),
            stock: parseInt(formData.get('stock'), 10),
            categoria: Number.isFinite(categoriaId) ? categoriaId : null,
            descripcion: formData.get('descripcion'),
            activo: formData.get('activo') === 'on'
        };

        if (isEdit && imagenActual.trim()) {
            productData.imagen = imagenActual.trim();
        }
        
        try {
            let response;
            if (isEdit) {
                response = await authFetch(`${API_BASE_URL}/products/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(productData)
                });
            } else {
                const fileInputCreate = formData.get('imagen');
                if (!fileInputCreate || !fileInputCreate.size) {
                    alert('Selecciona una imagen para crear el producto');
                    return;
                }
                response = await authFetch(`${API_BASE_URL}/products`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(productData)
                });
            }

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Error ${response.status}: ${errorText || 'sin detalle'}`);
            }
            
            const savedProduct = await response.json();
            const fileInput = formData.get('imagen');
            
            if (fileInput && fileInput.size > 0) {
                if (fileInput.size > MAX_IMAGE_BYTES) {
                    alert('La imagen es demasiado grande (máx 25MB).');
                    return;
                }
                const imgFormData = new FormData();
                imgFormData.append('imagen', fileInput);
                // Usar 'id' en edición, 'savedProduct.id' en creación
                const productId = isEdit ? id : savedProduct.id;
                await authFetch(`${API_BASE_URL}/products/${productId}/image`, {
                    method: 'POST',
                    body: imgFormData
                });
            }
            
            showSuccessModal(isEdit ? 'Actualizado' : 'Creado', savedProduct.nombre);
            navigate('catalogo');
        } catch (error) {
            console.error(error);
            alert('Error al guardar el producto. Revisa la consola para más detalle.');
        }
    };
}

async function renderResumen() {
    try {
        const response = await authFetch(`${API_BASE_URL}/products`);
        if (!response.ok) throw new Error(`Error ${response.status}`);
        const products = await response.json();
        
        const total = products.length;
        const categorias = [...new Set((products || []).map((p) => {
            const cat = p?.categoria;
            if (typeof cat === 'string') return cat;
            if (typeof cat === 'number') return String(cat);
            if (cat && typeof cat === 'object') {
                const nombre = cat.nombre ?? cat.name ?? cat.label ?? null;
                if (typeof nombre === 'string') return nombre;
                return String(cat.id ?? '');
            }
            return '';
        }).filter(Boolean))];
        const avg = total > 0 ? products.reduce((acc, p) => acc + p.precio, 0) / total : 0;
        const max = total > 0 ? Math.max(...products.map(p => p.precio)) : 0;
        const min = total > 0 ? Math.min(...products.map(p => p.precio)) : 0;

        app.innerHTML = await resumenHtml({
            total,
            categoriasCount: categorias.length,
            avg,
            max,
            min
        });
    } catch (error) {
        app.innerHTML = '<div class="alert alert-danger">Error al cargar resumen</div>';
    }
}

function updateNavbar() {
    const authed = isAuthenticated();
    const admin = authed && isAdmin();
    const user = getUser();

    const setVisible = (id, visible) => {
        const el = document.getElementById(id);
        if (!el) return;
        el.style.display = visible ? '' : 'none';
    };

    setVisible('nav-catalogo', admin);
    setVisible('nav-resumen', admin);
    setVisible('nav-contacto', authed);
    setVisible('nav-login', !authed);
    setVisible('nav-register', !authed);
    setVisible('nav-profile', authed);
    setVisible('nav-logout', authed);
    setVisible('nav-role', authed);

    const roleText = document.querySelector('#nav-role .navbar-text');
    if (roleText) {
        const role = String(user?.role || '').toUpperCase();
        const email = String(user?.email || '').trim();
        roleText.textContent = role ? `${email} (${role})` : email;
    }
}

async function renderProfile() {
    try {
        const res = await authFetch(`${API_BASE_URL}/profile`);
        const body = await res.json().catch(() => null);
        if (!res.ok || !body?.success) {
            const msg = body?.message || `Error al cargar perfil (${res.status})`;
            throw new Error(msg);
        }
        app.innerHTML = await profileHtml({ profile: body.data, apiOrigin: API_ORIGIN });
    } catch (error) {
        app.innerHTML = `<div class="container py-4"><div class="alert alert-danger">${escapeHtml(error?.message || 'Error al cargar perfil')}</div></div>`;
    }
}

async function renderLogin() {
    app.innerHTML = await loginHtml();

    const form = document.getElementById('loginForm');
    const errorBox = document.getElementById('loginError');
    if (!form) return;

    form.onsubmit = async (e) => {
        e.preventDefault();
        if (errorBox) errorBox.classList.add('d-none');

        const formData = new FormData(form);
        const email = String(formData.get('email') || '').trim();
        const password = String(formData.get('password') || '');

        try {
            const res = await authFetch(`${API_BASE_URL}/user/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            }, { auth: false });

            const body = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = body?.detail || body?.message || `Login falló (${res.status})`;
                throw new Error(msg);
            }

            const token = body?.token || '';
            const user = body?.data || null;
            if (!token || !user) throw new Error('Respuesta inválida del servidor');

            setSession({ token, user });
            updateNavbar();
            navigate('home');
        } catch (error) {
            if (errorBox) {
                errorBox.textContent = error?.message || 'Error al iniciar sesión';
                errorBox.classList.remove('d-none');
            }
        }
    };
}

async function renderRegister() {
    app.innerHTML = await registerHtml();

    const form = document.getElementById('registerForm');
    const errorBox = document.getElementById('registerError');
    const successBox = document.getElementById('registerSuccess');
    if (!form) return;

    form.onsubmit = async (e) => {
        e.preventDefault();
        if (errorBox) errorBox.classList.add('d-none');
        if (successBox) successBox.classList.add('d-none');

        const fd = new FormData(form);
        try {
            const res = await authFetch(`${API_BASE_URL}/user/register`, {
                method: 'POST',
                body: fd
            }, { auth: false });

            const body = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = body?.detail || body?.message || `Registro falló (${res.status})`;
                throw new Error(msg);
            }

            if (successBox) {
                successBox.textContent = 'Registro creado. Revisa tu correo para verificar la cuenta y luego inicia sesión.';
                successBox.classList.remove('d-none');
            }
            form.reset();
        } catch (error) {
            if (errorBox) {
                errorBox.textContent = error?.message || 'Error al registrar';
                errorBox.classList.remove('d-none');
            }
        }
    };
}

async function renderLogout() {
    try {
        await authFetch(`${API_BASE_URL}/auth/logout`, { method: 'POST' });
    } catch {
    } finally {
        clearSession();
        updateNavbar();
        navigate('home');
    }
}

async function renderContacto() {
    app.innerHTML = await contactoHtml();

    const form = document.getElementById('contactForm');
    const alertContainer = document.getElementById('contacto-alert');
    const submitBtn = document.getElementById('contacto-submit');

    if (!form) return;

    const setAlert = (type, message) => {
        if (!alertContainer) return;
        const safeMessage = escapeHtml(message || '');
        alertContainer.innerHTML = safeMessage
            ? `<div class="alert alert-${type} border-0 shadow-sm rounded-3 mb-3">${safeMessage}</div>`
            : '';
    };

    form.onsubmit = async (e) => {
        e.preventDefault();
        setAlert('', '');

        const nombre = (document.getElementById('contacto-nombre')?.value || '').toString().trim();
        const email = (document.getElementById('contacto-email')?.value || '').toString().trim();
        const mensaje = (document.getElementById('contacto-mensaje')?.value || '').toString().trim();

        if (!nombre || !email || !mensaje) {
            setAlert('warning', 'Completa todos los campos.');
            return;
        }

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.dataset.originalText = submitBtn.textContent || '';
            submitBtn.textContent = 'Enviando...';
        }

        try {
            const res = await authFetch(`${API_BASE_URL}/contact`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nombre, email, mensaje })
            });

            const body = await res.json().catch(() => null);
            if (!res.ok) {
                const msg = body?.detail || body?.message || `Error al enviar (${res.status})`;
                throw new Error(msg);
            }

            const msg = body?.message || 'Nos pondremos en contacto contigo en breve.';
            setAlert('success', msg);
            form.reset();
        } catch (error) {
            setAlert('danger', error?.message || 'Error al enviar el mensaje.');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = submitBtn.dataset.originalText || 'Enviar mensaje';
            }
        }
    };
}

// Helpers
async function confirmarEliminacion(id, nombre) {
    const modalHtml = await deleteModalHtml({ id, nombre });
    modalContainer.innerHTML = modalHtml;
    const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
}

async function deleteProduct(id) {
    try {
        const res = await authFetch(`${API_BASE_URL}/products/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(`Error ${res.status}`);
        bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
        const { params } = routeFromLocation();
        await renderCatalogo(params.page);
    } catch (error) {
        alert('Error al eliminar');
    }
}

async function showSuccessModal(accion, nombre) {
    const modalHtml = await successModalHtml({ accion, nombre });
    modalContainer.innerHTML = modalHtml;
    const modal = new bootstrap.Modal(document.getElementById('successModal'));
    modal.show();
}

window.navigate = navigate;
window.confirmarEliminacion = confirmarEliminacion;
window.deleteProduct = deleteProduct;

window.addEventListener('popstate', () => {
    const { route, params } = routeFromLocation();
    render(route, params);
});

window.addEventListener('load', () => {
    const { route, params } = routeFromLocation();
    render(route, params);
});
