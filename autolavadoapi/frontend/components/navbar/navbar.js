import { loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/navbar/navbar.html";

export async function render(outlet, { pathname } = {}) {
  outlet.innerHTML = await loadHtml(TEMPLATE_PATH);

  const links = outlet.querySelectorAll("a.nav-link[data-link]");
  links.forEach((a) => a.classList.remove("active"));
  const normalized = pathname?.endsWith("/") && pathname !== "/" ? pathname.slice(0, -1) : pathname;
  const active = Array.from(links).find((a) => {
    const href = a.getAttribute("href");
    if (href === "/") return normalized === "/";
    return normalized === href || normalized?.startsWith(`${href}/`);
  });
  if (active) active.classList.add("active");
}

