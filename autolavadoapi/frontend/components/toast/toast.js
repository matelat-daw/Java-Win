import { loadHtml } from "../../core.js";

const TEMPLATE_PATH = "/components/toast/toast.html";

export async function createToastService(outlet) {
  const template = await loadHtml(TEMPLATE_PATH);

  function show({ title, message, variant = "primary", delay = 3500 }) {
    const wrapper = document.createElement("div");
    wrapper.innerHTML = template.trim();
    const toastEl = wrapper.firstElementChild;
    toastEl.classList.add(`text-bg-${variant}`);

    const titleEl = toastEl.querySelector("[data-title]");
    const messageEl = toastEl.querySelector("[data-message]");
    if (titleEl) titleEl.textContent = title || "";
    if (messageEl) messageEl.textContent = message || "";

    outlet.appendChild(toastEl);
    const toast = bootstrap.Toast.getOrCreateInstance(toastEl, { delay });
    toastEl.addEventListener(
      "hidden.bs.toast",
      () => {
        toastEl.remove();
      },
      { once: true }
    );
    toast.show();
  }

  return { show };
}

