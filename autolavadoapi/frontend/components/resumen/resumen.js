import { apiRequest, formatPrecio, loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/resumen/resumen.html";

async function obtenerResumen() {
  return apiRequest("/api/reservas/resumen");
}

function card({ title, value, variant }) {
  const col = document.createElement("div");
  col.className = "col-12 col-md-4";
  col.innerHTML = `
    <div class="card shadow-sm border-0 h-100">
      <div class="card-body">
        <div class="text-muted small">${title}</div>
        <div class="display-6 fw-semibold text-${variant}">${value}</div>
      </div>
    </div>
  `;
  return col;
}

export async function render(outlet, { toast } = {}) {
  outlet.innerHTML = await loadHtml(TEMPLATE_PATH);
  const cardsEl = outlet.querySelector("[data-cards]");

  cardsEl.innerHTML = `
    <div class="col-12">
      <div class="card shadow-sm border-0">
        <div class="card-body">
          <div class="placeholder-glow">
            <span class="placeholder col-6"></span>
            <span class="placeholder col-5"></span>
          </div>
        </div>
      </div>
    </div>
  `;

  try {
    const resumen = await obtenerResumen();
    cardsEl.innerHTML = "";
    cardsEl.appendChild(card({ title: "Total de reservas", value: resumen.total ?? 0, variant: "primary" }));
    cardsEl.appendChild(card({ title: "Pendientes", value: resumen.pendientes ?? 0, variant: "warning" }));
    cardsEl.appendChild(card({ title: "Ingresos", value: formatPrecio(resumen.ingresos), variant: "success" }));
  } catch (e) {
    cardsEl.innerHTML = `
      <div class="col-12">
        <div class="alert alert-danger mb-0">${e.message}</div>
      </div>
    `;
    toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
  }
}

