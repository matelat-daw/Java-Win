import { loadHtml } from "./core.js";

async function renderNavbar(outlet, pathname) {
  const mod = await import("./components/navbar/navbar.js");
  await mod.render(outlet, { pathname, navigate });
}

async function renderToast(outlet) {
  const mod = await import("./components/toast/toast.js");
  return mod.createToastService(outlet);
}

function compileRoute(path) {
  const paramNames = [];
  const pattern = path
    .split("/")
    .map((seg) => {
      if (seg.startsWith(":")) {
        paramNames.push(seg.slice(1));
        return "([^/]+)";
      }
      return seg.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    })
    .join("/");
  return { regex: new RegExp(`^${pattern}$`), paramNames };
}

function matchRoute(routes, pathname) {
  for (const r of routes) {
    const { regex, paramNames } = compileRoute(r.path);
    const m = pathname.match(regex);
    if (!m) continue;
    const params = {};
    paramNames.forEach((name, idx) => (params[name] = decodeURIComponent(m[idx + 1])));
    return { route: r, params };
  }
  return null;
}

const routes = [
  { path: "/", module: "./components/home/home.js" },
  { path: "/servicios", module: "./components/servicios/servicios.js" },
  { path: "/reservas", module: "./components/reservas-list/reservas-list.js" },
  { path: "/reservas/nueva", module: "./components/reserva-form/reserva-form.js" },
  { path: "/reservas/:id", module: "./components/reserva-detalle/reserva-detalle.js" },
  { path: "/resumen", module: "./components/resumen/resumen.js" },
];

const navbarOutlet = document.getElementById("navbar-outlet");
const appOutlet = document.getElementById("app-outlet");
const toastOutlet = document.getElementById("toast-outlet");

let toast;

export function navigate(path) {
  history.pushState({}, "", path);
  return render();
}

async function render() {
  const pathname = window.location.pathname || "/";
  await renderNavbar(navbarOutlet, pathname);

  const match = matchRoute(routes, pathname);
  if (!match) {
    history.replaceState({}, "", "/");
    return render();
  }

  const mod = await import(match.route.module);
  await mod.render(appOutlet, {
    params: match.params,
    navigate,
    toast,
  });
}

document.body.addEventListener("click", (e) => {
  const link = e.target.closest("a[data-link]");
  if (!link) return;
  const href = link.getAttribute("href");
  if (!href) return;
  e.preventDefault();
  navigate(href);
});

window.addEventListener("popstate", () => render());

toast = await renderToast(toastOutlet);
await render();

