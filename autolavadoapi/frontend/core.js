const htmlCache = new Map();

export async function loadHtml(path) {
  if (htmlCache.has(path)) return htmlCache.get(path);
  const res = await fetch(path, { cache: "no-cache" });
  if (!res.ok) throw new Error(`No se pudo cargar: ${path}`);
  const html = await res.text();
  htmlCache.set(path, html);
  return html;
}

export async function apiRequest(path, { method = "GET", body } = {}) {
  const base = window.APP_API_BASE || "";
  const url = path.startsWith("http") ? path : `${base}${path}`;

  const headers = { Accept: "application/json" };
  const init = { method, headers };
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(body);
  }

  const res = await fetch(url, init);
  const contentType = res.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const data = isJson ? await res.json() : await res.text();

  if (!res.ok) {
    const looksLikeHtml = typeof data === "string" && data.toLowerCase().includes("<html");
    const message = looksLikeHtml
      ? "El servidor devolvió HTML en vez de JSON (revisa APP_API_BASE o el proxy /api en Nginx)"
      : data?.message || res.statusText;
    const err = new Error(message);
    err.status = res.status;
    err.fieldErrors = data?.fieldErrors || {};
    throw err;
  }

  return data;
}

export function formatPrecio(precio) {
  const n = typeof precio === "number" ? precio : Number(precio);
  if (Number.isNaN(n)) return `${precio ?? ""}`;
  return n.toLocaleString("es-ES", { style: "currency", currency: "EUR" });
}

