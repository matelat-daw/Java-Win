import { loadTemplate, renderTemplate } from '../shared/template.js';

export async function resumenHtml({ total, categoriasCount, avg, max, min }) {
    const tpl = await loadTemplate(new URL('./resumen.html', import.meta.url));
    return renderTemplate(tpl, {
        total,
        categoriasCount,
        avg: avg.toFixed(2),
        max: max.toFixed(2),
        min: min.toFixed(2)
    });
}
