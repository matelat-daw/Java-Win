import { loadTemplate } from '../shared/template.js';

export async function contactoHtml() {
    return loadTemplate(new URL('./contacto.html', import.meta.url));
}