window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

window.TIENDA_COMPONENTS.editar = async function ({ id }) {
    const form = document.getElementById('editar-form');
    const categorias = await window.TIENDA_API.getCategories();
    const producto = await window.TIENDA_API.getProduct(id);
    const select = document.getElementById('editar-categoria');
    const alertBox = document.getElementById('editar-alert');

    categorias.forEach(categoria => {
        const option = document.createElement('option');
        option.value = categoria;
        option.textContent = categoria;
        select.appendChild(option);
    });

    form.elements.id.value = producto.id;
    form.nombre.value = producto.nombre;
    form.precio.value = producto.precio;
    form.stock.value = producto.stock;
    form.categoria.value = producto.categoria;
    form.descripcion.value = producto.descripcion || '';

    form.addEventListener('submit', async event => {
        event.preventDefault();
        alertBox.innerHTML = '';

        try {
            await window.TIENDA_API.updateProduct(producto.id, new FormData(form));
            alertBox.innerHTML = '<div class="alert alert-success">Producto actualizado correctamente.</div>';
            setTimeout(() => {
                location.hash = `#/detalle/${producto.id}`;
            }, 600);
        } catch (error) {
            alertBox.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
        }
    });
};
