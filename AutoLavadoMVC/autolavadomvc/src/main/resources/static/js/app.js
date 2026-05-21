// 1. Funciones de validación pura (siempre arriba del todo)
function esTelefonoValido(telefono, tipoTelefono) {
    const normalizado = telefono.replace(/[\s-]/g, "");
    if (tipoTelefono) {
        return /^\d{9}$/.test(normalizado); // Corregido a 9 dígitos para móvil español
    }
    return /^(?:\+|00)\d{7,15}$/.test(normalizado);
}

function esMatriculaValida(matricula, tipoMatricula) {
    const normalizada = matricula.replace(/[\s-]/g, "").toUpperCase();
    
    if (tipoMatricula) {
        // Formato moderno corregido: Bloquea CH/LL y permite consonantes posteriores
        const sistemaModerno = /^(?!.*(?:CH|LL))\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$/;
        const provincialConLetras = /^[A-Z]{1,2}\d{4}[A-Z]{1,2}$/;
        const provincialSoloNumeros = /^[A-Z]{1,2}\d{1,6}$/;

        return sistemaModerno.test(normalizada) || 
               provincialConLetras.test(normalizada) || 
               provincialSoloNumeros.test(normalizada);
    }
    
    return /^[A-Z0-9]{5,12}$/.test(normalizada);
}

// 2. Inicialización del DOM
document.addEventListener('DOMContentLoaded', () => {
    const telefonoRadios = document.querySelectorAll('input[name="tipoTelefono"]');
    const matriculaEsp = document.getElementById('tipoMatriculaEsp');
    const matriculaExt = document.getElementById('tipoMatriculaExt');
    const fechaInput = document.getElementById('fecha');
    const storageKey = 'autolavado.reserva.fecha';

    const telefonoInput = document.getElementById('telefono');
    const errorTelefonoDiv = document.getElementById('error-telefono');
    const matriculaInput = document.getElementById('matricula');
    const errorMatriculaDiv = document.getElementById('error-matricula');

    // !!! LA FUNCIÓN DEBE DECLARARSE AQUÍ, ANTES DE SER USADA !!!
    const sincronizarMatricula = () => {
        const telefonoInternacional = document.getElementById('tipoTelefonoInt')?.checked;
        if (!matriculaEsp || !matriculaExt) {
            return;
        }
        matriculaExt.checked = Boolean(telefonoInternacional);
        matriculaEsp.checked = !telefonoInternacional;
    };

    const restaurarFecha = () => {
        if (!fechaInput) return;
        if (!fechaInput.value) {
            const fechaGuardada = window.localStorage.getItem(storageKey);
            if (fechaGuardada) {
                fechaInput.value = fechaGuardada;
            }
        }
    };

    const guardarFecha = () => {
        if (fechaInput && fechaInput.value && fechaInput.value.trim() !== "") {
            window.localStorage.setItem(storageKey, fechaInput.value);
        }
    };

    // 3. Asignación de Eventos (Ahora que las funciones ya existen en memoria)
    telefonoRadios.forEach((radio) => {
        radio.addEventListener('change', sincronizarMatricula);
    });

    if (fechaInput) {
        fechaInput.addEventListener('change', guardarFecha);
        fechaInput.addEventListener('blur', guardarFecha);
    }

    const form = document.getElementById('reserva-form');
    if (form) {
        form.addEventListener('submit', (event) => {
            guardarFecha();

            errorTelefonoDiv.textContent = "";
            errorMatriculaDiv.textContent = "";
            let todoValido = true;

            if (telefonoInput) {
                const esEsp = document.getElementById('tipoTelefonoEsp')?.checked;
                if (!esTelefonoValido(telefonoInput.value, esEsp)) {
                    errorTelefonoDiv.textContent = esEsp 
                        ? "El teléfono móvil español debe tener 9 dígitos y empezar por 6 o 7." 
                        : "El teléfono extranjero debe incluir prefijo (+ o 00) seguido de 7 a 15 números.";
                    todoValido = false;
                }
            }

            if (matriculaInput) {
                const esEspMat = document.getElementById('tipoMatriculaEsp')?.checked;
                if (!esMatriculaValida(matriculaInput.value, esEspMat)) {
                    errorMatriculaDiv.textContent = esEspMat 
                        ? "La matrícula española debe tener 4 números y 3 letras (ej. 1234BBB)." 
                        : "La matrícula extranjera debe tener entre 5 y 12 caracteres.";
                    todoValido = false;
                }
            }

            if (!todoValido) {
                event.preventDefault();  
                event.stopPropagation(); 
                return false;            
            }
        });
    }

    // 4. Ejecución inicial al cargar la página
    sincronizarMatricula();
    restaurarFecha();
});