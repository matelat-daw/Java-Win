import { apiRequest, formatPrecio, loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/reservas-list/reservas-list.html";
const pageSize = 8;

async function listarReservas({ matricula = "", pendientes = false, sort = "", dir = "asc", page = 0, size = 8 } = {}) {
  const params = new URLSearchParams();
  if (matricula) params.set("matricula", matricula);
  if (pendientes) params.set("pendientes", "true");
  if (sort) params.set("sort", sort);
  if (dir) params.set("dir", dir);
  params.set("page", String(page));
  params.set("size", String(size));
  return apiRequest(`/api/reservas?${params.toString()}`);
}

async function iniciarReserva(id) {
  return apiRequest(`/api/reservas/${encodeURIComponent(id)}/iniciar`, { method: "POST" });
}

async function finalizarReserva(id) {
  return apiRequest(`/api/reservas/${encodeURIComponent(id)}/finalizar`, { method: "POST" });
}

function formatFechaHora(reserva) {
  const fecha = reserva.fecha ?? "";
  const hora = reserva.hora ?? "";
  return hora ? `${fecha} ${hora}` : fecha;
}

function estadoBadge(estado) {
  const value = (estado || "").toUpperCase();
  if (value === "PENDIENTE") return "text-bg-warning";
  if (value === "EN_PROCESO") return "text-bg-info";
  if (value === "FINALIZADO") return "text-bg-success";
  return "text-bg-secondary";
}

function renderRow(reserva) {
  const tr = document.createElement("tr");
  const estado = reserva.estado ?? "";
  const servicio = reserva.servicio?.servicio ?? "";
  const total = formatPrecio(reserva.total);
  const fechaHora = formatFechaHora(reserva);

  const canIniciar = (estado || "").toUpperCase() === "PENDIENTE";
  const canFinalizar = (estado || "").toUpperCase() === "EN_PROCESO";

  tr.innerHTML = `
    <td class="fw-semibold">${reserva.nombreCliente ?? ""}</td>
    <td><span class="font-monospace">${reserva.matricula ?? ""}</span></td>
    <td>${servicio}</td>
    <td>${fechaHora}</td>
    <td><span class="badge ${estadoBadge(estado)}">${estado}</span></td>
    <td class="text-end">${total}</td>
    <td class="text-end">
      <div class="btn-group btn-group-sm" role="group">
        <a class="btn btn-outline-secondary" href="/reservas/${encodeURIComponent(reserva.id)}" data-link>Detalle</a>
        <button class="btn btn-outline-primary" type="button" data-action="iniciar" data-id="${encodeURIComponent(reserva.id)}" ${canIniciar ? "" : "disabled"}>Iniciar</button>
        <button class="btn btn-outline-success" type="button" data-action="finalizar" data-id="${encodeURIComponent(reserva.id)}" ${canFinalizar ? "" : "disabled"}>Finalizar</button>
      </div>
    </td>
  `;

  return tr;
}

export async function render(outlet, { toast } = {}) {
  outlet.innerHTML = await loadHtml(TEMPLATE_PATH);

  const form = outlet.querySelector("[data-filtros]");
  const rowsEl = outlet.querySelector("[data-rows]");
  const paginationEl = outlet.querySelector("[data-pagination]");
  const summaryEl = outlet.querySelector("[data-summary]");
  let currentPage = 0;

  function getFiltersFromForm() {
    const fd = new FormData(form);
    return {
      matricula: (fd.get("matricula") || "").toString().trim(),
      pendientes: fd.get("pendientes") === "on",
      sort: (fd.get("sort") || "").toString(),
      dir: (fd.get("dir") || "asc").toString(),
    };
  }

  function renderPagination({ page, totalPages }) {
    paginationEl.innerHTML = "";
    if (!totalPages || totalPages <= 1) return;

    const makeItem = ({ label, disabled, active, pageIndex }) => {
      const li = document.createElement("li");
      li.className = `page-item${disabled ? " disabled" : ""}${active ? " active" : ""}`;
      const a = document.createElement("a");
      a.className = "page-link";
      a.href = "#";
      a.textContent = label;
      a.addEventListener("click", async (e) => {
        e.preventDefault();
        if (disabled || active) return;
        currentPage = pageIndex;
        await loadAndRender();
      });
      li.appendChild(a);
      return li;
    };

    paginationEl.appendChild(makeItem({ label: "‹", disabled: page <= 0, active: false, pageIndex: Math.max(0, page - 1) }));

    const windowSize = 2;
    const start = Math.max(0, page - windowSize);
    const end = Math.min(totalPages - 1, page + windowSize);
    for (let i = start; i <= end; i++) {
      paginationEl.appendChild(makeItem({ label: String(i + 1), disabled: false, active: i === page, pageIndex: i }));
    }

    paginationEl.appendChild(
      makeItem({ label: "›", disabled: page >= totalPages - 1, active: false, pageIndex: Math.min(totalPages - 1, page + 1) })
    );
  }

  async function loadAndRender() {
    rowsEl.innerHTML = `
      <tr>
        <td colspan="7" class="p-4">
          <div class="placeholder-glow">
            <span class="placeholder col-6"></span>
            <span class="placeholder col-8"></span>
          </div>
        </td>
      </tr>
    `;

    try {
      const { matricula, pendientes, sort, dir } = getFiltersFromForm();
      const pageData = await listarReservas({ matricula, pendientes, sort, dir, page: currentPage, size: pageSize });
      const items = pageData?.items || [];
      const totalItems = pageData?.totalItems ?? 0;
      const totalPages = pageData?.totalPages ?? 0;
      const page = pageData?.page ?? currentPage;
      const size = pageData?.size ?? pageSize;
      currentPage = page;

      const from = totalItems === 0 ? 0 : page * size + 1;
      const to = Math.min((page + 1) * size, totalItems);
      summaryEl.textContent = totalItems === 0 ? "Sin resultados" : `Mostrando ${from}-${to} de ${totalItems}`;

      rowsEl.innerHTML = "";
      if (!items.length) {
        rowsEl.innerHTML = `
          <tr>
            <td colspan="7" class="text-center text-muted p-4">No hay reservas con esos filtros.</td>
          </tr>
        `;
        renderPagination({ page: currentPage, totalPages });
        return;
      }

      items.forEach((r) => rowsEl.appendChild(renderRow(r)));
      renderPagination({ page: currentPage, totalPages });

      rowsEl.querySelectorAll('button[data-action="iniciar"], button[data-action="finalizar"]').forEach((btn) => {
        btn.addEventListener("click", async () => {
          try {
            const id = btn.getAttribute("data-id");
            const action = btn.getAttribute("data-action");
            if (action === "iniciar") await iniciarReserva(id);
            if (action === "finalizar") await finalizarReserva(id);
            await loadAndRender();
            toast?.show?.({ title: "OK", message: "Estado actualizado", variant: "success", delay: 1400 });
          } catch (e) {
            toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
          }
        });
      });
    } catch (e) {
      rowsEl.innerHTML = `
        <tr>
          <td colspan="7" class="p-0">
            <div class="alert alert-danger m-3">${e.message}</div>
          </td>
        </tr>
      `;
      summaryEl.textContent = "";
      paginationEl.innerHTML = "";
      toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
    }
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    currentPage = 0;
    await loadAndRender();
  });

  await loadAndRender();
}

