window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

window.TIENDA_COMPONENTS.detalle = async function ({ id }) {
    const container = document.getElementById('detalle-content');
    const producto = await window.TIENDA_API.getProduct(id);
    const moneda = new Intl.NumberFormat('es-ES', {
        style: 'currency',
        currency: 'EUR'
    });
    const imagen = producto.imagen
        ? `${window.TIENDA_CONFIG.assetsBase}/imgs/${producto.imagen}`
        : 'https://placehold.co/900x700/0d1b2d/ffffff?text=Sin+imagen';

    container.innerHTML = `
        <div class="row g-4 align-items-center">
            <div class="col-lg-6">
                <img class="img-fluid rounded-4 shadow-sm" src="${imagen}" alt="${producto.nombre}">
            </div>
            <div class="col-lg-6">
                <span class="chip mb-3">${producto.categoria}</span>
                <h1 class="page-title display-6 mb-2">${producto.nombre}</h1>
                <p class="muted mb-4">ID #${producto.id}</p>
                <div class="d-flex align-items-center gap-3 mb-4">
                    <strong class="display-6 text-info mb-0">${moneda.format(producto.precio)}</strong>
                    <span class="chip">Stock ${producto.stock}</span>
                </div>
                <p class="lead mb-4">${producto.descripcion || 'Sin descripción'}</p>
                <div class="d-flex flex-wrap gap-2">
                    <a class="btn btn-warning" href="#/editar/${producto.id}">Editar</a>
                    <a class="btn btn-outline-light" href="#/catalogo">Catálogo</a>
                </div>
            </div>
        </div>
    `;
};
