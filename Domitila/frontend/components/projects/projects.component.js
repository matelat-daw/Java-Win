/**
 * projects.component.js - Gestión de proyectos, equipo y tablero Kanban
 */

class ProjectsComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.currentPage = 0;
        this.pageSize = 8;
        this.totalItems = 0;
        this.totalPages = 0;
        this.projects = [];
        this.isAdmin = false;
        this.currentStaffRole = null;
        this.projectFormTemplate = null;
        this.teamSearchDebounceId = null;
        this.memberSearchDebounceId = null;
        this.kanbanState = {
            project: null,
            team: [],
            tasks: [],
            beneficiaries: [],
            beneficiariesTotal: 0
        };
    }

    async init() {
        try {
            if (!AuthService.isAuthenticated()) {
                App.getInstance().navigateTo('/login');
                return;
            }

            this.currentStaffRole = AuthService.getRole();
            this.isAdmin = this.currentStaffRole === 'ADMIN';

            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Cargando proyectos...</span>
                        </div>
                        <p class="mt-3">Cargando lista de proyectos...</p>
                    </div>
                `;
            }

            await this.loadProjects();
            this.render();
        } catch (error) {
            const container = document.querySelector(this.selector);
            if (container) {
                container.innerHTML = `
                    <div class="alert alert-danger m-5">
                        <h4>Error al cargar proyectos</h4>
                        <p>${ProjectsComponent.escapeHtml(error.message || 'No se pudo cargar la lista de proyectos')}</p>
                        <button class="btn btn-primary" onclick="location.reload()">Recargar</button>
                    </div>
                `;
            }
        }
    }

    async loadProjects() {
        const pageData = await ProjectService.getProjects(this.currentPage, this.pageSize);
        this.projects = Array.isArray(pageData?.items) ? pageData.items : [];
        this.currentPage = pageData?.currentPage ?? this.currentPage;
        this.totalItems = pageData?.totalItems ?? this.projects.length;
        this.totalPages = pageData?.totalPages ?? (this.totalItems > 0 ? 1 : 0);
    }

    render() {
        const currentStaff = AuthService.getStaffSession();
        const staffName = currentStaff ? `${currentStaff.name} ${currentStaff.surname1}` : 'Usuario';
        const html = `
            <div class="container-fluid py-5">
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="fas fa-folder-open me-2"></i>
                            <strong>¡Bienvenido Staff: ${ProjectsComponent.escapeHtml(staffName)}!</strong>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </div>
                </div>

                <div class="row mb-4">
                    <div class="col-12 d-flex justify-content-between align-items-center flex-wrap gap-3">
                        <div>
                            <h1 class="display-5 fw-bold mb-2">
                                <i class="fas fa-diagram-project me-3"></i>Lista de Proyectos
                            </h1>
                            <p class="text-muted mb-0">Total de proyectos: <strong>${this.totalItems}</strong></p>
                        </div>
                        ${this.isAdmin ? `
                            <button type="button" class="btn btn-primary" id="addProjectBtn">
                                <i class="fas fa-folder-plus me-2"></i>Agregar proyecto
                            </button>
                        ` : ''}
                    </div>
                </div>

                <div class="row">
                    <div class="col-12">
                        <div class="card shadow-sm">
                            <div class="card-body">
                                ${this.projects.length > 0 ? this.renderTable() : this.renderEmpty()}
                            </div>
                        </div>
                    </div>
                </div>
                ${this.totalPages > 1 ? this.renderProjectsPagination() : ''}
            </div>
        `;

        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = html;
            this.attachEventListeners();
        }
    }

    renderProjectsPagination() {
        const pages = [];

        pages.push(`
            <li class="page-item ${this.currentPage === 0 ? 'disabled' : ''}">
                <button type="button" class="page-link project-page-btn" data-page="${this.currentPage - 1}">
                    Anterior
                </button>
            </li>
        `);

        for (let i = 0; i < this.totalPages; i++) {
            pages.push(`
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <button type="button" class="page-link project-page-btn" data-page="${i}">
                        ${i + 1}
                    </button>
                </li>
            `);
        }

        pages.push(`
            <li class="page-item ${this.currentPage >= this.totalPages - 1 ? 'disabled' : ''}">
                <button type="button" class="page-link project-page-btn" data-page="${this.currentPage + 1}">
                    Siguiente
                </button>
            </li>
        `);

        return `
            <div class="row mt-4">
                <div class="col-12 d-flex justify-content-center">
                    <nav aria-label="Paginación de proyectos">
                        <ul class="pagination mb-0">
                            ${pages.join('')}
                        </ul>
                    </nav>
                </div>
            </div>
        `;
    }

    renderTable() {
        const rows = this.projects.map(project => `
            <tr>
                <td><strong>${ProjectsComponent.escapeHtml(project.code || '-')}</strong></td>
                <td>${ProjectsComponent.escapeHtml(project.name || '-')}</td>
                <td>${ProjectsComponent.escapeHtml(project.description || '-')}</td>
                <td>${ProjectsComponent.escapeHtml(project.type || '-')}</td>
                <td>
                    ${this.isAdmin ? `
                        <select class="form-select form-select-sm project-status-select" data-project-id="${project.id}" style="min-width: 140px;">
                            <option value="ACTIVO" ${project.status === 'ACTIVO' ? 'selected' : ''}>ACTIVO</option>
                            <option value="PAUSADO" ${project.status === 'PAUSADO' ? 'selected' : ''}>PAUSADO</option>
                            <option value="COMPLETADO" ${project.status === 'COMPLETADO' ? 'selected' : ''}>COMPLETADO</option>
                            <option value="CANCELADO" ${project.status === 'CANCELADO' ? 'selected' : ''}>CANCELADO</option>
                        </select>
                    ` : `
                        <span class="badge ${ProjectsComponent.getStatusBadgeClass(project.status)}">
                            ${ProjectsComponent.escapeHtml(project.status || 'SIN ESTADO')}
                        </span>
                    `}
                </td>
                <td>
                    <span class="badge bg-light text-dark border">${project.teamMemberCount ?? 0}</span>
                </td>
                <td>
                    <span class="badge bg-light text-dark border">${project.taskCount ?? 0}</span>
                </td>
                <td>${ProjectsComponent.formatDate(project.startDate)}</td>
                <td>${ProjectsComponent.formatDate(project.endDate)}</td>
                <td>
                    <div class="btn-group btn-group-sm flex-wrap gap-1" role="group">
                        <button class="btn btn-outline-info view-team-btn" data-project-id="${project.id}">
                            <i class="fas fa-user"></i> Equipo
                        </button>
                        ${this.isAdmin ? `
                            <button class="btn btn-outline-primary add-members-btn" data-project-id="${project.id}">
                                <i class="fas fa-user-plus"></i> Miembros
                            </button>
                        ` : ''}
                        <button class="btn btn-outline-success board-btn" data-project-id="${project.id}">
                            <i class="fas fa-columns"></i> Tablero
                        </button>
                        ${this.isAdmin ? `
                            <button class="btn btn-outline-danger delete-project-btn" data-project-id="${project.id}" data-project-name="${ProjectsComponent.escapeHtml(project.name || '')}">
                                <i class="fas fa-trash"></i> Eliminar
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');

        return `
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Código</th>
                            <th>Nombre</th>
                            <th>Descripción</th>
                            <th>Tipo</th>
                            <th>Estado</th>
                            <th>Miembros</th>
                            <th>Tareas</th>
                            <th>Fecha Inicio</th>
                            <th>Fecha Fin</th>
                            <th style="width: 340px;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>
        `;
    }

    renderEmpty() {
        return `
            <div class="text-center py-5">
                <i class="fas fa-folder-open display-1 text-secondary mb-3"></i>
                <h4>No hay proyectos</h4>
                <p class="text-muted">Todavía no se han creado proyectos en el sistema.</p>
            </div>
        `;
    }

    attachEventListeners() {
        const addProjectBtn = document.getElementById('addProjectBtn');
        if (addProjectBtn) {
            addProjectBtn.addEventListener('click', () => this.showCreateProjectModal());
        }

        document.querySelectorAll('.project-page-btn').forEach(button => {
            button.addEventListener('click', async () => {
                const nextPage = Number.parseInt(button.dataset.page, 10);
                if (!Number.isInteger(nextPage) || nextPage < 0 || nextPage >= this.totalPages || nextPage === this.currentPage) {
                    return;
                }

                this.currentPage = nextPage;
                await this.loadProjects();
                this.render();
            });
        });

        document.querySelectorAll('.view-team-btn').forEach(button => {
            button.addEventListener('click', () => {
                const project = this.findProjectById(button.dataset.projectId);
                if (project) {
                    this.showProjectTeamModal(project);
                }
            });
        });

        document.querySelectorAll('.add-members-btn').forEach(button => {
            button.addEventListener('click', () => {
                const project = this.findProjectById(button.dataset.projectId);
                if (project) {
                    this.showAddMembersModal(project);
                }
            });
        });

        document.querySelectorAll('.board-btn').forEach(button => {
            button.addEventListener('click', () => {
                const project = this.findProjectById(button.dataset.projectId);
                if (project) {
                    this.showProjectBoardModal(project);
                }
            });
        });

        document.querySelectorAll('.delete-project-btn').forEach(button => {
            button.addEventListener('click', () => {
                const project = this.findProjectById(button.dataset.projectId);
                if (project) {
                    this.confirmDeleteProject(project);
                }
            });
        });

        document.querySelectorAll('.project-status-select').forEach(select => {
            select.addEventListener('change', async () => {
                const project = this.findProjectById(select.dataset.projectId);
                if (!project) {
                    return;
                }

                const originalStatus = project.status;
                const nextStatus = select.value;
                select.disabled = true;

                try {
                    await ProjectService.updateProject(project.id, {
                        code: project.code,
                        name: project.name,
                        description: project.description,
                        startDate: project.startDate,
                        endDate: project.endDate,
                        status: nextStatus,
                        type: project.type
                    });

                    project.status = nextStatus;
                    await this.loadProjects();
                    this.render();
                    Utils.showMessage('Estado actualizado', 'El estado del proyecto se ha actualizado correctamente.', 'success');
                } catch (error) {
                    project.status = originalStatus;
                    select.value = originalStatus || 'ACTIVO';
                    Utils.showMessage(
                        'Error',
                        ProjectsComponent.escapeHtml(error.backendMessage || error.message || 'No se pudo actualizar el estado del proyecto.'),
                        'error'
                    );
                } finally {
                    select.disabled = false;
                }
            });
        });
    }

    async showCreateProjectModal() {
        const template = await this.loadProjectFormTemplate();
        const modalId = 'projectCreateModal';
        const existingModal = document.getElementById(modalId);
        if (existingModal) {
            existingModal.remove();
        }

        const modalWrapper = document.createElement('div');
        modalWrapper.innerHTML = `
            <div class="modal fade" id="${modalId}" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header bg-primary text-white">
                            <h5 class="modal-title">
                                <i class="fas fa-folder-plus me-2"></i>Nuevo Proyecto
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            ${template}
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-primary" id="confirmCreateProjectBtn">
                                <i class="fas fa-save me-2"></i>Guardar Proyecto
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `.trim();

        document.body.appendChild(modalWrapper.firstElementChild);

        const modalElement = document.getElementById(modalId);
        const modal = new bootstrap.Modal(modalElement);
        const form = modalElement.querySelector('#createProjectForm');
        const submitBtn = modalElement.querySelector('#confirmCreateProjectBtn');

        if (form) {
            form.addEventListener('submit', async (event) => {
                event.preventDefault();
                await this.createProjectFromForm(form, submitBtn, modal);
            });
        }

        if (submitBtn) {
            submitBtn.addEventListener('click', () => {
                if (form) {
                    form.requestSubmit();
                }
            });
        }

        modalElement.addEventListener('hidden.bs.modal', () => {
            modalElement.remove();
        }, { once: true });

        modal.show();
    }

    async createProjectFromForm(form, submitBtn, modal) {
        if (!form.reportValidity()) {
            return;
        }

        const projectPayload = {
            code: form.querySelector('#pCode')?.value?.trim() || '',
            name: form.querySelector('#pName')?.value?.trim() || '',
            description: form.querySelector('#pDescription')?.value?.trim() || '',
            startDate: form.querySelector('#pStartDate')?.value || '',
            endDate: form.querySelector('#pEndDate')?.value || null,
            status: form.querySelector('#pStatus')?.value || 'ACTIVO',
            type: form.querySelector('#pType')?.value?.trim() || ''
        };

        const originalBtnHtml = submitBtn ? submitBtn.innerHTML : '';
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Guardando...';
        }

        try {
            await ProjectService.createProject(projectPayload);
            modal.hide();
            await this.loadProjects();
            this.render();
            Utils.showMessage('Proyecto creado', 'El proyecto se ha creado correctamente.', 'success');
        } catch (error) {
            Utils.showMessage(
                'Error',
                ProjectsComponent.escapeHtml(error.backendMessage || error.message || 'No se pudo crear el proyecto.'),
                'error'
            );
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnHtml;
            }
        }
    }

    async loadProjectFormTemplate() {
        if (this.projectFormTemplate) {
            return this.projectFormTemplate;
        }

        const response = await fetch('/frontend/components/projects/project.html');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        this.projectFormTemplate = await response.text();
        return this.projectFormTemplate;
    }

    async showProjectTeamModal(project) {
        const modalId = 'projectTeamModal';
        this.removeModal(modalId);

        const modalElement = this.createModal({
            id: modalId,
            title: `Equipo del proyecto: ${ProjectsComponent.escapeHtml(project.name)}`,
            size: 'modal-xl',
            body: `
                <div class="row g-3 mb-3">
                    <div class="col-md-8">
                        <label class="form-label">Buscar por apellido</label>
                        <input type="text" class="form-control" id="projectTeamSearchInput" placeholder="Ej: Pérez">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Orden por apellido</label>
                        <select class="form-select" id="projectTeamSortDir">
                            <option value="asc">A → Z</option>
                            <option value="desc">Z → A</option>
                        </select>
                    </div>
                </div>
                <div id="projectTeamListContainer">
                    <div class="text-center py-4">
                        <div class="spinner-border text-primary" role="status"></div>
                    </div>
                </div>
            `,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
            `
        });

        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        const loadTeam = async () => {
            const surname = modalElement.querySelector('#projectTeamSearchInput')?.value || '';
            const sortDir = modalElement.querySelector('#projectTeamSortDir')?.value || 'asc';
            const team = await ProjectService.getProjectTeam(project.id, { surname, sortDir });
            this.renderTeamList(modalElement.querySelector('#projectTeamListContainer'), project, team);
        };

        modalElement.querySelector('#projectTeamSearchInput')?.addEventListener('input', () => {
            clearTimeout(this.teamSearchDebounceId);
            this.teamSearchDebounceId = setTimeout(loadTeam, 250);
        });
        modalElement.querySelector('#projectTeamSortDir')?.addEventListener('change', loadTeam);

        await loadTeam();
    }

    renderTeamList(container, project, team) {
        if (!container) return;

        if (!team.length) {
            container.innerHTML = `
                <div class="text-center py-5">
                    <i class="fas fa-users display-1 text-secondary mb-3"></i>
                    <h5>Este proyecto no tiene miembros asignados</h5>
                    <p class="text-muted mb-0">Usa el botón "Miembros" para agregar personas al equipo.</p>
                </div>
            `;
            return;
        }

        const rows = team.map(staff => `
            <tr>
                <td>${ProjectsComponent.renderStaffAvatar(staff)}</td>
                <td>
                    <strong>${ProjectsComponent.escapeHtml(staff.nick || '-')}</strong><br>
                    <small class="text-muted">${ProjectsComponent.escapeHtml(ProjectsComponent.getStaffFullName(staff))}</small>
                </td>
                <td>${ProjectsComponent.renderEmailCell(staff.email)}</td>
                <td>${ProjectsComponent.renderPhoneCell(staff.phone)}</td>
                <td>${ProjectsComponent.escapeHtml(staff.role || 'USER')}</td>
                <td class="text-end">
                    ${this.isAdmin ? `
                        <button class="btn btn-outline-danger btn-sm remove-member-btn" data-project-id="${project.id}" data-staff-id="${staff.id}">
                            <i class="fas fa-user-minus"></i> Quitar
                        </button>
                    ` : ''}
                </td>
            </tr>
        `).join('');

        container.innerHTML = `
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th style="width: 56px;">Foto</th>
                            <th>Staff</th>
                            <th>Email</th>
                            <th>Teléfono</th>
                            <th>Rol</th>
                            <th class="text-end">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>
        `;

        container.querySelectorAll('.remove-member-btn').forEach(button => {
            button.addEventListener('click', async () => {
                await ProjectService.removeProjectMember(button.dataset.projectId, button.dataset.staffId);
                await this.loadProjects();
                this.render();
                const refreshedTeam = await ProjectService.getProjectTeam(project.id, {
                    surname: document.getElementById('projectTeamSearchInput')?.value || '',
                    sortDir: document.getElementById('projectTeamSortDir')?.value || 'asc'
                });
                this.renderTeamList(container, project, refreshedTeam);
            });
        });
    }

    async showAddMembersModal(project) {
        const modalId = 'projectAddMembersModal';
        this.removeModal(modalId);

        const modalElement = this.createModal({
            id: modalId,
            title: `Agregar miembros a: ${ProjectsComponent.escapeHtml(project.name)}`,
            size: 'modal-xl',
            body: `
                <div class="row g-3 mb-3">
                    <div class="col-md-8">
                        <label class="form-label">Buscar staff por apellido</label>
                        <input type="text" class="form-control" id="projectMembersSearchInput" placeholder="Ej: Pérez">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label">Orden por apellido</label>
                        <select class="form-select" id="projectMembersSortDir">
                            <option value="asc">A → Z</option>
                            <option value="desc">Z → A</option>
                        </select>
                    </div>
                </div>
                <div class="alert alert-light border mb-3" id="projectCurrentTeamSummary">Cargando equipo actual...</div>
                <div id="projectAvailableMembersContainer">
                    <div class="text-center py-4">
                        <div class="spinner-border text-primary" role="status"></div>
                    </div>
                </div>
            `,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
            `
        });

        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        const loadAvailableMembers = async () => {
            const surname = modalElement.querySelector('#projectMembersSearchInput')?.value || '';
            const sortDir = modalElement.querySelector('#projectMembersSortDir')?.value || 'asc';
            const [team, response] = await Promise.all([
                ProjectService.getProjectTeam(project.id, { sortDir: 'asc' }),
                StaffService.getStaff(0, 100, { surname, sortBy: 'surname1', sortDir })
            ]);

            const allUsers = Array.isArray(response?.data?.items) ? response.data.items : [];
            const teamIds = new Set(team.map(staff => staff.id));
            const availableUsers = allUsers.filter(staff => !teamIds.has(staff.id));

            const summary = modalElement.querySelector('#projectCurrentTeamSummary');
            if (summary) {
                summary.innerHTML = `
                    <strong>Equipo actual:</strong> ${team.length} miembro(s)
                    ${team.length ? `<div class="mt-2 d-flex flex-wrap gap-2">${team.map(staff => `
                        <span class="badge bg-light text-dark border">${ProjectsComponent.escapeHtml(ProjectsComponent.getStaffFullName(staff))}</span>
                    `).join('')}</div>` : ''}
                `;
            }

            this.renderAvailableMembers(
                modalElement.querySelector('#projectAvailableMembersContainer'),
                project,
                availableUsers,
                loadAvailableMembers
            );
        };

        modalElement.querySelector('#projectMembersSearchInput')?.addEventListener('input', () => {
            clearTimeout(this.memberSearchDebounceId);
            this.memberSearchDebounceId = setTimeout(loadAvailableMembers, 250);
        });
        modalElement.querySelector('#projectMembersSortDir')?.addEventListener('change', loadAvailableMembers);

        await loadAvailableMembers();
    }

    renderAvailableMembers(container, project, staff, reload) {
        if (!container) return;

        if (!staff.length) {
            container.innerHTML = `
                <div class="text-center py-5">
                    <i class="fas fa-user-slash display-1 text-secondary mb-3"></i>
                    <h5>No hay staff disponible</h5>
                    <p class="text-muted mb-0">Todo el staff encontrado ya forma parte del proyecto.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th style="width: 56px;">Foto</th>
                            <th>Staff</th>
                            <th>Email</th>
                            <th>Teléfono</th>
                            <th class="text-end">Acción</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${staff.map(staff => `
                            <tr>
                                <td>${ProjectsComponent.renderStaffAvatar(staff)}</td>
                                <td>
                                    <strong>${ProjectsComponent.escapeHtml(staff.nick || '-')}</strong><br>
                                    <small class="text-muted">${ProjectsComponent.escapeHtml(ProjectsComponent.getStaffFullName(staff))}</small>
                                </td>
                                <td>${ProjectsComponent.renderEmailCell(staff.email)}</td>
                                <td>${ProjectsComponent.renderPhoneCell(staff.phone)}</td>
                                <td class="text-end">
                                    <button class="btn btn-outline-primary btn-sm assign-member-btn" data-project-id="${project.id}" data-staff-id="${staff.id}">
                                        <i class="fas fa-user-plus"></i> Agregar
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;

        container.querySelectorAll('.assign-member-btn').forEach(button => {
            button.addEventListener('click', async () => {
                await ProjectService.addProjectMember(button.dataset.projectId, button.dataset.staffId);
                await this.loadProjects();
                this.render();
                await reload();
            });
        });
    }

    async showProjectBoardModal(project) {
        const modalId = 'projectBoardModal';
        this.removeModal(modalId);

        const modalElement = this.createModal({
            id: modalId,
            title: `Tablero Kanban: ${ProjectsComponent.escapeHtml(project.name)}`,
            size: 'modal-fullscreen-xl-down modal-xl',
            body: `
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                    <div class="text-muted">
                        ${this.isAdmin
                            ? 'Organiza el trabajo por fases y asigna cada tarea a un miembro del proyecto.'
                            : 'Solo ves las tareas que te han sido asignadas dentro de este proyecto.'}
                    </div>
                    <div class="d-flex align-items-center flex-wrap gap-2">
                        <button type="button" class="btn btn-outline-primary" id="openProjectBeneficiariesBtn">
                            <i class="fas fa-people-group me-2"></i>Beneficiarios
                        </button>
                        ${this.isAdmin ? `
                            <button type="button" class="btn btn-primary" id="openCreateTaskBtn">
                                <i class="fas fa-plus me-2"></i>Nueva tarea
                            </button>
                        ` : `
                            <span class="badge bg-light text-dark border px-3 py-2">
                                <i class="fas fa-user-lock me-2"></i>Solo ADMIN puede crear tareas
                            </span>
                        `}
                    </div>
                </div>
                <div id="projectKanbanContainer">
                    <div class="text-center py-4">
                        <div class="spinner-border text-primary" role="status"></div>
                    </div>
                </div>
            `,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
            `
        });

        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        const refreshBoard = async () => {
            const [team, tasks, beneficiariesPage] = await Promise.all([
                ProjectService.getProjectTeam(project.id, { sortDir: 'asc' }),
                ProjectService.getProjectTasks(project.id),
                ProjectService.getProjectBeneficiaries(project.id, 0, this.pageSize)
            ]);

            this.kanbanState = {
                project,
                team,
                tasks,
                beneficiaries: Array.isArray(beneficiariesPage?.items) ? beneficiariesPage.items : [],
                beneficiariesTotal: beneficiariesPage?.totalItems ?? 0
            };
            this.renderKanban(modalElement.querySelector('#projectKanbanContainer'), project, team, tasks);
        };

        if (this.isAdmin) {
            modalElement.querySelector('#openCreateTaskBtn')?.addEventListener('click', () => {
                this.showCreateTaskModal(project, refreshBoard);
            });
        }

        modalElement.querySelector('#openProjectBeneficiariesBtn')?.addEventListener('click', () => {
            this.showProjectBeneficiariesModal(project, refreshBoard);
        });

        await refreshBoard();
    }

    renderKanban(container, project, team, tasks) {
        if (!container) return;

        const columns = [
            { key: 'POR_HACER', title: 'Por hacer', icon: 'fa-list-check', className: 'border-secondary' },
            { key: 'EN_PROGRESO', title: 'En progreso', icon: 'fa-spinner', className: 'border-warning' },
            { key: 'TERMINADO', title: 'Terminado', icon: 'fa-circle-check', className: 'border-success' }
        ];

        container.innerHTML = `
            <div class="row g-3">
                ${columns.map(column => {
                    const columnTasks = tasks.filter(task => ProjectsComponent.normalizeTaskStatus(task.status) === column.key);
                    return `
                        <div class="col-lg-4">
                            <div class="card shadow-sm h-100 ${column.className}">
                                <div class="card-header bg-light">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h6 class="mb-0"><i class="fas ${column.icon} me-2"></i>${column.title}</h6>
                                        <span class="badge bg-dark">${columnTasks.length}</span>
                                    </div>
                                </div>
                                <div class="card-body bg-body-tertiary">
                                ${columnTasks.length ? columnTasks.map(task => this.renderTaskCard(project, task)).join('') : `
                                        <div class="text-center text-muted py-5">
                                            <i class="fas fa-inbox mb-2 d-block fs-2"></i>
                                            <span>Sin tareas en esta fase</span>
                                        </div>
                                    `}
                                </div>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;

        container.querySelectorAll('.task-status-select').forEach(select => {
            select.addEventListener('change', async () => {
                await ProjectService.updateTaskStatus(select.dataset.projectId, select.dataset.taskId, select.value);
                const refreshedTasks = await ProjectService.getProjectTasks(project.id);
                this.kanbanState.tasks = refreshedTasks;
                this.renderKanban(container, project, team, refreshedTasks);
                await this.loadProjects();
                this.render();
            });
        });

        container.querySelectorAll('.task-incidents-btn').forEach(button => {
            button.addEventListener('click', async () => {
                const selectedTask = (this.kanbanState.tasks || []).find(task => String(task.id) === String(button.dataset.taskId));
                if (!selectedTask) {
                    Utils.showMessage('Error', 'No se pudo localizar la tarea seleccionada.', 'error');
                    return;
                }

                this.showTaskIncidentsModal(project, selectedTask);
            });
        });
    }

    renderTaskCard(project, task) {
        const assignee = task.assignedStaff;
        const incidents = Array.isArray(task.incidents) ? task.incidents : [];
        const incidentCount = Number.isFinite(task.incidentCount) ? task.incidentCount : incidents.length;
        const canCreateIncident = ProjectsComponent.canCreateIncident(task.status);
        return `
            <div class="card shadow-sm mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
                        <h6 class="card-title mb-0">${ProjectsComponent.escapeHtml(task.title || 'Sin título')}</h6>
                        <select class="form-select form-select-sm task-status-select" data-project-id="${project.id}" data-task-id="${task.id}" style="max-width: 150px;">
                            <option value="POR_HACER" ${ProjectsComponent.normalizeTaskStatus(task.status) === 'POR_HACER' ? 'selected' : ''}>Por hacer</option>
                            <option value="EN_PROGRESO" ${ProjectsComponent.normalizeTaskStatus(task.status) === 'EN_PROGRESO' ? 'selected' : ''}>En progreso</option>
                            <option value="TERMINADO" ${ProjectsComponent.normalizeTaskStatus(task.status) === 'TERMINADO' ? 'selected' : ''}>Terminado</option>
                        </select>
                    </div>
                    <p class="card-text small text-muted mb-3">${ProjectsComponent.escapeHtml(task.description || 'Sin descripción')}</p>
                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <div class="small text-muted">
                            <i class="fas fa-calendar-day me-1"></i>${ProjectsComponent.formatDate(task.dueDate)}
                        </div>
                        <div class="small">
                            ${assignee ? `
                                <span class="d-inline-flex align-items-center gap-2">
                                    ${ProjectsComponent.renderStaffAvatar(assignee, 28)}
                                    <span>${ProjectsComponent.escapeHtml(ProjectsComponent.getStaffFullName(assignee))}</span>
                                </span>
                            ` : '<span class="text-muted">Sin asignar</span>'}
                        </div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-3 pt-3 border-top">
                        <span class="badge text-bg-light border">
                            <i class="fas fa-triangle-exclamation me-1 text-danger"></i>${incidentCount} incidencia${incidentCount === 1 ? '' : 's'}
                        </span>
                        <button
                            type="button"
                            class="btn btn-outline-danger btn-sm task-incidents-btn"
                            data-project-id="${project.id}"
                            data-task-id="${task.id}"
                        >
                            <i class="fas fa-triangle-exclamation me-1"></i>${canCreateIncident ? 'Registrar incidencia' : 'Ver incidencias'}
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    showTaskIncidentsModal(project, task) {
        const modalId = 'taskIncidentsModal';
        this.removeModal(modalId);

        const incidents = Array.isArray(task.incidents) ? task.incidents : [];
        const canCreateIncident = ProjectsComponent.canCreateIncident(task.status);
        const modalElement = this.createModal({
            id: modalId,
            title: `Incidencias de ${ProjectsComponent.escapeHtml(task.title || 'la tarea')}`,
            size: 'modal-lg',
            body: `
                <div class="mb-4">
                    <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                        <div>
                            <div class="small text-muted mb-1">Proyecto</div>
                            <div class="fw-semibold">${ProjectsComponent.escapeHtml(project.name || 'Sin nombre')}</div>
                        </div>
                        <div>
                            <div class="small text-muted mb-1">Estado actual</div>
                            <span class="badge ${ProjectsComponent.getTaskStatusBadgeClass(task.status)}">
                                ${ProjectsComponent.escapeHtml(ProjectsComponent.getTaskStatusLabel(task.status))}
                            </span>
                        </div>
                    </div>
                    <p class="text-muted small mt-3 mb-0">${ProjectsComponent.escapeHtml(task.description || 'Sin descripción')}</p>
                </div>
                <div class="mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h6 class="mb-0">Historial de incidencias</h6>
                        <span class="badge text-bg-light border">${incidents.length}</span>
                    </div>
                    <div id="taskIncidentListContainer">
                        ${this.renderIncidentList(incidents)}
                    </div>
                </div>
                ${canCreateIncident ? `
                    <form id="createIncidentForm">
                        <h6 class="mb-3">Registrar nueva incidencia</h6>
                        <div class="mb-3">
                            <label class="form-label">Título</label>
                            <input type="text" class="form-control" id="incidentTitle" required>
                        </div>
                        <div class="row">
                            <div class="col-md-4 mb-3">
                                <label class="form-label">Severidad</label>
                                <select class="form-select" id="incidentSeverity">
                                    <option value="BAJA">Baja</option>
                                    <option value="MEDIA" selected>Media</option>
                                    <option value="ALTA">Alta</option>
                                    <option value="CRITICA">Crítica</option>
                                </select>
                            </div>
                            <div class="col-md-8 mb-3">
                                <label class="form-label">Descripción</label>
                                <textarea class="form-control" id="incidentDescription" rows="4" required></textarea>
                            </div>
                        </div>
                    </form>
                ` : `
                    <div class="alert alert-light border mb-0">
                        Solo se pueden registrar incidencias cuando la tarea está en <strong>Por hacer</strong> o <strong>En progreso</strong>.
                    </div>
                `}
            `,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                ${canCreateIncident ? `
                    <button type="button" class="btn btn-danger" id="confirmCreateIncidentBtn">
                        <i class="fas fa-triangle-exclamation me-2"></i>Registrar incidencia
                    </button>
                ` : ''}
            `
        });

        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        const attachIncidentStatusListeners = () => {
            modalElement.querySelectorAll('.incident-status-select').forEach(select => {
                select.addEventListener('change', async () => {
                    const incidentId = select.dataset.incidentId;
                    const nextStatus = select.value;
                    const originalStatus = select.dataset.originalStatus || '';

                    select.disabled = true;
                    try {
                        await ProjectService.updateTaskIncidentStatus(project.id, task.id, incidentId, nextStatus);
                        const refreshedTasks = await ProjectService.getProjectTasks(project.id);
                        this.kanbanState.tasks = refreshedTasks;

                        const refreshedTask = refreshedTasks.find(t => String(t.id) === String(task.id));
                        const refreshedIncidents = Array.isArray(refreshedTask?.incidents) ? refreshedTask.incidents : [];
                        const container = modalElement.querySelector('#taskIncidentListContainer');
                        if (container) {
                            container.innerHTML = this.renderIncidentList(refreshedIncidents);
                        }
                        attachIncidentStatusListeners();
                        Utils.showMessage('Incidencia actualizada', 'El estado de la incidencia se ha actualizado correctamente.', 'success');
                    } catch (error) {
                        select.value = originalStatus;
                        Utils.showMessage(
                            'Error',
                            ProjectsComponent.escapeHtml(error.backendMessage || error.message || 'No se pudo actualizar el estado de la incidencia.'),
                            'error'
                        );
                    } finally {
                        select.disabled = false;
                    }
                });
            });
        };

        attachIncidentStatusListeners();

        if (!canCreateIncident) {
            return;
        }

        const form = modalElement.querySelector('#createIncidentForm');
        const confirmBtn = modalElement.querySelector('#confirmCreateIncidentBtn');

        const submit = async () => {
            if (!form.reportValidity()) {
                return;
            }

            const originalHtml = confirmBtn.innerHTML;
            confirmBtn.disabled = true;
            confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Registrando...';

            try {
                await ProjectService.createTaskIncident(project.id, task.id, {
                    title: modalElement.querySelector('#incidentTitle')?.value?.trim() || '',
                    description: modalElement.querySelector('#incidentDescription')?.value?.trim() || '',
                    severity: modalElement.querySelector('#incidentSeverity')?.value || 'MEDIA'
                });

                modal.hide();
                const refreshedTasks = await ProjectService.getProjectTasks(project.id);
                this.kanbanState.tasks = refreshedTasks;
                this.renderKanban(document.querySelector('#projectKanbanContainer'), project, this.kanbanState.team || [], refreshedTasks);
                await this.loadProjects();
                this.render();
                Utils.showMessage('Incidencia registrada', 'La incidencia se ha guardado correctamente.', 'success');
            } catch (error) {
                Utils.showMessage(
                    'Error',
                    ProjectsComponent.escapeHtml(error.backendMessage || error.message || 'No se pudo registrar la incidencia.'),
                    'error'
                );
            } finally {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = originalHtml;
            }
        };

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            await submit();
        });
        confirmBtn.addEventListener('click', submit);
    }

    renderIncidentList(incidents) {
        if (!Array.isArray(incidents) || !incidents.length) {
            return `
                <div class="text-center text-muted py-4 border rounded bg-body-tertiary">
                    <i class="fas fa-shield-heart mb-2 d-block fs-3"></i>
                    <span>No hay incidencias registradas en esta tarea.</span>
                </div>
            `;
        }

        return incidents.map(incident => `
            <div class="border rounded p-3 mb-3 bg-body-tertiary">
                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap mb-2">
                    <div>
                        <h6 class="mb-1">${ProjectsComponent.escapeHtml(incident.title || 'Sin título')}</h6>
                        <div class="small text-muted">${ProjectsComponent.formatDateTime(incident.createdAt)}</div>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <span class="badge ${ProjectsComponent.getIncidentSeverityBadgeClass(incident.severity)}">
                            ${ProjectsComponent.escapeHtml(incident.severity || 'MEDIA')}
                        </span>
                        <span class="badge ${ProjectsComponent.getIncidentStatusBadgeClass(incident.status)}">
                            ${ProjectsComponent.escapeHtml(ProjectsComponent.getIncidentStatusLabel(incident.status))}
                        </span>
                    </div>
                </div>
                <p class="mb-0 small">${ProjectsComponent.escapeHtml(incident.description || 'Sin descripción')}</p>
                <div class="mt-3">
                    <label class="form-label small text-muted mb-1">Estado</label>
                    <select
                        class="form-select form-select-sm incident-status-select"
                        data-incident-id="${incident.id}"
                        data-original-status="${ProjectsComponent.escapeHtml(incident.status || 'ABIERTA')}"
                    >
                        <option value="ABIERTA" ${(incident.status || 'ABIERTA') === 'ABIERTA' ? 'selected' : ''}>ABIERTA</option>
                        <option value="EN_REVISION" ${incident.status === 'EN_REVISION' ? 'selected' : ''}>EN REVISIÓN</option>
                        <option value="RESUELTA" ${incident.status === 'RESUELTA' ? 'selected' : ''}>RESUELTA</option>
                    </select>
                </div>
            </div>
        `).join('');
    }

    async showProjectBeneficiariesModal(project, refreshBoard) {
        const modalId = 'projectBeneficiariesModal';
        this.removeModal(modalId);

        const modalElement = this.createModal({
            id: modalId,
            title: `Beneficiarios del proyecto: ${ProjectsComponent.escapeHtml(project.name)}`,
            size: 'modal-xl',
            body: `
                <div id="projectBeneficiaryFeedback" class="mb-3"></div>
                <div class="row g-4">
                    <div class="col-lg-5">
                        <div class="card shadow-sm h-100">
                            <div class="card-header bg-light">
                                <h6 class="mb-0"><i class="fas fa-user-plus me-2"></i>Agregar beneficiario</h6>
                            </div>
                            <div class="card-body">
                                <form id="createBeneficiaryForm">
                                    <div class="mb-3">
                                        <label class="form-label">Nombre</label>
                                        <input type="text" class="form-control" id="beneficiaryName" maxlength="32" required>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">Primer apellido</label>
                                            <input type="text" class="form-control" id="beneficiarySurname1" maxlength="24" required>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">Segundo apellido</label>
                                            <input type="text" class="form-control" id="beneficiarySurname2" maxlength="24" required>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">DNI</label>
                                            <input type="text" class="form-control" id="beneficiaryDni" maxlength="10" required>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">Código postal</label>
                                            <input type="number" class="form-control" id="beneficiaryPostalCode" min="0" step="1" required>
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label">Dirección</label>
                                        <input type="text" class="form-control" id="beneficiaryAddress" maxlength="128" required>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">Teléfono</label>
                                            <input type="tel" class="form-control" id="beneficiaryPhone" required>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label class="form-label">Email</label>
                                            <input type="email" class="form-control" id="beneficiaryEmail" maxlength="64" required>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-7">
                        <div class="card shadow-sm h-100">
                            <div class="card-header bg-light d-flex justify-content-between align-items-center">
                                <h6 class="mb-0"><i class="fas fa-people-group me-2"></i>Lista de beneficiarios</h6>
                                <span class="badge bg-dark" id="projectBeneficiariesCountBadge">0</span>
                            </div>
                            <div class="card-body">
                                <div id="projectBeneficiariesListContainer">
                                    <div class="text-center py-4">
                                        <div class="spinner-border text-primary" role="status"></div>
                                    </div>
                                </div>
                                <div id="projectBeneficiariesPaginationContainer" class="mt-3"></div>
                            </div>
                        </div>
                    </div>
                </div>
            `,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                <button type="button" class="btn btn-primary" id="confirmCreateBeneficiaryBtn">
                    <i class="fas fa-save me-2"></i>Guardar beneficiario
                </button>
            `
        });

        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        const listContainer = modalElement.querySelector('#projectBeneficiariesListContainer');
        const paginationContainer = modalElement.querySelector('#projectBeneficiariesPaginationContainer');
        const countBadge = modalElement.querySelector('#projectBeneficiariesCountBadge');
        const feedbackContainer = modalElement.querySelector('#projectBeneficiaryFeedback');
        const form = modalElement.querySelector('#createBeneficiaryForm');
        const confirmBtn = modalElement.querySelector('#confirmCreateBeneficiaryBtn');
        const dniInput = modalElement.querySelector('#beneficiaryDni');
        const beneficiaryPageState = {
            currentPage: 0,
            totalPages: 0,
            totalItems: 0
        };

        const clearFieldError = (input) => {
            if (!input) {
                return;
            }
            input.classList.remove('is-invalid');
            const feedback = input.parentNode?.querySelector('.invalid-feedback');
            if (feedback) {
                feedback.remove();
            }
        };

        const showFieldError = (input, message) => {
            if (!input) {
                return;
            }
            clearFieldError(input);
            input.classList.add('is-invalid');
            const feedback = document.createElement('div');
            feedback.className = 'invalid-feedback d-block';
            feedback.textContent = message;
            input.parentNode?.appendChild(feedback);
        };

        const validateBeneficiaryForm = () => {
            const dniValue = dniInput?.value || '';
            clearFieldError(dniInput);

            if (!Utils.validateDniNie(dniValue)) {
                showFieldError(dniInput, 'Introduce un DNI o NIE válido.');
                dniInput?.focus();
                return false;
            }

            dniInput.value = Utils.normalizeIdentityDocument(dniValue);
            return true;
        };

        const bindPagination = () => {
            modalElement.querySelectorAll('.beneficiary-page-btn').forEach(button => {
                button.addEventListener('click', async () => {
                    const nextPage = Number.parseInt(button.dataset.page, 10);
                    if (!Number.isInteger(nextPage)
                            || nextPage < 0
                            || nextPage >= beneficiaryPageState.totalPages
                            || nextPage === beneficiaryPageState.currentPage) {
                        return;
                    }
                    await reloadBeneficiaries(nextPage);
                });
            });
        };

        const reloadBeneficiaries = async (page = beneficiaryPageState.currentPage) => {
            const beneficiariesPage = await ProjectService.getProjectBeneficiaries(project.id, page, this.pageSize);
            const beneficiaries = Array.isArray(beneficiariesPage?.items) ? beneficiariesPage.items : [];

            beneficiaryPageState.currentPage = beneficiariesPage?.currentPage ?? page;
            beneficiaryPageState.totalPages = beneficiariesPage?.totalPages ?? 0;
            beneficiaryPageState.totalItems = beneficiariesPage?.totalItems ?? beneficiaries.length;

            this.kanbanState.beneficiaries = beneficiaries;
            this.kanbanState.beneficiariesTotal = beneficiaryPageState.totalItems;
            if (countBadge) {
                countBadge.textContent = String(beneficiaryPageState.totalItems);
            }
            if (listContainer) {
                listContainer.innerHTML = this.renderBeneficiariesTable(beneficiaries);
            }
            if (paginationContainer) {
                paginationContainer.innerHTML = this.renderInlinePagination(
                    beneficiaryPageState.currentPage,
                    beneficiaryPageState.totalPages,
                    'beneficiary-page-btn',
                    'Paginación de beneficiarios'
                );
            }
            bindPagination();
        };

        await reloadBeneficiaries();

        const getBeneficiaryPayload = () => ({
            name: modalElement.querySelector('#beneficiaryName')?.value?.trim() || '',
            surname1: modalElement.querySelector('#beneficiarySurname1')?.value?.trim() || '',
            surname2: modalElement.querySelector('#beneficiarySurname2')?.value?.trim() || '',
            dni: Utils.normalizeIdentityDocument(modalElement.querySelector('#beneficiaryDni')?.value || ''),
            address: modalElement.querySelector('#beneficiaryAddress')?.value?.trim() || '',
            postalCode: ProjectsComponent.parseNullableInt(modalElement.querySelector('#beneficiaryPostalCode')?.value),
            phone: ProjectsComponent.parseNullableInt(modalElement.querySelector('#beneficiaryPhone')?.value),
            email: modalElement.querySelector('#beneficiaryEmail')?.value?.trim() || ''
        });

        const submit = async () => {
            if (!form.reportValidity()) {
                return;
            }
            if (!validateBeneficiaryForm()) {
                return;
            }

            const originalHtml = confirmBtn.innerHTML;
            confirmBtn.disabled = true;
            confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Guardando...';

            try {
                const payload = getBeneficiaryPayload();
                const result = await ProjectService.createProjectBeneficiary(project.id, payload);

                if (result?.action === 'ALREADY_IN_PROJECT') {
                    if (feedbackContainer) {
                        feedbackContainer.innerHTML = `
                            <div class="alert alert-warning mb-0">
                                El usuario ya esta vinculado a este proyecto.
                            </div>
                        `;
                    }
                    return;
                }

                if (result?.action === 'EXISTS_IN_DATABASE' && result?.requiresConfirmation) {
                    const confirmed = await this.showConfirmModal({
                        title: 'Usuario existente',
                        body: `El usuario <strong>${ProjectsComponent.escapeHtml(ProjectsComponent.getBeneficiaryFullName(result.beneficiary || payload))}</strong> ya existe en la base de datos. ¿Quieres agregarlo a este proyecto?`,
                        confirmText: 'Sí, agregar al proyecto',
                        confirmClass: 'btn-primary'
                    });

                    if (!confirmed) {
                        if (feedbackContainer) {
                            feedbackContainer.innerHTML = `
                                <div class="alert alert-info mb-0">
                                    El usuario ya existe en la base de datos, pero no se ha agregado al proyecto.
                                </div>
                            `;
                        }
                        return;
                    }

                    const confirmedResult = await ProjectService.createProjectBeneficiary(project.id, payload, { confirmExisting: true });
                    form.reset();
                    await reloadBeneficiaries();
                    if (typeof refreshBoard === 'function') {
                        await refreshBoard();
                    }
                    if (feedbackContainer) {
                        feedbackContainer.innerHTML = `
                            <div class="alert alert-success mb-0">
                                ${ProjectsComponent.escapeHtml(confirmedResult?.message || 'El usuario se ha agregado al proyecto.')}
                            </div>
                        `;
                    }
                    return;
                }

                form.reset();
                await reloadBeneficiaries();
                if (typeof refreshBoard === 'function') {
                    await refreshBoard();
                }
                if (feedbackContainer) {
                    feedbackContainer.innerHTML = `
                        <div class="alert alert-success mb-0">
                            ${ProjectsComponent.escapeHtml(result?.message || 'El beneficiario se ha vinculado correctamente al proyecto.')}
                        </div>
                    `;
                }
            } catch (error) {
                if (feedbackContainer) {
                    feedbackContainer.innerHTML = `
                        <div class="alert alert-danger mb-0">
                            ${ProjectsComponent.escapeHtml(error.backendMessage || error.message || 'No se pudo guardar el beneficiario.')}
                        </div>
                    `;
                }
            } finally {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = originalHtml;
            }
        };

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            await submit();
        });
        confirmBtn.addEventListener('click', submit);
        dniInput?.addEventListener('input', () => clearFieldError(dniInput));
        dniInput?.addEventListener('blur', () => {
            if (!dniInput.value) {
                clearFieldError(dniInput);
                return;
            }
            dniInput.value = Utils.normalizeIdentityDocument(dniInput.value);
            if (!Utils.validateDniNie(dniInput.value)) {
                showFieldError(dniInput, 'Introduce un DNI o NIE válido.');
                return;
            }
            clearFieldError(dniInput);
        });
    }

    renderBeneficiariesTable(beneficiaries) {
        if (!Array.isArray(beneficiaries) || !beneficiaries.length) {
            return `
                <div class="text-center text-muted py-5 border rounded bg-body-tertiary">
                    <i class="fas fa-people-group mb-2 d-block fs-2"></i>
                    <span>Todavía no hay beneficiarios registrados en este proyecto.</span>
                </div>
            `;
        }

        const rows = beneficiaries.map(beneficiary => `
            <tr>
                <td><strong>${ProjectsComponent.escapeHtml(ProjectsComponent.getBeneficiaryFullName(beneficiary))}</strong></td>
                <td>${ProjectsComponent.escapeHtml(beneficiary.dni || '-')}</td>
                <td>${ProjectsComponent.escapeHtml(beneficiary.address || '-')}</td>
                <td>${ProjectsComponent.escapeHtml(beneficiary.postalCode || '-')}</td>
                <td>${ProjectsComponent.renderPhoneCell(beneficiary.phone)}</td>
                <td>${ProjectsComponent.renderEmailCell(beneficiary.email)}</td>
            </tr>
        `).join('');

        return `
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Nombre completo</th>
                            <th>DNI</th>
                            <th>Dirección</th>
                            <th>CP</th>
                            <th>Teléfono</th>
                            <th>Email</th>
                        </tr>
                    </thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>
        `;
    }

    renderInlinePagination(currentPage, totalPages, buttonClass, ariaLabel) {
        if (!totalPages || totalPages <= 1) {
            return '';
        }

        const pages = [];
        pages.push(`
            <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
                <button type="button" class="page-link ${buttonClass}" data-page="${currentPage - 1}">
                    Anterior
                </button>
            </li>
        `);

        for (let i = 0; i < totalPages; i++) {
            pages.push(`
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <button type="button" class="page-link ${buttonClass}" data-page="${i}">
                        ${i + 1}
                    </button>
                </li>
            `);
        }

        pages.push(`
            <li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
                <button type="button" class="page-link ${buttonClass}" data-page="${currentPage + 1}">
                    Siguiente
                </button>
            </li>
        `);

        return `
            <nav aria-label="${ProjectsComponent.escapeHtml(ariaLabel)}">
                <ul class="pagination pagination-sm justify-content-center mb-0">
                    ${pages.join('')}
                </ul>
            </nav>
        `;
    }

    showCreateTaskModal(project, refreshBoard) {
        const modalId = 'createTaskModal';
        this.removeModal(modalId);

        const team = this.kanbanState.team || [];
        const modalElement = this.createModal({
            id: modalId,
            title: `Nueva tarea para ${ProjectsComponent.escapeHtml(project.name)}`,
            size: 'modal-lg',
            body: `
                <form id="createTaskForm">
                    <div class="mb-3">
                        <label class="form-label">Título</label>
                        <input type="text" class="form-control" id="taskTitle" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Descripción</label>
                        <textarea class="form-control" id="taskDescription" rows="3"></textarea>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Fecha límite</label>
                            <input type="date" class="form-control" id="taskDueDate">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Fase inicial</label>
                            <select class="form-select" id="taskStatus">
                                <option value="POR_HACER">Por hacer</option>
                                <option value="EN_PROGRESO">En progreso</option>
                                <option value="TERMINADO">Terminado</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Asignar a</label>
                        ${this.renderAssigneeDropdown(team)}
                    </div>
                </form>
            `,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-primary" id="confirmCreateTaskBtn">
                    <i class="fas fa-save me-2"></i>Crear tarea
                </button>
            `
        });

        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        this.attachAssigneeDropdownListeners(modalElement);

        const confirmBtn = modalElement.querySelector('#confirmCreateTaskBtn');
        const form = modalElement.querySelector('#createTaskForm');

        const submit = async () => {
            if (!form.reportValidity()) {
                return;
            }

            const originalHtml = confirmBtn.innerHTML;
            confirmBtn.disabled = true;
            confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creando...';

            try {
                await ProjectService.createTask(project.id, {
                    title: modalElement.querySelector('#taskTitle')?.value?.trim() || '',
                    description: modalElement.querySelector('#taskDescription')?.value?.trim() || '',
                    dueDate: modalElement.querySelector('#taskDueDate')?.value || null,
                    status: modalElement.querySelector('#taskStatus')?.value || 'POR_HACER',
                    assignedStaffId: ProjectsComponent.parseNullableInt(modalElement.querySelector('#taskAssignedUserId')?.value)
                });
                modal.hide();
                await refreshBoard();
                await this.loadProjects();
                this.render();
            } catch (error) {
                Utils.showMessage('Error', ProjectsComponent.escapeHtml(error.backendMessage || error.message || 'No se pudo crear la tarea.'), 'error');
            } finally {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = originalHtml;
            }
        };

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            await submit();
        });
        confirmBtn.addEventListener('click', submit);
    }

    renderAssigneeDropdown(team) {
        return `
            <input type="hidden" id="taskAssignedUserId" value="">
            <div class="dropdown">
                <button class="btn btn-outline-secondary dropdown-toggle w-100 text-start d-flex align-items-center justify-content-between" type="button" id="taskAssigneeDropdownBtn" data-bs-toggle="dropdown" aria-expanded="false">
                    <span id="taskAssigneeDropdownLabel">Sin asignar</span>
                </button>
                <ul class="dropdown-menu w-100" aria-labelledby="taskAssigneeDropdownBtn">
                    <li>
                        <button type="button" class="dropdown-item assignee-option" data-staff-id="">
                            <i class="fas fa-user-slash me-2"></i>Sin asignar
                        </button>
                    </li>
                    ${team.map(staff => `
                        <li>
                            <button type="button" class="dropdown-item assignee-option d-flex align-items-center gap-2" data-staff-id="${staff.id}" data-staff-label="${ProjectsComponent.escapeHtml(ProjectsComponent.getStaffFullName(staff))}">
                                ${ProjectsComponent.renderStaffAvatar(staff, 28)}
                                <span>${ProjectsComponent.escapeHtml(ProjectsComponent.getStaffFullName(staff))}</span>
                            </button>
                        </li>
                    `).join('')}
                </ul>
            </div>
        `;
    }

    attachAssigneeDropdownListeners(modalElement) {
        const hiddenInput = modalElement.querySelector('#taskAssignedUserId');
        const label = modalElement.querySelector('#taskAssigneeDropdownLabel');

        modalElement.querySelectorAll('.assignee-option').forEach(button => {
            button.addEventListener('click', () => {
                const staffId = button.dataset.staffId || '';
                hiddenInput.value = staffId;
                label.innerHTML = button.innerHTML;
            });
        });
    }

    async confirmDeleteProject(project) {
        const confirmed = await this.showConfirmModal({
            title: 'Eliminar proyecto',
            body: `¿Seguro que quieres eliminar el proyecto <strong>${ProjectsComponent.escapeHtml(project.name)}</strong>? Esta acción no se puede deshacer.`,
            confirmText: 'Sí, eliminar',
            confirmClass: 'btn-danger'
        });

        if (!confirmed) {
            return;
        }

        await ProjectService.deleteProject(project.id);
        await this.loadProjects();
        this.render();
        Utils.showMessage('Proyecto eliminado', 'El proyecto se ha eliminado correctamente.', 'success');
    }

    createModal({ id, title, body, footer = '', size = 'modal-lg' }) {
        const wrapper = document.createElement('div');
        wrapper.innerHTML = `
            <div class="modal fade" id="${id}" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog ${size} modal-dialog-centered modal-dialog-scrollable">
                    <div class="modal-content">
                        <div class="modal-header bg-primary text-white">
                            <h5 class="modal-title">${title}</h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">${body}</div>
                        <div class="modal-footer">${footer}</div>
                    </div>
                </div>
            </div>
        `.trim();

        document.body.appendChild(wrapper.firstElementChild);
        return document.getElementById(id);
    }

    removeModal(id) {
        const existing = document.getElementById(id);
        if (existing) {
            existing.remove();
        }
    }

    async showConfirmModal({ title, body, confirmText, confirmClass }) {
        const modalId = 'genericConfirmModal';
        this.removeModal(modalId);
        const modalElement = this.createModal({
            id: modalId,
            title,
            size: 'modal-md',
            body,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn ${confirmClass}" id="confirmModalActionBtn">${confirmText}</button>
            `
        });

        return new Promise(resolve => {
            const modal = new bootstrap.Modal(modalElement);
            const actionBtn = modalElement.querySelector('#confirmModalActionBtn');

            modalElement.addEventListener('hidden.bs.modal', () => {
                modalElement.remove();
                resolve(false);
            }, { once: true });

            actionBtn?.addEventListener('click', () => {
                modal.hide();
                modalElement.remove();
                resolve(true);
            }, { once: true });

            modal.show();
        });
    }

    findProjectById(projectId) {
        const numericId = Number(projectId);
        return this.projects.find(project => Number(project.id) === numericId);
    }

    static escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    static formatDate(value) {
        if (!value) return '-';

        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return ProjectsComponent.escapeHtml(value);
        }

        return date.toLocaleDateString();
    }

    static formatDateTime(value) {
        if (!value) return '-';

        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return ProjectsComponent.escapeHtml(String(value));

        return date.toLocaleString();
    }

    static canCreateIncident(status) {
        const normalized = ProjectsComponent.normalizeTaskStatus(status);
        return normalized === 'POR_HACER' || normalized === 'EN_PROGRESO';
    }

    static getTaskStatusLabel(status) {
        switch (ProjectsComponent.normalizeTaskStatus(status)) {
            case 'POR_HACER':
                return 'Por hacer';
            case 'EN_PROGRESO':
                return 'En progreso';
            case 'TERMINADO':
                return 'Terminado';
            default:
                return status || 'Sin estado';
        }
    }

    static getTaskStatusBadgeClass(status) {
        switch (ProjectsComponent.normalizeTaskStatus(status)) {
            case 'POR_HACER':
                return 'text-bg-secondary';
            case 'EN_PROGRESO':
                return 'text-bg-warning';
            case 'TERMINADO':
                return 'text-bg-success';
            default:
                return 'text-bg-light';
        }
    }

    static getIncidentSeverityBadgeClass(severity) {
        switch ((severity || '').toUpperCase()) {
            case 'BAJA':
                return 'text-bg-info';
            case 'ALTA':
                return 'text-bg-danger';
            case 'CRITICA':
                return 'bg-dark text-white';
            case 'MEDIA':
            default:
                return 'text-bg-warning';
        }
    }

    static getIncidentStatusLabel(status) {
        switch ((status || 'ABIERTA').toUpperCase()) {
            case 'EN_REVISION':
                return 'EN REVISIÓN';
            case 'RESUELTA':
                return 'RESUELTA';
            case 'ABIERTA':
            default:
                return 'ABIERTA';
        }
    }

    static getIncidentStatusBadgeClass(status) {
        switch ((status || 'ABIERTA').toUpperCase()) {
            case 'RESUELTA':
                return 'text-bg-success';
            case 'EN_REVISION':
                return 'text-bg-primary';
            case 'ABIERTA':
            default:
                return 'text-bg-secondary';
        }
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
        const safeEmail = ProjectsComponent.escapeHtml(email);
        if (!safeEmail) return '-';
        return `<a href="mailto:${safeEmail}" class="text-decoration-none">${safeEmail}</a>`;
    }

    static renderPhoneCell(phone) {
        const safePhone = ProjectsComponent.escapeHtml(phone);
        if (!safePhone) return '-';

        const normalized = ProjectsComponent.normalizePhoneForWhatsApp(phone);
        if (!normalized) return safePhone;

        return `
            <a href="https://wa.me/${normalized}" target="_blank" rel="noopener noreferrer" class="text-decoration-none">
                <i class="fab fa-whatsapp me-1 text-success"></i>${safePhone}
            </a>
        `.trim();
    }

    static renderStaffAvatar(staff, size = 40) {
        const imagePath = staff?.profileImg
            ? `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.IMAGES}/${staff.profileImg}`
            : null;

        if (imagePath) {
            return `<img src="${imagePath}" alt="${ProjectsComponent.escapeHtml(staff.nick || 'Usuario')}" class="rounded-circle border" style="width: ${size}px; height: ${size}px; object-fit: cover;">`;
        }

        return `
            <div class="bg-secondary rounded-circle d-inline-flex align-items-center justify-content-center" style="width: ${size}px; height: ${size}px;">
                <i class="fas fa-user text-white"></i>
            </div>
        `.trim();
    }

    static getStaffFullName(staff) {
        return [staff?.name, staff?.surname1, staff?.surname2].filter(Boolean).join(' ').trim() || staff?.nick || 'Usuario';
    }

    static getBeneficiaryFullName(beneficiary) {
        return [beneficiary?.name, beneficiary?.surname1, beneficiary?.surname2].filter(Boolean).join(' ').trim() || 'Sin nombre';
    }

    static getStatusBadgeClass(status) {
        switch (status) {
            case 'ACTIVO':
                return 'bg-success';
            case 'PAUSADO':
                return 'bg-warning text-dark';
            case 'COMPLETADO':
                return 'bg-primary';
            case 'CANCELADO':
                return 'bg-danger';
            default:
                return 'bg-secondary';
        }
    }

    static normalizeTaskStatus(status) {
        switch ((status || '').toUpperCase()) {
            case 'TODO':
            case 'POR_HACER':
                return 'POR_HACER';
            case 'IN_PROGRESS':
            case 'EN_PROGRESO':
                return 'EN_PROGRESO';
            case 'DONE':
            case 'TERMINADO':
                return 'TERMINADO';
            default:
                return 'POR_HACER';
        }
    }

    static parseNullableInt(value) {
        if (value === null || value === undefined || value === '') {
            return null;
        }

        const numeric = Number(value);
        return Number.isFinite(numeric) ? numeric : null;
    }
}

window.ProjectsComponent = ProjectsComponent;

if (typeof AppScripts !== 'undefined') AppScripts.register('projects');
