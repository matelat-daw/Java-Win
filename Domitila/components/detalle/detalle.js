import { escapeHtml, loadTemplate, renderTemplate } from '../shared/template.js';

export async function detalleHtml({ producto, imgBaseUrl, isAdmin = false }) {
    const tpl = await loadTemplate(new URL('./detalle.html', import.meta.url));
    const p = producto;
    const adminEditButton = isAdmin
        ? `<div class="col-12 col-sm-6"><button class="btn btn-warning btn-lg w-100" onclick="navigate('editar', {id: '${escapeHtml(p.id)}'})">✏️ Editar</button></div>`
        : '';
    return renderTemplate(tpl, {
        id: escapeHtml(p.id),
        nombre: escapeHtml(p.nombre),
        categoria: escapeHtml(p.categoria),
        precio: Number(p.precio).toFixed(2),
        descripcion: escapeHtml(p.descripcion),
        imagenSrc: `${imgBaseUrl}/${escapeHtml(p.imagen)}`,
        adminEditButton
    });
}
