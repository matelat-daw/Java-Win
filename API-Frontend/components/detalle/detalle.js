import { escapeHtml, loadTemplate, renderTemplate } from '../shared/template.js';

export async function detalleHtml({ producto, imgBaseUrl }) {
    const tpl = await loadTemplate(new URL('./detalle.html', import.meta.url));
    const p = producto;
    return renderTemplate(tpl, {
        id: escapeHtml(p.id),
        nombre: escapeHtml(p.nombre),
        categoria: escapeHtml(p.categoria),
        precio: Number(p.precio).toFixed(2),
        descripcion: escapeHtml(p.descripcion),
        imagenSrc: `${imgBaseUrl}/${escapeHtml(p.imagen)}`
    });
}
