import { apiRequest, formatPrecio, loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/reserva-detalle/reserva-detalle.html";

async function obtenerReserva(id) {
  return apiRequest(`/api/reservas/${encodeURIComponent(id)}`);
}

async function iniciarReserva(id) {
  return apiRequest(`/api/reservas/${encodeURIComponent(id)}/iniciar`, { method: "POST" });
}

async function finalizarReserva(id) {
  return apiRequest(`/api/reservas/${encodeURIComponent(id)}/finalizar`, { method: "POST" });
}

function estadoBadge(estado) {
  const value = (estado || "").toUpperCase();
  if (value === "PENDIENTE") return "text-bg-warning";
  if (value === "EN_PROCESO") return "text-bg-info";
  if (value === "FINALIZADO") return "text-bg-success";
  return "text-bg-secondary";
}

function canIniciar(estado) {
  return (estado || "").toUpperCase() === "PENDIENTE";
}

function canFinalizar(estado) {
  return (estado || "").toUpperCase() === "EN_PROCESO";
}

export async function render(outlet, { params, toast } = {}) {
  outlet.innerHTML = await loadHtml(TEMPLATE_PATH);

  const id = params?.id;
  const content = outlet.querySelector("[data-content]");
  const subtitle = outlet.querySelector("[data-subtitle]");

  async function load() {
    content.innerHTML = `
      <div class="placeholder-glow">
        <span class="placeholder col-7"></span>
        <span class="placeholder col-4"></span>
        <span class="placeholder col-4"></span>
        <span class="placeholder col-6"></span>
      </div>
    `;

    const reserva = await obtenerReserva(id);
    subtitle.textContent = `#${reserva.id} — ${reserva.matricula ?? ""}`;

    const servicio = reserva.servicio?.servicio ?? "";
    const total = formatPrecio(reserva.total);

    content.innerHTML = `
      <div class="row g-3">
        <div class="col-12 col-md-6">
          <div class="text-muted small">Cliente</div>
          <div class="fw-semibold">${reserva.nombreCliente ?? ""}</div>
        </div>
        <div class="col-12 col-md-6">
          <div class="text-muted small">Teléfono</div>
          <div class="font-monospace">${reserva.telefono ?? ""}</div>
        </div>
        <div class="col-12 col-md-6">
          <div class="text-muted small">Servicio</div>
          <div>${servicio}</div>
        </div>
        <div class="col-12 col-md-6">
          <div class="text-muted small">Fecha/Hora</div>
          <div>${reserva.fecha ?? ""} ${reserva.hora ?? ""}</div>
        </div>
        <div class="col-12 col-md-6">
          <div class="text-muted small">Estado</div>
          <div><span class="badge ${estadoBadge(reserva.estado)}">${reserva.estado ?? ""}</span></div>
        </div>
        <div class="col-12 col-md-6">
          <div class="text-muted small">Total</div>
          <div class="fw-semibold">${total}</div>
        </div>
        <div class="col-12">
          <div class="text-muted small">Observaciones</div>
          <div>${reserva.observaciones ? reserva.observaciones : '<span class="text-muted">—</span>'}</div>
        </div>
        <div class="col-12 d-flex gap-2 justify-content-end">
          <button class="btn btn-outline-primary" type="button" data-action="iniciar" ${canIniciar(reserva.estado) ? "" : "disabled"}>Iniciar</button>
          <button class="btn btn-outline-success" type="button" data-action="finalizar" ${canFinalizar(reserva.estado) ? "" : "disabled"}>Finalizar</button>
        </div>
      </div>
    `;

    content.querySelector('[data-action="iniciar"]')?.addEventListener("click", async () => {
      try {
        await iniciarReserva(id);
        await load();
        toast?.show?.({ title: "OK", message: "Lavado iniciado", variant: "success", delay: 1400 });
      } catch (e) {
        toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
      }
    });

    content.querySelector('[data-action="finalizar"]')?.addEventListener("click", async () => {
      try {
        await finalizarReserva(id);
        await load();
        toast?.show?.({ title: "OK", message: "Lavado finalizado", variant: "success", delay: 1400 });
      } catch (e) {
        toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
      }
    });
  }

  try {
    await load();
  } catch (e) {
    content.innerHTML = `<div class="alert alert-danger mb-0">${e.message}</div>`;
    toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
  }
}

