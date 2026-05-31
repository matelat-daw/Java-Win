import { loadTemplate } from '../shared/template.js';

export async function homeHtml() {
    return loadTemplate(new URL('./home.html', import.meta.url));
}
