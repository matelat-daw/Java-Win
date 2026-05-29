window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

window.TIENDA_COMPONENTS.contacto = async function () {
    const form = document.getElementById('contacto-form');
    const alertBox = document.getElementById('contacto-alert');

    form.addEventListener('submit', async event => {
        event.preventDefault();
        alertBox.innerHTML = '';

        const payload = Object.fromEntries(new FormData(form).entries());

        try {
            await window.TIENDA_API.sendContact(payload);
            alertBox.innerHTML = '<div class="alert alert-success">Mensaje enviado correctamente.</div>';
            form.reset();
        } catch (error) {
            alertBox.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
        }
    });
};
