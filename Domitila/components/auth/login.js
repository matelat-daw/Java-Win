import { loadTemplate } from '../shared/template.js';

export async function loginHtml() {
    return loadTemplate(new URL('./login.html', import.meta.url));
}

