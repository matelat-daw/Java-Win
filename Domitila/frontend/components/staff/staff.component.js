/**
 * StaffComponent.js - Componente de gestión de usuarios
 */

class StaffComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.currentPage = 0;
        this.pageSize = 10;
        this.staff = [];
        this.totalItems = 0;
        this.totalPages = 0;
        this.searchSurname = '';
        this.sortBy = 'surname1';
        this.sortDir = 'asc';
        this._searchDebounceId = null;
    }

    async init() {
        try {
// Verificar si el usuario está autenticado
            if (!AuthService.isAuthenticated()) {
App.getInstance().navigateTo('/login');
                return;
            }

            // Obtener el rol del usuario
            const staffRole = AuthService.getRole();
const isAdmin = staffRole === 'ADMIN';
            if (!isAdmin) {
                this.renderAccessDenied(staffRole);
                return;
            }

            // Guardar el rol en la instancia para usarlo en other methods
            this.staffRole = staffRole;
            this.isAdmin = isAdmin;

            // Mostrar spinner mientras se cargan los datos
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Cargando staff...</span>
                        </div>
                        <p class="mt-3">Cargando lista de staff...</p>
                    </div>
                `;
            }

            // Cargar usuarios
            await this.loadStaff();

            // Renderizar tabla
            this.renderAdminView();

            // Adjuntar event listeners para paginación
            this.attachPaginationListeners();
} catch (error) {
const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="alert alert-danger m-5">
                        <h4>Error al cargar staff</h4>
                        <p>${error.message || 'No se pudo cargar la lista de staff'}</p>
                        <button class="btn btn-primary" onclick="location.reload()">Recargar</button>
                    </div>
                `;
            }
        }
    }

    async loadStaff() {
        try {
const response = await StaffService.getStaff(this.currentPage, this.pageSize, {
                surname: this.searchSurname,
                sortBy: this.sortBy,
                sortDir: this.sortDir
            });
            
            if (response.success) {
                const pageData = response.data || {};
                const currentStaff = AuthService.getStaffSession();
                const currentStaffId = currentStaff?.id ?? null;
                const currentStaffEmail = currentStaff?.email ?? null;
                const rawItems = Array.isArray(pageData.items) ? pageData.items : [];
                const currentStaffIncluded = rawItems.some(staff => staff?.id === currentStaffId || staff?.email === currentStaffEmail);

                this.staff = rawItems.filter(staff => staff?.id !== currentStaffId && staff?.email !== currentStaffEmail);
                this.totalItems = Math.max(0, (pageData.totalItems ?? this.staff.length) - (currentStaffIncluded ? 1 : 0));
                this.totalPages = pageData.totalPages ?? (this.totalItems > 0 ? 1 : 0);
} else {
                throw new Error(response.message || 'Error al obtener staff');
            }
        } catch (error) {
throw error;
        }
    }

    renderAdminView() {
        const currentStaff = AuthService.getStaffSession();
        const staffName = currentStaff ? `${currentStaff.name} ${currentStaff.surname1}` : 'Usuario';
        const safeSearchSurnameValue = StaffComponent.escapeHtml(this.searchSurname);
        
        const html = `
            <div class="container-fluid py-5">
                <!-- Saludo Usuario -->
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="fas fa-user-check me-2"></i>
                            <strong>¡Bienvenido Staff: ${staffName}!</strong>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </div>
                </div>

                <!-- Header -->
                <div class="row mb-4">
                    <div class="col-12 d-flex justify-content-between align-items-center">
                        <h1 class="display-5 fw-bold mb-2">
                            <i class="fas fa-user me-3"></i>Lista de Staff
                        </h1>
                        <button type="button" class="btn btn-primary" id="addUserBtn">
                            <i class="fas fa-user-plus me-2"></i>Agregar staff
                        </button>
                    </div>
                    <div class="col-12">
                        <p class="text-muted">Total de staff: <strong>${this.totalItems}</strong></p>
                    </div>
                </div>

                <div class="row mb-4">
                    <div class="col-12">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                <div class="row g-3 align-items-end">
                                    <div class="col-md-6">
                                        <label class="form-label">Buscar por apellido</label>
                                        <input type="text" class="form-control" id="surnameSearchInput" value="${safeSearchSurnameValue}" placeholder="Ej: Pérez">
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label">Orden por apellido</label>
                                        <select class="form-select" id="surnameSortDir">
                                            <option value="asc" ${this.sortDir === 'asc' ? 'selected' : ''}>A → Z</option>
                                            <option value="desc" ${this.sortDir === 'desc' ? 'selected' : ''}>Z → A</option>
                                        </select>
                                    </div>
                                    <div class="col-md-3 d-grid">
                                        <button type="button" class="btn btn-outline-secondary" id="clearSurnameSearchBtn" ${this.searchSurname ? '' : 'disabled'}>
                                            Limpiar búsqueda
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Tabla de usuarios -->
                <div class="row">
                    <div class="col-12">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                ${this.staff.length > 0 ? this.renderTable() : this.renderEmpty()}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Paginación -->
                ${this.totalPages > 1 ? this.renderPagination() : ''}
            </div>
        `;

        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = html;
            
            // Adjuntar event listeners después de renderizar
            this.attachTableListeners();
        }
    }

    renderTable() {
        const rows = this.staff.map(staff => `
            <tr>
                <td>
                    ${staff.profileImg ? 
                        `<img src="${API_CONFIG.BASE_URL}/images/${staff.profileImg}" alt="${staff.nick}" class="img-thumbnail rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">` :
                        `<div class="bg-secondary rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 40px; height: 40px;"><i class="fas fa-user text-white"></i></div>`
                    }
                </td>
                <td>
                    <strong>${staff.nick || 'N/A'}</strong><br>
                    <small class="text-muted">${staff.name} ${staff.surname1}</small>
                </td>
                <td>${StaffComponent.renderEmailCell(staff.email)}</td>
                <td>${StaffComponent.renderPhoneCell(staff.phone)}</td>
                <td>
                    <span class="badge ${this.getRoleBadgeClass(staff.role) || 'bg-secondary'}">
                        ${staff.role || 'USER'}
                    </span>
                </td>
                <td>
                    <small class="text-muted">${new Date(staff.createdAt).toLocaleDateString()}</small>
                </td>
                <td>
                    <div class="btn-group btn-group-sm" role="group">
                        <button class="btn btn-outline-primary btn-sm view-staff-btn" data-staff-id="${staff.id}">
                            <i class="fas fa-eye"></i> Ver
                        </button>
                        ${this.isAdmin ? `
                            <button class="btn btn-outline-success btn-sm edit-staff-btn" data-staff-id="${staff.id}">
                                <i class="fas fa-pen"></i> Editar
                            </button>
                            <button class="btn btn-outline-danger btn-sm delete-staff-btn" data-staff-id="${staff.id}" data-staff-nick="${staff.nick}">
                                <i class="fas fa-trash"></i> Eliminar
                            </button>
                        ` : `
                            <button class="btn btn-outline-secondary btn-sm" disabled title="Solo lectura">
                                <i class="fas fa-lock"></i> Solo lectura
                            </button>
                        `}
                    </div>
                </td>
            </tr>
        `).join('');

        return `
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th style="width: 50px;">Foto</th>
                            <th>Staff</th>
                            <th>Email</th>
                            <th>Teléfono</th>
                            <th>Rol</th>
                            <th>Registro</th>
                            <th style="width: 220px;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        `;
    }

    static escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    static normalizePhoneForWhatsApp(phone) {
        const raw = String(phone ?? '').trim();
        if (!raw) return null;

        let digits = raw.replace(/[^\d+]/g, '');
        if (digits.startsWith('+')) digits = digits.substring(1);
        if (digits.startsWith('00')) digits = digits.substring(2);
        digits = digits.replace(/\D/g, '');

        if (!digits) return null;
        if (digits.length === 9) return `34${digits}`;
        return digits;
    }

    static renderEmailCell(email) {
        const safeEmail = StaffComponent.escapeHtml(email);
        if (!safeEmail) return '-';
        const href = `mailto:${safeEmail}`;
        return `<a href="${href}" class="text-decoration-none">${safeEmail}</a>`;
    }

    static renderPhoneCell(phone) {
        const safePhone = StaffComponent.escapeHtml(phone);
        if (!safePhone) return '-';

        const normalized = StaffComponent.normalizePhoneForWhatsApp(phone);
        if (!normalized) return safePhone;

        const waUrl = `https://wa.me/${normalized}`;
        return `
            <a href="${waUrl}" target="_blank" rel="noopener noreferrer" class="text-decoration-none">
                <i class="fab fa-whatsapp me-1 text-success"></i>${safePhone}
            </a>
        `.trim();
    }

    renderEmpty() {
        return `
            <div class="text-center py-5">
                <i class="fas fa-inbox display-1 text-secondary mb-3"></i>
                <h4>No hay staff</h4>
                <p class="text-muted">No se encontraron usuarios en el sistema.</p>
            </div>
        `;
    }

    renderPagination() {
        const pages = [];
        
        // Boton anterior
        pages.push(`
            <li class="page-item ${this.currentPage === 0 ? 'disabled' : ''}">
                <button type="button" class="page-link btn btn-link text-decoration-none" onclick="StaffComponent.previousPage()" data-page="${this.currentPage - 1}">
                    Anterior
                </button>
            </li>
        `);

        // Números de página
        for (let i = 0; i < this.totalPages; i++) {
            pages.push(`
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <button type="button" class="page-link btn btn-link text-decoration-none" onclick="StaffComponent.goToPage(${i})" data-page="${i}">
                        ${i + 1}
                    </button>
                </li>
            `);
        }

        // Boton siguiente
        pages.push(`
            <li class="page-item ${this.currentPage === this.totalPages - 1 ? 'disabled' : ''}">
                <button type="button" class="page-link btn btn-link text-decoration-none" onclick="StaffComponent.nextPage()" data-page="${this.currentPage + 1}">
                    Siguiente
                </button>
            </li>
        `);

        return `
            <div class="row mt-5">
                <div class="col-12 d-flex justify-content-center">
                    <nav>
                        <ul class="pagination">
                            ${pages.join('')}
                        </ul>
                    </nav>
                </div>
            </div>
        `;
    }

    renderAccessDenied(staffRole) {
        const roleBadgeClass = this.getRoleBadgeClass(staffRole);
        const html = `
            <div class="container py-5">
                <div class="row mb-5">
                    <div class="col-12">
                        <h1 class="display-5 fw-bold mb-2"><i class="fas fa-user me-3"></i>Lista de Staff</h1>
                        <p class="text-muted">Administra el staff del sistema</p>
                    </div>
                </div>
                <div class="row justify-content-center">
                    <div class="col-md-8">
                        <div class="card shadow-sm border-warning">
                            <div class="card-body text-center py-5">
                                <div style="font-size: 80px; margin-bottom: 20px;">🔐</div>
                                <h2 class="card-title fw-bold mb-3 text-danger">Acceso Denegado</h2>
                                <p class="lead mb-3">No tienes permisos para acceder a esta sección</p>
                                <p class="text-muted mb-4">
                                    <i class="fas fa-info-circle me-2"></i>
                                    <strong>Esta funcionalidad solo está disponible para staff con rol ADMIN</strong>
                                </p>
                                <div class="alert alert-info" role="alert">
                                    <i class="fas fa-user-circle me-2"></i>
                                    <strong>Tu rol actual:</strong> <span class="badge ${roleBadgeClass} ms-2">${staffRole || 'No especificado'}</span>
                                </div>
                                <a href="/dashboard" class="btn btn-primary mt-3">
                                    <i class="fas fa-arrow-left me-2"></i>Volver al Dashboard
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = html;
        }
    }

    attachTableListeners() {
        const addUserBtn = document.getElementById('addUserBtn');
        if (addUserBtn && addUserBtn.dataset.listenersAttached !== 'true') {
            addUserBtn.dataset.listenersAttached = 'true';
            addUserBtn.addEventListener('click', () => {
                StaffComponent.showCreateUserModal();
            });
        }

        const surnameSearchInput = document.getElementById('surnameSearchInput');
        if (surnameSearchInput && surnameSearchInput.dataset.listenersAttached !== 'true') {
            surnameSearchInput.dataset.listenersAttached = 'true';
            surnameSearchInput.addEventListener('input', (e) => {
                const instance = window.StaffComponentInstance;
                if (!instance) return;

                const nextValue = String(e.target.value || '');
                instance.searchSurname = nextValue;
                instance.currentPage = 0;

                if (instance._searchDebounceId) {
                    clearTimeout(instance._searchDebounceId);
                }
                instance._searchDebounceId = setTimeout(async () => {
                    await instance.loadStaff();
                    instance.renderAdminView();
                    instance.attachPaginationListeners();
                }, 300);
            });
        }

        const surnameSortDir = document.getElementById('surnameSortDir');
        if (surnameSortDir && surnameSortDir.dataset.listenersAttached !== 'true') {
            surnameSortDir.dataset.listenersAttached = 'true';
            surnameSortDir.addEventListener('change', async (e) => {
                const instance = window.StaffComponentInstance;
                if (!instance) return;
                instance.sortBy = 'surname1';
                instance.sortDir = (String(e.target.value) === 'desc') ? 'desc' : 'asc';
                instance.currentPage = 0;
                await instance.loadStaff();
                instance.renderAdminView();
                instance.attachPaginationListeners();
            });
        }

        const clearSurnameSearchBtn = document.getElementById('clearSurnameSearchBtn');
        if (clearSurnameSearchBtn && clearSurnameSearchBtn.dataset.listenersAttached !== 'true') {
            clearSurnameSearchBtn.dataset.listenersAttached = 'true';
            clearSurnameSearchBtn.addEventListener('click', async () => {
                const instance = window.StaffComponentInstance;
                if (!instance) return;
                instance.searchSurname = '';
                instance.currentPage = 0;
                await instance.loadStaff();
                instance.renderAdminView();
                instance.attachPaginationListeners();
            });
        }

        // Listeners para botones "Ver" (detalles del usuario)
        document.querySelectorAll('.view-staff-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const staffId = btn.dataset.staffId;
                if (staffId) {
                    App.getInstance().navigateTo('/staff/' + staffId);
                }
            });
        });

        document.querySelectorAll('.edit-staff-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const staffId = btn.dataset.staffId;
                if (!staffId) return;
                try {
                    const res = await StaffService.getStaffById(staffId);
                    const staff = res?.data || res?.staff || null;
                    if (!staff) {
                        StaffComponent.showErrorNotification('No se pudo cargar el usuario');
                        return;
                    }
                    StaffComponent.showEditUserModal(staff);
                } catch (e) {
                    StaffComponent.showErrorNotification(e.message || 'Error al cargar usuario');
                }
            });
        });

        // Listeners para botones "Eliminar"
        document.querySelectorAll('.delete-staff-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const staffId = btn.dataset.staffId;
                const staffNick = btn.dataset.staffNick;
                if (staffId) {
                    StaffComponent.confirmDelete(staffId, staffNick);
                }
            });
        });
    }

    attachPaginationListeners() {
        // Los listeners ya están adjuntos en los botones
    }

    getRoleBadgeClass(role) {
        const roleBadgeMap = {
            'ADMIN': 'bg-danger',
            'USER': 'bg-info',
            'MODERATOR': 'bg-warning',
            'GUEST': 'bg-secondary'
        };
        return roleBadgeMap[role] || 'bg-secondary';
    }

    /**
     * Confirma y elimina un usuario
     */
    static async confirmDelete(staffId, staffNick) {
        // Crear modal de confirmación
        const modalHtml = `
            <div class="modal fade" id="deleteUserModal" tabindex="-1" aria-labelledby="deleteUserLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header bg-danger text-white">
                            <h5 class="modal-title" id="deleteUserLabel">
                                <i class="fas fa-trash me-2"></i>Confirmar Eliminación
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <div class="alert alert-warning mb-3" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>
                                <strong>¡Advertencia!</strong> Esta acción es irreversible.
                            </div>
                            <p>¿Estás seguro de que deseas eliminar al usuario <strong>${staffNick}</strong>?</p>
                            <p class="text-muted"><small>Una vez eliminado, no se puede recuperar.</small></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-danger" onclick="StaffComponent.deleteUserConfirmed(${staffId})">
                                <i class="fas fa-trash me-2"></i>Sí, eliminar
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Remover modal anterior si existe
        const existingModal = document.getElementById('deleteUserModal');
        if (existingModal) {
            existingModal.remove();
        }

        // Agregar nuevo modal
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // Mostrar modal
        const modal = new bootstrap.Modal(document.getElementById('deleteUserModal'));
        modal.show();
    }

    static showCreateUserModal() {
        const modalHtml = `
            <div class="modal fade" id="createUserModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header bg-primary text-white">
                            <h5 class="modal-title">
                                <i class="fas fa-user-plus me-2"></i>Agregar staff
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <form id="createUserForm">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Nick</label>
                                        <input class="form-control" name="nick" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Email</label>
                                        <input class="form-control" type="email" name="email" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Nombre</label>
                                        <input class="form-control" name="name" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Primer apellido</label>
                                        <input class="form-control" name="surname1" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Segundo apellido</label>
                                        <input class="form-control" name="surname2">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Teléfono</label>
                                        <input class="form-control" name="phone" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Género</label>
                                        <select class="form-select" name="gender" required>
                                            <option value="">Seleccionar</option>
                                            <option value="MALE">Male</option>
                                            <option value="FEMALE">Female</option>
                                            <option value="OTHER">Other</option>
                                        </select>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Fecha de nacimiento</label>
                                        <input class="form-control" type="date" name="bday">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Contraseña</label>
                                        <input class="form-control" type="password" name="password" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Foto de perfil</label>
                                        <input class="form-control" type="file" name="profilePicture" accept="image/*">
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-primary" id="confirmCreateUserBtn">
                                <i class="fas fa-save me-2"></i>Crear
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        const existingModal = document.getElementById('createUserModal');
        if (existingModal) {
            existingModal.remove();
        }

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        const modalEl = document.getElementById('createUserModal');
        const modal = new bootstrap.Modal(modalEl);
        modal.show();

        const confirmBtn = document.getElementById('confirmCreateUserBtn');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => StaffComponent.createUserConfirmed(), { once: true });
        }
    }

    static async createUserConfirmed() {
        const form = document.getElementById('createUserForm');
        const btn = document.getElementById('confirmCreateUserBtn');
        if (!form || !btn) return;

        const fd = new FormData(form);
        const staffData = {
            nick: String(fd.get('nick') || '').trim(),
            name: String(fd.get('name') || '').trim(),
            surname1: String(fd.get('surname1') || '').trim(),
            surname2: String(fd.get('surname2') || '').trim(),
            email: String(fd.get('email') || '').trim(),
            phone: String(fd.get('phone') || '').trim(),
            password: String(fd.get('password') || ''),
            gender: String(fd.get('gender') || ''),
            bday: fd.get('bday') ? String(fd.get('bday')) : null
        };

        if (!staffData.nick || !staffData.email || !staffData.name || !staffData.surname1 || !staffData.phone || !staffData.password || !staffData.gender) {
            StaffComponent.showErrorNotification('Completa los campos obligatorios');
            return;
        }

        btn.disabled = true;
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creando...';

        try {
            const staff = new Staff(staffData);
            const profilePicture = fd.get('profilePicture');
            const response = await StaffService.register(staff, profilePicture);
            if (!response?.success) {
                throw new Error(response?.message || 'No se pudo crear el usuario');
            }

            const modal = bootstrap.Modal.getInstance(document.getElementById('createUserModal'));
            if (modal) {
                modal.hide();
            }
            StaffComponent.showSuccessNotification('Staff creado. Revisa el correo para verificar la cuenta.');

            const instance = window.StaffComponentInstance;
            if (instance) {
                instance.currentPage = 0;
                await instance.loadStaff();
                instance.renderAdminView();
                instance.attachPaginationListeners();
            }
        } catch (e) {
            StaffComponent.showErrorNotification(e.message || 'Error al crear usuario');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalHtml;
        }
    }

    static showEditUserModal(staff) {
        const modalHtml = `
            <div class="modal fade" id="editUserModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header bg-success text-white">
                            <h5 class="modal-title">
                                <i class="fas fa-pen me-2"></i>Editar usuario
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <form id="editUserForm" data-staff-id="${staff.id}">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Nick</label>
                                        <input class="form-control" value="${staff.nick || ''}" disabled>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Email</label>
                                        <input class="form-control" value="${staff.email || ''}" disabled>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Nombre</label>
                                        <input class="form-control" name="name" value="${staff.name || ''}" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Primer apellido</label>
                                        <input class="form-control" name="surname1" value="${staff.surname1 || ''}" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Segundo apellido</label>
                                        <input class="form-control" name="surname2" value="${staff.surname2 || ''}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Teléfono</label>
                                        <input class="form-control" name="phone" value="${staff.phone || ''}" required>
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-success" id="confirmEditUserBtn">
                                <i class="fas fa-save me-2"></i>Guardar
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        const existingModal = document.getElementById('editUserModal');
        if (existingModal) {
            existingModal.remove();
        }

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        const modalEl = document.getElementById('editUserModal');
        const modal = new bootstrap.Modal(modalEl);
        modal.show();

        const confirmBtn = document.getElementById('confirmEditUserBtn');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => StaffComponent.updateUserConfirmed(), { once: true });
        }
    }

    static async updateUserConfirmed() {
        const form = document.getElementById('editUserForm');
        const btn = document.getElementById('confirmEditUserBtn');
        if (!form || !btn) return;

        const staffId = form.dataset.staffId;
        const fd = new FormData(form);
        const payload = {
            name: String(fd.get('name') || '').trim(),
            surname1: String(fd.get('surname1') || '').trim(),
            surname2: String(fd.get('surname2') || '').trim(),
            phone: String(fd.get('phone') || '').trim()
        };

        if (!staffId || !payload.name || !payload.surname1 || !payload.phone) {
            StaffComponent.showErrorNotification('Completa los campos obligatorios');
            return;
        }

        btn.disabled = true;
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Guardando...';

        try {
            const response = await StaffService.updateStaff(staffId, payload);
            if (!response?.success) {
                throw new Error(response?.message || 'No se pudo actualizar el usuario');
            }

            const modal = bootstrap.Modal.getInstance(document.getElementById('editUserModal'));
            if (modal) {
                modal.hide();
            }
            StaffComponent.showSuccessNotification('Staff actualizado exitosamente');

            const instance = window.StaffComponentInstance;
            if (instance) {
                await instance.loadStaff();
                instance.renderAdminView();
                instance.attachPaginationListeners();
            }
        } catch (e) {
            StaffComponent.showErrorNotification(e.message || 'Error al actualizar usuario');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalHtml;
        }
    }

    /**
     * Ejecuta la eliminación del usuario confirmada
     */
    static async deleteUserConfirmed(staffId) {
        try {
            const deleteBtn = document.querySelector('.btn-danger[onclick*="deleteUserConfirmed"]');
            if (deleteBtn) {
                deleteBtn.disabled = true;
                deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Eliminando...';
            }
const response = await StaffService.deleteStaff(staffId);

            if (response.success) {
// Cerrar modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('deleteUserModal'));
                if (modal) {
                    modal.hide();
                }

                // Mostrar notificación de éxito
                this.showSuccessNotification('Staff eliminado exitosamente');

                // Recargar lista de staff de forma más simple
setTimeout(async () => {
                    try {
                        // Acceder a la instancia global
                        const instance = window.StaffComponentInstance;
                        
                        if (instance) {
// Resetear a primera página
                            instance.currentPage = 0;
                            await instance.loadStaff();
                            instance.renderAdminView();
                            instance.attachPaginationListeners();
                        } else {
// Si no hay instancia, navegar a /staffs para forzar recarga
                            App.getInstance().navigateTo('/staff');
                        }
                    } catch (error) {
// Fallback: Navegar a /staffs
                        App.getInstance().navigateTo('/staff');
                    }
                }, 800);

            } else {
this.showErrorNotification(response.message || 'Error al eliminar usuario');
                
                // Habilitar botón nuevamente
                if (deleteBtn) {
                    deleteBtn.disabled = false;
                    deleteBtn.innerHTML = '<i class="fas fa-trash me-2"></i>Sí, eliminar';
                }
            }
        } catch (error) {
this.showErrorNotification('Error al eliminar usuario: ' + error.message);
            
            const deleteBtn = document.querySelector('.btn-danger[onclick*="deleteUserConfirmed"]');
            if (deleteBtn) {
                deleteBtn.disabled = false;
                deleteBtn.innerHTML = '<i class="fas fa-trash me-2"></i>Sí, eliminar';
            }
        }
    }

    /**
     * Muestra notificación de éxito
     */
    static showSuccessNotification(message) {
        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show position-fixed" role="alert" 
                 style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
                <i class="fas fa-check-circle me-2"></i>
                <strong>Éxito!</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', alertHtml);

        // Auto-hide después de 3 segundos
        setTimeout(() => {
            const alerts = document.querySelectorAll('.alert-success.position-fixed');
            alerts.forEach(alert => alert.remove());
        }, 3000);
    }

    /**
     * Muestra notificación de error
     */
    static showErrorNotification(message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show position-fixed" role="alert" 
                 style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
                <i class="fas fa-exclamation-circle me-2"></i>
                <strong>Error!</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', alertHtml);

        // Auto-hide después de 5 segundos
        setTimeout(() => {
            const alerts = document.querySelectorAll('.alert-danger.position-fixed');
            alerts.forEach(alert => alert.remove());
        }, 5000);
    }

    // Métodos de paginación estáticos
    static async goToPage(page) {
        const instance = StaffComponentInstance;
        if (instance) {
            instance.currentPage = page;
            await instance.loadStaff();
            instance.renderAdminView();
            instance.attachPaginationListeners();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }

    static async nextPage() {
        const instance = StaffComponentInstance;
        if (instance && instance.currentPage < instance.totalPages - 1) {
            await this.goToPage(instance.currentPage + 1);
        }
    }

    static async previousPage() {
        const instance = StaffComponentInstance;
        if (instance && instance.currentPage > 0) {
            await this.goToPage(instance.currentPage - 1);
        }
    }
}

// Instancia global para acceder desde métodos estáticos
let StaffComponentInstance = null;

// Exponer globalmente para la verificación de carga
window.StaffComponent = StaffComponent;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') {
    AppScripts.register('staff');
}


