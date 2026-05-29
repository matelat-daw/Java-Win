window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

window.TIENDA_COMPONENTS.resumen = async function () {
    const resumen = await window.TIENDA_API.getSummary();
    const grid = document.getElementById('resumen-grid');
    const moneda = new Intl.NumberFormat('es-ES', {
        style: 'currency',
        currency: 'EUR'
    });

    const cards = [
        { title: 'Total de productos', value: resumen.totalProductos, className: 'info' },
        { title: 'Total de categorías', value: resumen.totalCategorias, className: 'success' },
        { title: 'Precio promedio', value: moneda.format(resumen.precioPromedio), className: 'warning' },
        { title: 'Producto más caro', value: moneda.format(resumen.precioMayor), className: 'danger' },
        { title: 'Producto más barato', value: moneda.format(resumen.precioMenor), className: 'secondary' }
    ];

    grid.innerHTML = cards.map(card => `
        <div class="col-md-6 col-xl-4">
            <div class="card-surface p-4 h-100 border-start border-4 border-${card.className}">
                <p class="muted mb-2">${card.title}</p>
                <div class="display-6 mb-0">${card.value}</div>
            </div>
        </div>
    `).join('');
};
