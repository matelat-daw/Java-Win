window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

function ensureHashRoute() {
    if (!location.hash) {
        location.hash = '#/inicio';
    }
}

window.addEventListener('hashchange', () => {
    window.TIENDA_ROUTER.render().catch(error => {
        document.getElementById('app').innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
    });
});

document.addEventListener('DOMContentLoaded', async () => {
    ensureHashRoute();

    try {
        await window.TIENDA_ROUTER.render();
    } catch (error) {
        document.getElementById('app').innerHTML = `
            <div class="hero-panel fade-in">
                <h1 class="page-title h2 mb-3">No se pudo cargar la aplicación</h1>
                <p class="mb-0 text-danger">${error.message}</p>
            </div>
        `;
    }
});
