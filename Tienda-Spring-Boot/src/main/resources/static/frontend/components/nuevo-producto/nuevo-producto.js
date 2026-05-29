window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

window.TIENDA_COMPONENTS.nuevo = async function () {
    const form = document.getElementById('nuevo-form');
    const categorias = await window.TIENDA_API.getCategories();
    const select = document.getElementById('nuevo-categoria');
    const alertBox = document.getElementById('nuevo-alert');

    categorias.forEach(categoria => {
        const option = document.createElement('option');
        option.value = categoria;
        option.textContent = categoria;
        select.appendChild(option);
    });

    form.addEventListener('submit', async event => {
        event.preventDefault();
        alertBox.innerHTML = '';

        try {
            await window.TIENDA_API.createProduct(new FormData(form));
            alertBox.innerHTML = '<div class="alert alert-success">Producto guardado correctamente.</div>';
            form.reset();
            setTimeout(() => {
                location.hash = '#/catalogo';
            }, 600);
        } catch (error) {
            alertBox.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
        }
    });
};
