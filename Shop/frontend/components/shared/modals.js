import { escapeHtml, loadTemplate, renderTemplate } from './template.js';

let parsedTemplates = null;

async function getTemplates() {
    if (parsedTemplates) return parsedTemplates;
    const html = await loadTemplate(new URL('./modals.html', import.meta.url));
    const doc = new DOMParser().parseFromString(html, 'text/html');
    const deleteTpl = doc.getElementById('deleteModalTpl')?.innerHTML || '';
    const successTpl = doc.getElementById('successModalTpl')?.innerHTML || '';
    parsedTemplates = { deleteTpl, successTpl };
    return parsedTemplates;
}

export async function deleteModalHtml({ id, nombre }) {
    const { deleteTpl } = await getTemplates();
    return renderTemplate(deleteTpl, { id: escapeHtml(id), nombre: escapeHtml(nombre) });
}

export async function successModalHtml({ accion, nombre }) {
    const { successTpl } = await getTemplates();
    return renderTemplate(successTpl, { accion: escapeHtml(accion), nombre: escapeHtml(nombre) });
}
