import { apiRequest, formatPrecio, loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/servicios/servicios.html";

async function listarServicios() {
  return apiRequest("/api/servicios");
}

function crearCard(servicio) {
  const col = document.createElement("div");
  col.className = "col-12 col-md-6 col-lg-4";

  const base = window.APP_ASSET_BASE || "";
  const hasImagen = Boolean(servicio.imagen);
  const imgUrl = hasImagen ? `${base}/imgs/${servicio.imagen}` : "";

  col.innerHTML = `
    <div class="card shadow border-0 h-100 overflow-hidden">
      <div class="ratio ratio-4x3 bg-light">
        ${
          hasImagen
            ? `<a href="#" class="d-block w-100 h-100 text-decoration-none servicio-preview-link" data-bs-toggle="modal" data-bs-target="#servicioImagenModal" data-imagen="${servicio.imagen}" data-titulo="${servicio.servicio ?? ""}">
                 <img class="w-100 h-100 object-fit-cover servicio-preview-image" src="${imgUrl}" alt="${servicio.servicio ?? ""}" />
               </a>`
            : `<div class="d-flex align-items-center justify-content-center text-muted small">Sin imagen disponible</div>`
        }
      </div>
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-start gap-2">
          <div>
            <h5 class="card-title mb-1">${servicio.servicio ?? ""}</h5>
            <div class="text-muted small">${servicio.descripcion ?? ""}</div>
          </div>
          <span class="badge text-bg-primary">${formatPrecio(servicio.precio)}</span>
        </div>
      </div>
      <div class="card-footer bg-white border-0 d-flex gap-2">
        <a class="btn btn-outline-primary w-100" href="/reservas/nueva?servicioId=${encodeURIComponent(servicio.id)}" data-link>Reservar</a>
        <button class="btn btn-outline-secondary w-100" type="button" ${hasImagen ? "" : "disabled"} data-bs-toggle="modal" data-bs-target="#servicioImagenModal" data-imagen="${servicio.imagen ?? ""}" data-titulo="${servicio.servicio ?? ""}">Ver imagen</button>
      </div>
    </div>
  `;

  return col;
}

function initModal(outlet) {
  const modalEl = outlet.querySelector("#servicioImagenModal");
  if (!modalEl) return;

  modalEl.addEventListener("show.bs.modal", (event) => {
    const trigger = event.relatedTarget;
    const imagen = trigger?.getAttribute("data-imagen");
    const titulo = trigger?.getAttribute("data-titulo");
    const imgEl = modalEl.querySelector("#servicioImagenModalImg");
    const titleEl = modalEl.querySelector("#servicioImagenModalLabel");

    const base = window.APP_ASSET_BASE || "";
    const src = imagen ? `${base}/imgs/${imagen}` : `${base}/imgs/logo.webp`;
    if (imgEl) {
      imgEl.src = src;
      imgEl.alt = titulo || "Servicio";
    }
    if (titleEl) titleEl.textContent = titulo || "Imagen ampliada";
  });

  modalEl.addEventListener("hidden.bs.modal", () => {
    const imgEl = modalEl.querySelector("#servicioImagenModalImg");
    if (imgEl) {
      imgEl.src = "";
      imgEl.alt = "Imagen del servicio";
    }
  });

  const grid = outlet.querySelector("[data-servicios]");
  if (grid) {
    grid.addEventListener("click", (e) => {
      const link = e.target.closest(".servicio-preview-link");
      if (!link) return;
      e.preventDefault();
      const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
      modal.show(link);
    });
  }
}

export async function render(outlet, { toast } = {}) {
  outlet.innerHTML = await loadHtml(TEMPLATE_PATH);
  const grid = outlet.querySelector("[data-servicios]");

  grid.innerHTML = `
    <div class="col-12">
      <div class="card shadow-sm border-0">
        <div class="card-body">
          <div class="placeholder-glow">
            <span class="placeholder col-7"></span>
            <span class="placeholder col-4"></span>
            <span class="placeholder col-4"></span>
            <span class="placeholder col-6"></span>
          </div>
        </div>
      </div>
    </div>
  `;

  try {
    const servicios = await listarServicios();
    grid.innerHTML = "";
    servicios.forEach((s) => grid.appendChild(crearCard(s)));
    initModal(outlet);
  } catch (e) {
    grid.innerHTML = `
      <div class="col-12">
        <div class="alert alert-danger mb-0">${e.message}</div>
      </div>
    `;
    toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
  }
}
