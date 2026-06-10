import { loadTemplate } from '../shared/template.js';

export async function registerHtml() {
    return loadTemplate(new URL('./register.html', import.meta.url));
}

