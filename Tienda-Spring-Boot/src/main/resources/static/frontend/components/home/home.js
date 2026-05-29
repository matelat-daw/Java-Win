window.TIENDA_COMPONENTS = window.TIENDA_COMPONENTS || {};

window.TIENDA_COMPONENTS.inicio = async function () {
    const info = await window.TIENDA_API.getInfo();

    document.getElementById('home-message').textContent = 'Catálogo, resumen y contacto consumiendo una API JSON.';
    document.getElementById('home-address').textContent = info.direccion;
    document.getElementById('home-phone').textContent = info.telefono;
    document.getElementById('home-email').textContent = info.email;
    document.getElementById('home-hours').textContent = info.horario;
};
