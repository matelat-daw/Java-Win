import { loadTemplate } from './template.js';

export async function spinnerHtml() {
    return loadTemplate(new URL('./spinner.html', import.meta.url));
}
