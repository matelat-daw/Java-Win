// 1. Funciones de validación pura (siempre arriba del todo)
function esTelefonoValido(telefono, tipoTelefono) {
    const normalizado = telefono.replace(/[\s-]/g, "");
    if (tipoTelefono) {
        return /^\d{9}$/.test(normalizado); // 9 dígitos para móvil español
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
    const form = document.getElementById('reserva-form');
    if (!form) return;

    const telefonoRadios = document.querySelectorAll('input[name="tipoTelefono"]');
    const matriculaEsp = document.getElementById('tipoMatriculaEsp');
    const matriculaExt = document.getElementById('tipoMatriculaExt');
    const fechaInput = document.getElementById('fecha');
    const storageKey = 'autolavado.reserva.fecha';
    const nombreInput = document.getElementById('nombreCliente');
    const telefonoInput = document.getElementById('telefono');
    const errorTelefonoDiv = document.getElementById('error-telefono');
    const matriculaInput = document.getElementById('matricula');
    const errorMatriculaDiv = document.getElementById('error-matricula');
    const errorFechaDiv = document.getElementById('error-fecha');
    const tipoLavadoSelect = document.getElementById('tipoLavado');
    const horaInput = document.getElementById('hora');

    // Sincronizar automáticamente los radios de matrícula con los de teléfono
    const sincronizarMatricula = () => {
        const telefonoInternacional = document.getElementById('tipoTelefonoInt')?.checked;
        if (!matriculaEsp || !matriculaExt) return;
        
        matriculaExt.checked = Boolean(telefonoInternacional);
        matriculaEsp.checked = !telefonoInternacional;
        
        // Revalidar inmediatamente teléfono y matrícula al cambiar el tipo
        validarTelefono();
        validarMatricula();
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

    // Auxiliar para aplicar estilos visuales de Bootstrap
    const marcarInput = (inputElement, esValido) => {
        if (!inputElement) return;
        if (esValido) {
            inputElement.classList.remove('is-invalid');
            inputElement.classList.add('is-valid');
        } else {
            inputElement.classList.remove('is-valid');
            inputElement.classList.add('is-invalid');
        }
    };

    // --- FUNCIONES DE VALIDACIÓN INDIVIDUAL EN TIEMPO REAL ---

    const validarNombre = () => {
        if (!nombreInput) return true;
        const valido = nombreInput.value.trim() !== "";
        marcarInput(nombreInput, valido);
        return valido;
    };

    const validarLavado = () => {
        if (!tipoLavadoSelect) return true;
        const valido = tipoLavadoSelect.value !== "";
        marcarInput(tipoLavadoSelect, valido);
        return valido;
    };

    const validarHora = () => {
        if (!horaInput) return true;
        const valido = horaInput.value !== "";
        marcarInput(horaInput, valido);
        return valido;
    };

    const validarFecha = () => {
        if (!fechaInput) return true;
        let valido = fechaInput.value !== "";
        if (valido) {
            const hoy = new Date();
            hoy.setHours(0, 0, 0, 0);
            const fechaSeleccionada = new Date(fechaInput.value);
            fechaSeleccionada.setHours(0, 0, 0, 0);

            if (fechaSeleccionada < hoy) {
                valido = false;
                if (errorFechaDiv) errorFechaDiv.textContent = "La fecha no puede ser anterior a hoy.";
            }
        } else {
            if (errorFechaDiv) errorFechaDiv.textContent = "Selecciona una fecha válida.";
        }
        marcarInput(fechaInput, valido);
        return valido;
    };

    const validarTelefono = () => {
        if (!telefonoInput) return true;
        const esEsp = document.getElementById('tipoTelefonoEsp')?.checked;
        const valido = esTelefonoValido(telefonoInput.value, esEsp);
        marcarInput(telefonoInput, valido);
        
        if (!valido && errorTelefonoDiv) {
            errorTelefonoDiv.textContent = esEsp 
                ? "El teléfono móvil español debe tener 9 dígitos." 
                : "El teléfono extranjero debe incluir prefijo (+ o 00) seguido de 7 a 15 números.";
        } else if (valido && errorTelefonoDiv) {
            errorTelefonoDiv.textContent = "";
        }
        return valido;
    };

    const validarMatricula = () => {
        if (!matriculaInput) return true;
        const esEspMat = document.getElementById('tipoMatriculaEsp')?.checked;
        const valido = esMatriculaValida(matriculaInput.value, esEspMat);
        marcarInput(matriculaInput, valido);

        if (!valido && errorMatriculaDiv) {
            errorMatriculaDiv.textContent = esEspMat 
                ? "La matrícula española debe tener 4 números y 3 letras (ej. 1234BBB) o formato provincial válido." 
                : "La matrícula extranjera debe tener entre 5 y 12 caracteres alfanuméricos.";
        } else if (valido && errorMatriculaDiv) {
            errorMatriculaDiv.textContent = "";
        }
        return valido;
    };

    // 3. Asignación de Eventos en tiempo real (`input` y `change`)
    nombreInput?.addEventListener('input', validarNombre);
    tipoLavadoSelect?.addEventListener('change', validarLavado);
    horaInput?.addEventListener('input', validarHora);
    
    if (fechaInput) {
        fechaInput.addEventListener('change', () => {
            validarFecha();
            guardarFecha();
        });
        fechaInput.addEventListener('blur', guardarFecha);
    }
    
    telefonoInput?.addEventListener('input', validarTelefono);
    matriculaInput?.addEventListener('input', validarMatricula);

    telefonoRadios.forEach((radio) => {
        radio.addEventListener('change', sincronizarMatricula);
    });

    // 4. Validación global en el Submit
    form.addEventListener('submit', (event) => {
        guardarFecha();

        // Ejecutamos todas las validaciones para forzar el feedback visual
        const vNombre = validarNombre();
        const vLavado = validarLavado();
        const vHora = validarHora();
        const vFecha = validarFecha();
        const vTelefono = validarTelefono();
        const vMatricula = validarMatricula();

        const todoValido = vNombre && vLavado && vHora && vFecha && vTelefono && vMatricula;

        if (!todoValido) {
            event.preventDefault();  
            event.stopPropagation(); 
            return false;            
        }
    });

    // 5. Ejecución inicial al cargar la página
    sincronizarMatricula();
    restaurarFecha();
});