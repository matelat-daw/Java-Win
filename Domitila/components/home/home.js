import { loadTemplate, renderTemplate } from '../shared/template.js';

export async function homeHtml({ homeActions = '', featuredCta = '', featuredContent = '' } = {}) {
    const tpl = await loadTemplate(new URL('./home.html', import.meta.url));
    return renderTemplate(tpl, { homeActions, featuredCta, featuredContent });
}
