/**
 * projects.component.js - Componente de listado y alta de proyectos
 */

class ProjectsComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.projects = [];
        this.projectFormTemplate = null;
    }

    async init() {
        try {
            if (!AuthService.isAuthenticated()) {
                App.getInstance().navigateTo('/login');
                return;
            }

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
        const response = await ProjectService.getProjects();

        if (Array.isArray(response)) {
            this.projects = response;
            return;
        }

        if (response?.data && Array.isArray(response.data.items)) {
            this.projects = response.data.items;
            return;
        }

        this.projects = [];
    }

    render() {
        const currentUser = AuthService.getUserSession();
        const userName = currentUser ? `${currentUser.name} ${currentUser.surname1}` : 'Usuario';
        const html = `
            <div class="container-fluid py-5">
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="fas fa-folder-open me-2"></i>
                            <strong>¡Bienvenido Usuario: ${ProjectsComponent.escapeHtml(userName)}!</strong>
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
                            <p class="text-muted mb-0">Total de proyectos: <strong>${this.projects.length}</strong></p>
                        </div>
                        <button type="button" class="btn btn-primary" id="addProjectBtn">
                            <i class="fas fa-folder-plus me-2"></i>Agregar proyecto
                        </button>
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
            </div>
        `;

        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = html;
            this.attachEventListeners();
        }
    }

    renderTable() {
        const rows = this.projects.map(project => `
            <tr>
                <td><strong>${ProjectsComponent.escapeHtml(project.code || '-')}</strong></td>
                <td>${ProjectsComponent.escapeHtml(project.name || '-')}</td>
                <td>${ProjectsComponent.escapeHtml(project.description || '-')}</td>
                <td>${ProjectsComponent.escapeHtml(project.type || '-')}</td>
                <td>
                    <span class="badge ${ProjectsComponent.getStatusBadgeClass(project.status)}">
                        ${ProjectsComponent.escapeHtml(project.status || 'SIN ESTADO')}
                    </span>
                </td>
                <td>${ProjectsComponent.formatDate(project.startDate)}</td>
                <td>${ProjectsComponent.formatDate(project.endDate)}</td>
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
                            <th>Fecha Inicio</th>
                            <th>Fecha Fin</th>
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
}

window.ProjectsComponent = ProjectsComponent;

if (typeof AppScripts !== 'undefined') AppScripts.register('projects');
