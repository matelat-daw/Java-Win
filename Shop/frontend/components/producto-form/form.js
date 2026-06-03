import { escapeHtml, loadTemplate, renderTemplate } from '../shared/template.js';

export async function productoFormHtml({ producto, isEdit, id, opcionesCategoriasHtml, imgBaseUrl }) {
    const tpl = await loadTemplate(new URL('./form.html', import.meta.url));
    const p = producto;
    const previewImagen = isEdit && p.imagen
        ? `<div class="mb-2"><img src="${imgBaseUrl}/${escapeHtml(p.imagen)}" class="img-thumbnail" style="max-height: 100px;"></div>`
        : '';

    const activo = p.activo === false ? false : true;

    return renderTemplate(tpl, {
        titulo: isEdit ? '✏️ Editar Producto' : '➕ Nuevo Producto',
        id: escapeHtml(id || ''),
        imagenActual: escapeHtml(p.imagen || ''),
        nombre: escapeHtml(p.nombre),
        precio: escapeHtml(p.precio),
        stock: escapeHtml(p.stock ?? 0),
        activoChecked: activo ? 'checked' : '',
        opcionesCategorias: opcionesCategoriasHtml,
        descripcion: escapeHtml(p.descripcion),
        previewImagen,
        fileRequired: isEdit ? '' : 'required'
    });
}
