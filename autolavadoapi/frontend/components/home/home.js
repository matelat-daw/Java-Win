import { loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/home/home.html";

export async function render(outlet) {
  outlet.innerHTML = await loadHtml(TEMPLATE_PATH);
  const base = window.APP_ASSET_BASE || "";
  const card = outlet.querySelector("[data-bg-logo]");
  if (card) card.style.backgroundImage = `url(${base}/imgs/logo.webp)`;
}

