/**
 * UsersComponent.js - Componente de gestión de usuarios
 */

class UsersComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.currentPage = 0;
        this.pageSize = 10;
        this.users = [];
        this.totalItems = 0;
        this.totalPages = 0;
    }

    async init() {
        try {
// Verificar si el usuario está autenticado
            if (!AuthService.isAuthenticated()) {
App.getInstance().navigateTo('/login');
                return;
            }

            // Obtener el rol del usuario
            const userRole = AuthService.getRole();
const isAdmin = userRole === 'ADMIN';
            if (!isAdmin) {
                this.renderAccessDenied(userRole);
                return;
            }

            // Guardar el rol en la instancia para usarlo en other methods
            this.userRole = userRole;
            this.isAdmin = isAdmin;

            // Mostrar spinner mientras se cargan los datos
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Cargando usuarios...</span>
                        </div>
                        <p class="mt-3">Cargando lista de usuarios...</p>
                    </div>
                `;
            }

            // Cargar usuarios
            await this.loadUsers();

            // Renderizar tabla
            this.renderAdminView();

            // Adjuntar event listeners para paginación
            this.attachPaginationListeners();
} catch (error) {
const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="alert alert-danger m-5">
                        <h4>Error al cargar usuarios</h4>
                        <p>${error.message || 'No se pudo cargar la lista de usuarios'}</p>
                        <button class="btn btn-primary" onclick="location.reload()">Recargar</button>
                    </div>
                `;
            }
        }
    }

    async loadUsers() {
        try {
// #region debug-point A:users-request
            fetch("http://127.0.0.1:7777/event",{method:"POST",mode:"no-cors",headers:{"Content-Type":"text/plain;charset=UTF-8"},body:JSON.stringify({sessionId:"users-list-empty",runId:"pre-fix",hypothesisId:"A",location:"frontend/components/users/users.component.js:75",msg:"[DEBUG] Solicitud de usuarios iniciada",data:{page:this.currentPage,size:this.pageSize,currentUser:AuthService?.getUserSession?.()?.email||null,currentRole:AuthService?.getRole?.()||null},ts:Date.now()})}).catch(()=>{});
// #endregion
const response = await UserService.getUsers(this.currentPage, this.pageSize);
// #region debug-point C:users-response-shape
            fetch("http://127.0.0.1:7777/event",{method:"POST",mode:"no-cors",headers:{"Content-Type":"text/plain;charset=UTF-8"},body:JSON.stringify({sessionId:"users-list-empty",runId:"pre-fix",hypothesisId:"C",location:"frontend/components/users/users.component.js:77",msg:"[DEBUG] Respuesta de usuarios recibida",data:{success:response?.success||false,topLevelKeys:response?Object.keys(response):[],dataKeys:response?.data&&typeof response.data==="object"?Object.keys(response.data):[],responseUsersLength:Array.isArray(response?.users)?response.users.length:null,responseDataItemsLength:Array.isArray(response?.data?.items)?response.data.items.length:null,responseDataTotalItems:response?.data?.totalItems??null,responseMessage:response?.message||null},ts:Date.now()})}).catch(()=>{});
// #endregion
            
            if (response.success) {
                const pageData = response.data || {};
                const currentUser = AuthService.getUserSession();
                const currentUserId = currentUser?.id ?? null;
                const currentUserEmail = currentUser?.email ?? null;
                const rawItems = Array.isArray(pageData.items) ? pageData.items : [];
                const currentUserIncluded = rawItems.some(user => user?.id === currentUserId || user?.email === currentUserEmail);

                this.users = rawItems.filter(user => user?.id !== currentUserId && user?.email !== currentUserEmail);
                this.totalItems = Math.max(0, (pageData.totalItems ?? this.users.length) - (currentUserIncluded ? 1 : 0));
                this.totalPages = pageData.totalPages ?? (this.totalItems > 0 ? 1 : 0);
// #region debug-point B:users-frontend-state
                fetch("http://127.0.0.1:7777/event",{method:"POST",mode:"no-cors",headers:{"Content-Type":"text/plain;charset=UTF-8"},body:JSON.stringify({sessionId:"users-list-empty",runId:"post-fix",hypothesisId:"B",location:"frontend/components/users/users.component.js:90",msg:"[DEBUG] Estado frontend tras normalizar usuarios",data:{currentUserId,currentUserEmail,assignedUsersLength:Array.isArray(this.users)?this.users.length:null,totalItems:this.totalItems,totalPages:this.totalPages},ts:Date.now()})}).catch(()=>{});
// #endregion
} else {
                throw new Error(response.message || 'Error al obtener usuarios');
            }
        } catch (error) {
throw error;
        }
    }

    renderAdminView() {
        const currentUser = AuthService.getUserSession();
        const userName = currentUser ? `${currentUser.name} ${currentUser.surname1}` : 'Usuario';
        
        const html = `
            <div class="container-fluid py-5">
                <!-- Saludo Usuario -->
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="fas fa-user-check me-2"></i>
                            <strong>¡Bienvenido Usuario: ${userName}!</strong>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </div>
                </div>

                <!-- Header -->
                <div class="row mb-4">
                    <div class="col-12 d-flex justify-content-between align-items-center">
                        <h1 class="display-5 fw-bold mb-2">
                            <i class="fas fa-users me-3"></i>Lista de Usuarios
                        </h1>
                        <button type="button" class="btn btn-primary" id="addUserBtn">
                            <i class="fas fa-user-plus me-2"></i>Agregar usuario
                        </button>
                    </div>
                    <div class="col-12">
                        <p class="text-muted">Total de usuarios: <strong>${this.totalItems}</strong></p>
                    </div>
                </div>

                <!-- Tabla de usuarios -->
                <div class="row">
                    <div class="col-12">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                ${this.users.length > 0 ? this.renderTable() : this.renderEmpty()}
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
        const rows = this.users.map(user => `
            <tr>
                <td>
                    ${user.profileImg ? 
                        `<img src="${API_CONFIG.BASE_URL}/images/${user.profileImg}" alt="${user.nick}" class="img-thumbnail rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">` :
                        `<div class="bg-secondary rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 40px; height: 40px;"><i class="fas fa-user text-white"></i></div>`
                    }
                </td>
                <td>
                    <strong>${user.nick || 'N/A'}</strong><br>
                    <small class="text-muted">${user.name} ${user.surname1}</small>
                </td>
                <td>${user.email}</td>
                <td>${user.phone || '-'}</td>
                <td>
                    <span class="badge ${this.getRoleBadgeClass(user.role) || 'bg-secondary'}">
                        ${user.role || 'USER'}
                    </span>
                </td>
                <td>
                    <small class="text-muted">${new Date(user.createdAt).toLocaleDateString()}</small>
                </td>
                <td>
                    <div class="btn-group btn-group-sm" role="group">
                        <button class="btn btn-outline-primary btn-sm view-user-btn" data-user-id="${user.id}">
                            <i class="fas fa-eye"></i> Ver
                        </button>
                        ${this.isAdmin ? `
                            <button class="btn btn-outline-success btn-sm edit-user-btn" data-user-id="${user.id}">
                                <i class="fas fa-pen"></i> Editar
                            </button>
                            <button class="btn btn-outline-danger btn-sm delete-user-btn" data-user-id="${user.id}" data-user-nick="${user.nick}">
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
                            <th>Usuario</th>
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

    renderEmpty() {
        return `
            <div class="text-center py-5">
                <i class="fas fa-inbox display-1 text-secondary mb-3"></i>
                <h4>No hay usuarios</h4>
                <p class="text-muted">No se encontraron usuarios en el sistema.</p>
            </div>
        `;
    }

    renderPagination() {
        const pages = [];
        
        // Boton anterior
        pages.push(`
            <li class="page-item ${this.currentPage === 0 ? 'disabled' : ''}">
                <button type="button" class="page-link btn btn-link text-decoration-none" onclick="UsersComponent.previousPage()" data-page="${this.currentPage - 1}">
                    Anterior
                </button>
            </li>
        `);

        // Números de página
        for (let i = 0; i < this.totalPages; i++) {
            pages.push(`
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <button type="button" class="page-link btn btn-link text-decoration-none" onclick="UsersComponent.goToPage(${i})" data-page="${i}">
                        ${i + 1}
                    </button>
                </li>
            `);
        }

        // Boton siguiente
        pages.push(`
            <li class="page-item ${this.currentPage === this.totalPages - 1 ? 'disabled' : ''}">
                <button type="button" class="page-link btn btn-link text-decoration-none" onclick="UsersComponent.nextPage()" data-page="${this.currentPage + 1}">
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

    renderAccessDenied(userRole) {
        const roleBadgeClass = this.getRoleBadgeClass(userRole);
        const html = `
            <div class="container py-5">
                <div class="row mb-5">
                    <div class="col-12">
                        <h1 class="display-5 fw-bold mb-2"><i class="fas fa-users me-3"></i>Lista de Usuarios</h1>
                        <p class="text-muted">Administra los usuarios del sistema</p>
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
                                    <strong>Esta funcionalidad solo está disponible para usuarios con rol ADMIN</strong>
                                </p>
                                <div class="alert alert-info" role="alert">
                                    <i class="fas fa-user-circle me-2"></i>
                                    <strong>Tu rol actual:</strong> <span class="badge ${roleBadgeClass} ms-2">${userRole || 'No especificado'}</span>
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
                UsersComponent.showCreateUserModal();
            });
        }

        // Listeners para botones "Ver" (detalles del usuario)
        document.querySelectorAll('.view-user-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const userId = btn.dataset.userId;
                if (userId) {
                    App.getInstance().navigateTo('/users/' + userId);
                }
            });
        });

        document.querySelectorAll('.edit-user-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const userId = btn.dataset.userId;
                if (!userId) return;
                try {
                    const res = await UserService.getUserById(userId);
                    const user = res?.data || res?.user || null;
                    if (!user) {
                        UsersComponent.showErrorNotification('No se pudo cargar el usuario');
                        return;
                    }
                    UsersComponent.showEditUserModal(user);
                } catch (e) {
                    UsersComponent.showErrorNotification(e.message || 'Error al cargar usuario');
                }
            });
        });

        // Listeners para botones "Eliminar"
        document.querySelectorAll('.delete-user-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const userId = btn.dataset.userId;
                const userNick = btn.dataset.userNick;
                if (userId) {
                    UsersComponent.confirmDelete(userId, userNick);
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
    static async confirmDelete(userId, userNick) {
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
                            <p>¿Estás seguro de que deseas eliminar al usuario <strong>${userNick}</strong>?</p>
                            <p class="text-muted"><small>Una vez eliminado, no se puede recuperar.</small></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-danger" onclick="UsersComponent.deleteUserConfirmed(${userId})">
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
                                <i class="fas fa-user-plus me-2"></i>Agregar usuario
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
            confirmBtn.addEventListener('click', () => UsersComponent.createUserConfirmed(), { once: true });
        }
    }

    static async createUserConfirmed() {
        const form = document.getElementById('createUserForm');
        const btn = document.getElementById('confirmCreateUserBtn');
        if (!form || !btn) return;

        const fd = new FormData(form);
        const userData = {
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

        if (!userData.nick || !userData.email || !userData.name || !userData.surname1 || !userData.phone || !userData.password || !userData.gender) {
            UsersComponent.showErrorNotification('Completa los campos obligatorios');
            return;
        }

        btn.disabled = true;
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creando...';

        try {
            const user = new User(userData);
            const profilePicture = fd.get('profilePicture');
            const response = await UserService.register(user, profilePicture);
            if (!response?.success) {
                throw new Error(response?.message || 'No se pudo crear el usuario');
            }

            const modal = bootstrap.Modal.getInstance(document.getElementById('createUserModal'));
            if (modal) {
                modal.hide();
            }
            UsersComponent.showSuccessNotification('Usuario creado. Revisa el correo para verificar la cuenta.');

            const instance = window.UsersComponentInstance;
            if (instance) {
                instance.currentPage = 0;
                await instance.loadUsers();
                instance.renderAdminView();
                instance.attachPaginationListeners();
            }
        } catch (e) {
            UsersComponent.showErrorNotification(e.message || 'Error al crear usuario');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalHtml;
        }
    }

    static showEditUserModal(user) {
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
                            <form id="editUserForm" data-user-id="${user.id}">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Nick</label>
                                        <input class="form-control" value="${user.nick || ''}" disabled>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Email</label>
                                        <input class="form-control" value="${user.email || ''}" disabled>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Nombre</label>
                                        <input class="form-control" name="name" value="${user.name || ''}" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Primer apellido</label>
                                        <input class="form-control" name="surname1" value="${user.surname1 || ''}" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Segundo apellido</label>
                                        <input class="form-control" name="surname2" value="${user.surname2 || ''}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Teléfono</label>
                                        <input class="form-control" name="phone" value="${user.phone || ''}" required>
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
            confirmBtn.addEventListener('click', () => UsersComponent.updateUserConfirmed(), { once: true });
        }
    }

    static async updateUserConfirmed() {
        const form = document.getElementById('editUserForm');
        const btn = document.getElementById('confirmEditUserBtn');
        if (!form || !btn) return;

        const userId = form.dataset.userId;
        const fd = new FormData(form);
        const payload = {
            name: String(fd.get('name') || '').trim(),
            surname1: String(fd.get('surname1') || '').trim(),
            surname2: String(fd.get('surname2') || '').trim(),
            phone: String(fd.get('phone') || '').trim()
        };

        if (!userId || !payload.name || !payload.surname1 || !payload.phone) {
            UsersComponent.showErrorNotification('Completa los campos obligatorios');
            return;
        }

        btn.disabled = true;
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Guardando...';

        try {
            const response = await UserService.updateUser(userId, payload);
            if (!response?.success) {
                throw new Error(response?.message || 'No se pudo actualizar el usuario');
            }

            const modal = bootstrap.Modal.getInstance(document.getElementById('editUserModal'));
            if (modal) {
                modal.hide();
            }
            UsersComponent.showSuccessNotification('Usuario actualizado exitosamente');

            const instance = window.UsersComponentInstance;
            if (instance) {
                await instance.loadUsers();
                instance.renderAdminView();
                instance.attachPaginationListeners();
            }
        } catch (e) {
            UsersComponent.showErrorNotification(e.message || 'Error al actualizar usuario');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalHtml;
        }
    }

    /**
     * Ejecuta la eliminación del usuario confirmada
     */
    static async deleteUserConfirmed(userId) {
        try {
            const deleteBtn = document.querySelector('.btn-danger[onclick*="deleteUserConfirmed"]');
            if (deleteBtn) {
                deleteBtn.disabled = true;
                deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Eliminando...';
            }
const response = await UserService.deleteUser(userId);

            if (response.success) {
// Cerrar modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('deleteUserModal'));
                if (modal) {
                    modal.hide();
                }

                // Mostrar notificación de éxito
                this.showSuccessNotification('Usuario eliminado exitosamente');

                // Recargar lista de usuarios de forma más simple
setTimeout(async () => {
                    try {
                        // Acceder a la instancia global
                        const instance = window.UsersComponentInstance;
                        
                        if (instance) {
// Resetear a primera página
                            instance.currentPage = 0;
                            await instance.loadUsers();
                            instance.renderAdminView();
                            instance.attachPaginationListeners();
                        } else {
// Si no hay instancia, navegar a /users para forzar recarga
                            App.getInstance().navigateTo('/users');
                        }
                    } catch (error) {
// Fallback: Navegar a /users
                        App.getInstance().navigateTo('/users');
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
        const instance = UsersComponentInstance;
        if (instance) {
            instance.currentPage = page;
            await instance.loadUsers();
            instance.renderAdminView();
            instance.attachPaginationListeners();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }

    static async nextPage() {
        const instance = UsersComponentInstance;
        if (instance && instance.currentPage < instance.totalPages - 1) {
            await this.goToPage(instance.currentPage + 1);
        }
    }

    static async previousPage() {
        const instance = UsersComponentInstance;
        if (instance && instance.currentPage > 0) {
            await this.goToPage(instance.currentPage - 1);
        }
    }
}

// Instancia global para acceder desde métodos estáticos
let UsersComponentInstance = null;

// Exponer globalmente para la verificación de carga
window.UsersComponent = UsersComponent;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') {
    AppScripts.register('users');
}


