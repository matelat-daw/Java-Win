/* ═══════════════════════════════════════════════════════════════
 app.js — Funciones compartidas de TiendaTechSpring
 Todas las páginas HTML importan este archivo.
═══════════════════════════════════════════════════════════════════ */

/* URL base de la API REST de Spring Boot.
 Si cambias el puerto en application.properties, cámbialo aquí también. */
const API_BASE = "http://localhost:8085";
/* ───────────────────────────────────────────────────────────────────
 obtenerProductos()
 Llama a GET /productos y devuelve la lista de productos como array.
 Devuelve una Promesa que resuelve con el array de productos.
─────────────────────────────────────────────────────────────────── */
async function obtenerProductos() {
    const respuesta = await fetch(`${API_BASE}/api/productos`);
 // TODO 5: comprueba si la respuesta fue correcta.
 // Si respuesta.ok es false, lanza un error con: throw new Error("Error al cargar productos")
    if (!respuesta.ok) {
        throw new Error("Error al cargar productos");
    }
    const datos = await respuesta.json();
    return datos;
}
/* ───────────────────────────────────────────────────────────────────
 anadirProducto(nombre, precio, categoria, stock, disponible)
 Llama a GET /productos/añadir con los parámetros indicados.
 Devuelve el mensaje de confirmación como texto.
─────────────────────────────────────────────────────────────────── */
async function anadirProducto(nombre, precio, categoria, stock, disponible, destacado) {
 // Construimos la URL con los parámetros.
 // encodeURIComponent convierte caracteres especiales (espacios, tildes) a formato URL seguro.
    const url = `${API_BASE}/api/productos/a%C3%B1adir` +
        `?nombre=${encodeURIComponent(nombre)}` +
        `&precio=${encodeURIComponent(precio)}` +
        `&categoria=${encodeURIComponent(categoria)}` +
        `&stock=${encodeURIComponent(stock)}` +
        `&disponible=${encodeURIComponent(disponible)}` +
        `&destacado=${encodeURIComponent(destacado)}`;
    // TODO 6: usa fetch() para llamar a la url construida arriba.
    // Guarda el resultado en una variable llamada respuesta.
    // Pista: const respuesta = await fetch(url);
    const respuesta = await fetch(url);
    // TODO 7: comprueba si la respuesta fue correcta (igual que en TODO 5).
    if (!respuesta.ok) {
        throw new Error("Error al añadir producto");
    }
    // TODO 8: lee el cuerpo de la respuesta como texto y devuélvelo.
    // Pista: const mensaje = await respuesta.text();
    // return mensaje;
    const mensaje = await respuesta.text();
    return mensaje;
}

/* ───────────────────────────────────────────────────────────────────
obtenerInfo()
 Llama a GET /api/productos/info y devuelve el texto informativo.
─────────────────────────────────────────────────────────────────── */
async function obtenerInfo() {
    // TODO 9: implementa esta función igual que obtenerProductos()
    // pero llamando a /api/productos/info y devolviendo el texto con .text() en lugar de .json()
    const respuesta = await fetch(`${API_BASE}/api/resumen`);
    if (!respuesta.ok) {
        throw new Error("Error al cargar información");
    }
    const mensaje = await respuesta.text();
    return mensaje;
}
/* ───────────────────────────────────────────────────────────────────
 mostrarMensaje(elementoId, texto, esError)
 Muestra un mensaje en el elemento indicado.
 Si esError es true, usa la clase mensaje-error. Si no, mensaje-exito.
─────────────────────────────────────────────────────────────────── */
function mostrarMensaje(elementoId, texto, esError = false) {
    const el = document.getElementById(elementoId);
    if (!el) return;
    el.textContent = texto;
    el.className = esError ? "mensaje-error" : "mensaje-exito";
    el.style.display = "block";
    // Ocultar el mensaje después de 4 segundos
    setTimeout(() => { el.style.display = "none"; }, 4000);
}

/* ───────────────────────────────────────────────────────────────────
 formatearPrecio(precio)
 Formatea un número a dos decimales con formato: "X,XX €"
 Ejemplo: formatearPrecio(29.5) devuelve "29,50 €"
─────────────────────────────────────────────────────────────────── */
function formatearPrecio(precio) {
    return parseFloat(precio).toLocaleString('es-ES', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }) + ' €';
}