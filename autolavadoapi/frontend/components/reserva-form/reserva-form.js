import { apiRequest, formatPrecio, loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/reserva-form/reserva-form.html";

function todayIso() {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

function getBooleanFromRadios(form, name, defaultValue = true) {
  const el = form.querySelector(`input[name="${name}"]:checked`);
  if (!el) return defaultValue;
  return el.value === "true";
}

function setFieldError(form, name, message) {
  const control = form.querySelector(`[name="${name}"]`);
  const errorEl = form.querySelector(`[data-error="${name}"]`);
  if (message) {
    control?.classList.add("is-invalid");
    if (errorEl) errorEl.textContent = message;
  } else {
    control?.classList.remove("is-invalid");
    if (errorEl) errorEl.textContent = "";
  }
}

function clearErrors(form) {
  form.querySelectorAll("[data-error]").forEach((el) => (el.textContent = ""));
  form.querySelectorAll(".is-invalid").forEach((el) => el.classList.remove("is-invalid"));
}

function esTelefonoValido(telefono, tipoTelefono) {
  if (telefono == null) return false;
  const normalizado = telefono.replace(/[\s-]/g, "");
  if (tipoTelefono) return /^[67]\d{8}$/.test(normalizado);
  return /^(?:\+|00)\d{7,15}$/.test(normalizado);
}

function esMatriculaValida(matricula, tipoMatricula) {
  if (matricula == null) return false;
  const normalizada = matricula.replace(/[\s-]/g, "").toUpperCase();
  if (tipoMatricula) {
    const sistemaModerno = /^(?!.*(?:CH|LL))\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$/;
    const provincialConLetras = /^[A-Z]{1,2}\d{4}[A-Z]{1,2}$/;
    const provincialSoloNumeros = /^[A-Z]{1,2}\d{1,6}$/;
    return sistemaModerno.test(normalizada) || provincialConLetras.test(normalizada) || provincialSoloNumeros.test(normalizada);
  }
  return /^[A-Z0-9]{5,12}$/.test(normalizada);
}

async function listarServicios() {
  return apiRequest("/api/servicios");
}

async function crearReserva(payload) {
  return apiRequest("/api/reservas", { method: "POST", body: payload });
}

function cargarServicios(select, servicios) {
  select.innerHTML = `<option value="">Selecciona...</option>`;
  servicios.forEach((s) => {
    const opt = document.createElement("option");
    opt.value = s.id;
    opt.textContent = `${s.servicio} — ${formatPrecio(s.precio)}`;
    select.appendChild(opt);
  });
}

function sincronizarMatriculaSegunTelefono(form) {
  const telefonoEsp = form.querySelector("#tipoTelefonoEsp")?.checked;
  const matEsp = form.querySelector("#tipoMatriculaEsp");
  const matExt = form.querySelector("#tipoMatriculaExt");
  if (!matEsp || !matExt) return;
  matExt.checked = !telefonoEsp;
  matEsp.checked = Boolean(telefonoEsp);
}

export async function render(outlet, { navigate, toast } = {}) {
  outlet.innerHTML = await loadHtml(TEMPLATE_PATH);

  const form = outlet.querySelector("[data-form]");
  const selectServicio = form.querySelector("#servicioId");
  const fechaInput = form.querySelector("#fecha");

  fechaInput.min = todayIso();

  try {
    const servicios = await listarServicios();
    cargarServicios(selectServicio, servicios);
  } catch (e) {
    toast?.show?.({ title: "Error", message: e.message, variant: "danger" });
  }

  const urlParams = new URLSearchParams(window.location.search);
  const preServicioId = urlParams.get("servicioId");
  if (preServicioId) {
    selectServicio.value = preServicioId;
  }

  form.querySelectorAll('input[name="tipoTelefono"]').forEach((r) =>
    r.addEventListener("change", () => sincronizarMatriculaSegunTelefono(form))
  );
  sincronizarMatriculaSegunTelefono(form);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearErrors(form);

    const nombreCliente = form.nombreCliente.value.trim();
    const telefono = form.telefono.value.trim();
    const matricula = form.matricula.value.trim();
    const servicioId = form.servicioId.value;
    const fecha = form.fecha.value;
    const hora = form.hora.value;
    const observaciones = form.observaciones.value.trim();

    const tipoTelefono = getBooleanFromRadios(form, "tipoTelefono", true);
    const tipoMatricula = getBooleanFromRadios(form, "tipoMatricula", true);

    let ok = true;

    if (!nombreCliente || nombreCliente.length < 3 || nombreCliente.length > 80) {
      setFieldError(form, "nombreCliente", "El nombre debe tener entre 3 y 80 caracteres");
      ok = false;
    }

    if (!telefono) {
      setFieldError(form, "telefono", "El teléfono es obligatorio");
      ok = false;
    } else if (!esTelefonoValido(telefono, tipoTelefono)) {
      setFieldError(form, "telefono", "El teléfono no coincide con el tipo seleccionado");
      ok = false;
    }

    if (!matricula) {
      setFieldError(form, "matricula", "La matrícula es obligatoria");
      ok = false;
    } else if (matricula.length < 6 || matricula.length > 12) {
      setFieldError(form, "matricula", "La matrícula debe tener entre 6 y 12 caracteres");
      ok = false;
    } else if (!esMatriculaValida(matricula, tipoMatricula)) {
      setFieldError(form, "matricula", "La matrícula no coincide con el tipo seleccionado");
      ok = false;
    }

    if (!servicioId) {
      setFieldError(form, "servicioId", "El tipo de servicio es obligatorio");
      ok = false;
    }

    if (!fecha) {
      setFieldError(form, "fecha", "La fecha es obligatoria");
      ok = false;
    } else if (fecha < todayIso()) {
      setFieldError(form, "fecha", "La fecha no puede ser anterior a hoy");
      ok = false;
    }

    if (!hora) {
      setFieldError(form, "hora", "La hora es obligatoria");
      ok = false;
    }

    if (observaciones.length > 200) {
      setFieldError(form, "observaciones", "Las observaciones no pueden superar 200 caracteres");
      ok = false;
    }

    if (!ok) return;

    try {
      await crearReserva({
        nombreCliente,
        tipoTelefono,
        telefono,
        tipoMatricula,
        matricula,
        servicioId: Number(servicioId),
        fecha,
        hora,
        observaciones: observaciones || null,
      });
      toast?.show?.({ title: "OK", message: "Reserva creada", variant: "success" });
      navigate?.("/reservas");
    } catch (err) {
      const fieldErrors = err?.fieldErrors || {};
      Object.entries(fieldErrors).forEach(([field, msg]) => setFieldError(form, field, msg));
      toast?.show?.({ title: "Error", message: err?.message || "No se pudo crear la reserva", variant: "danger" });
    }
  });
}

