const templateCache = new Map();

export function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

export function escapeJsString(value) {
    return String(value).replaceAll('\\', '\\\\').replaceAll("'", "\\'");
}

export async function loadTemplate(url) {
    const key = url.href || String(url);
    if (templateCache.has(key)) return templateCache.get(key);
    const res = await fetch(url);
    if (!res.ok) throw new Error(`No se pudo cargar plantilla: ${key} (${res.status})`);
    const text = await res.text();
    templateCache.set(key, text);
    return text;
}

export function renderTemplate(template, data) {
    let out = template;
    for (const [key, value] of Object.entries(data)) {
        out = out.replaceAll(`{{${key}}}`, String(value));
    }
    return out;
}

