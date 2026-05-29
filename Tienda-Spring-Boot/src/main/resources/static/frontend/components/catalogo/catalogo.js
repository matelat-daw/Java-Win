window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

window.TIENDA_COMPONENTS.catalogo = async function () {
    const grid = document.getElementById('catalogo-grid');
    const categoriaSelect = document.getElementById('catalogo-categoria');
    const buscarInput = document.getElementById('catalogo-buscar');
    const limpiarBtn = document.getElementById('catalogo-limpiar');
    const alertBox = document.getElementById('catalogo-alert');
    const modalElement = document.getElementById('modalEliminarProducto');
    const modal = new bootstrap.Modal(modalElement);
    const modalNombre = document.getElementById('catalogo-producto-a-eliminar');
    const modalConfirmar = document.getElementById('catalogo-confirmar-eliminacion');

    let productos = [];
    let productoPendiente = null;

    const moneda = new Intl.NumberFormat('es-ES', {
        style: 'currency',
        currency: 'EUR'
    });

    function imagenUrl(nombre) {
        return `${window.TIENDA_CONFIG.assetsBase}/imgs/${nombre}`;
    }

    function mostrarAlerta(mensaje, tipo = 'success') {
        alertBox.innerHTML = `
            <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
                ${mensaje}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
            </div>
        `;
    }

    function render() {
        const texto = buscarInput.value.trim().toLowerCase();
        const categoria = categoriaSelect.value;

        const filtrados = productos.filter(producto => {
            const coincideCategoria = !categoria || producto.categoria === categoria;
            const coincideTexto = !texto || [producto.nombre, producto.categoria, producto.descripcion]
                .filter(Boolean)
                .join(' ')
                .toLowerCase()
                .includes(texto);
            return coincideCategoria && coincideTexto;
        });

        if (!filtrados.length) {
            grid.innerHTML = `
                <div class="col-12">
                    <div class="panel p-4 text-center muted">No hay productos para mostrar.</div>
                </div>
            `;
            return;
        }

        grid.innerHTML = filtrados.map(producto => `
            <div class="col-sm-6 col-lg-4 col-xxl-3">
                <article class="product-card">
                    <img class="product-cover" src="${producto.imagen ? imagenUrl(producto.imagen) : 'https://placehold.co/600x450/0d1b2d/ffffff?text=Sin+imagen'}" alt="${producto.nombre}">
                    <div class="card-body-soft">
                        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
                            <h3 class="h5 mb-0">${producto.nombre}</h3>
                            <span class="badge badge-soft">#${producto.id}</span>
                        </div>
                        <p class="mb-2"><span class="chip">${producto.categoria}</span></p>
                        <p class="muted small mb-3">${producto.descripcion || 'Sin descripción'}</p>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <strong class="fs-5 text-info">${moneda.format(producto.precio)}</strong>
                            <span class="chip">Stock ${producto.stock}</span>
                        </div>
                        <div class="d-flex flex-wrap gap-2">
                            <a class="btn btn-outline-light btn-sm" href="#/detalle/${producto.id}">Ver</a>
                            <a class="btn btn-warning btn-sm" href="#/editar/${producto.id}">Editar</a>
                            <button class="btn btn-danger btn-sm" data-delete="${producto.id}">Eliminar</button>
                        </div>
                    </div>
                </article>
            </div>
        `).join('');

        grid.querySelectorAll('[data-delete]').forEach(button => {
            button.addEventListener('click', () => {
                productoPendiente = filtrados.find(item => String(item.id) === button.dataset.delete);
                modalNombre.textContent = productoPendiente ? productoPendiente.nombre : '';
                modal.show();
            });
        });
    }

    categoriaSelect.addEventListener('change', render);
    buscarInput.addEventListener('input', render);
    limpiarBtn.addEventListener('click', () => {
        categoriaSelect.value = '';
        buscarInput.value = '';
        render();
    });

    modalConfirmar.addEventListener('click', async () => {
        if (!productoPendiente) {
            return;
        }

        try {
            await window.TIENDA_API.deleteProduct(productoPendiente.id);
            productos = productos.filter(item => item.id !== productoPendiente.id);
            render();
            modal.hide();
            mostrarAlerta(`Producto <strong>${productoPendiente.nombre}</strong> eliminado correctamente.`, 'success');
            productoPendiente = null;
        } catch (error) {
            mostrarAlerta(error.message, 'danger');
        }
    });

    const [categorias, dataProductos] = await Promise.all([
        window.TIENDA_API.getCategories(),
        window.TIENDA_API.getProducts()
    ]);

    categorias.forEach(categoria => {
        const option = document.createElement('option');
        option.value = categoria;
        option.textContent = categoria;
        categoriaSelect.appendChild(option);
    });

    productos = dataProductos;
    render();
};
