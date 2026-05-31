import { escapeHtml, escapeJsString, loadTemplate, renderTemplate } from '../shared/template.js';

export async function catalogoHtml({ products, imgBaseUrl, page = 1, pageSize = 8 }) {
    const [tpl, cardTpl, rowTpl] = await Promise.all([
        loadTemplate(new URL('./catalogo.html', import.meta.url)),
        loadTemplate(new URL('./product-card.html', import.meta.url)),
        loadTemplate(new URL('./product-row.html', import.meta.url))
    ]);

    const total = Array.isArray(products) ? products.length : 0;
    const size = Math.max(1, Number(pageSize) || 8);
    const totalPages = Math.max(1, Math.ceil(total / size));
    const currentPage = Math.min(Math.max(1, Number(page) || 1), totalPages);
    const start = (currentPage - 1) * size;
    const end = start + size;
    const pageProducts = (Array.isArray(products) ? products : []).slice(start, end);

    const productsMobile = pageProducts.map((p) => {
        const stock = p?.stock ?? 0;
        const activo = p?.activo === false ? false : true;
        return renderTemplate(cardTpl, {
            id: escapeHtml(p.id),
            nombre: escapeHtml(p.nombre),
            nombreJs: escapeJsString(p.nombre),
            categoria: escapeHtml(p.categoria),
            stock: escapeHtml(String(stock)),
            activoLabel: activo ? 'Activo' : 'Inactivo',
            activoBadgeClass: activo ? 'text-bg-success' : 'text-bg-secondary',
            precio: Number(p.precio).toFixed(2),
            imagenSrc: `${imgBaseUrl}/${escapeHtml(p.imagen)}`
        });
    }).join('');

    const productsTable = pageProducts.map((p) => {
        const stock = p?.stock ?? 0;
        const activo = p?.activo === false ? false : true;
        return renderTemplate(rowTpl, {
            id: escapeHtml(p.id),
            nombre: escapeHtml(p.nombre),
            nombreJs: escapeJsString(p.nombre),
            categoria: escapeHtml(p.categoria),
            stock: escapeHtml(String(stock)),
            activoLabel: activo ? 'Activo' : 'Inactivo',
            activoBadgeClass: activo ? 'text-bg-success' : 'text-bg-secondary',
            precio: Number(p.precio).toFixed(2),
            imagenSrc: `${imgBaseUrl}/${escapeHtml(p.imagen)}`
        });
    }).join('');

    const pagination = totalPages > 1 ? buildPaginationHtml({ currentPage, totalPages }) : '';

    return renderTemplate(tpl, { productsMobile, productsTable, pagination });
}

function buildPaginationHtml({ currentPage, totalPages }) {
    const items = [];

    const prevDisabled = currentPage <= 1 ? 'disabled' : '';
    const nextDisabled = currentPage >= totalPages ? 'disabled' : '';

    items.push(`
        <li class="page-item ${prevDisabled}">
            <button class="page-link" type="button" onclick="navigate('catalogo', {page: ${currentPage - 1}})" ${prevDisabled ? 'disabled' : ''}>Anterior</button>
        </li>
    `);

    const pagesToShow = new Set([1, totalPages]);
    for (let p = currentPage - 2; p <= currentPage + 2; p++) {
        if (p >= 1 && p <= totalPages) pagesToShow.add(p);
    }
    const orderedPages = Array.from(pagesToShow).sort((a, b) => a - b);

    let last = 0;
    for (const p of orderedPages) {
        if (last && p > last + 1) {
            items.push('<li class="page-item disabled"><span class="page-link">…</span></li>');
        }
        const active = p === currentPage ? 'active' : '';
        items.push(`
            <li class="page-item ${active}">
                <button class="page-link" type="button" onclick="navigate('catalogo', {page: ${p}})">${p}</button>
            </li>
        `);
        last = p;
    }

    items.push(`
        <li class="page-item ${nextDisabled}">
            <button class="page-link" type="button" onclick="navigate('catalogo', {page: ${currentPage + 1}})" ${nextDisabled ? 'disabled' : ''}>Siguiente</button>
        </li>
    `);

    return `
        <nav class="mt-4" aria-label="Paginación de catálogo">
            <ul class="pagination justify-content-center mb-0">
                ${items.join('')}
            </ul>
        </nav>
    `;
}
