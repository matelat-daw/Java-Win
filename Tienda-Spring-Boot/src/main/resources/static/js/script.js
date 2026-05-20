function confirmarEliminacion(id, nombre) {
    const modalEl = document.getElementById('modalConfirmarEliminar');
    const nombreEl = document.getElementById('nombreProductoEliminar');
    const btnEliminar = document.getElementById('btnConfirmarEliminar');

    if (modalEl && nombreEl && btnEliminar) {
        // Actualizar datos del modal
        nombreEl.textContent = nombre;
        const base = (window.APP_BASE || '/').replace(/\/?$/, '/');
        btnEliminar.href = base + 'store/delete/' + id;

        // Mostrar el modal usando Bootstrap 5
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();
    } else {
        console.error("No se encontraron los elementos del modal de eliminación.");
    }
}
