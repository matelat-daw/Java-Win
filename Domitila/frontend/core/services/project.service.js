/**
 * project.service.js - Servicio para gestionar proyectos
 */

class ProjectService {
    static _normalizePageResponse(response) {
        const page = response?.data || response || {};
        return {
            items: Array.isArray(page?.items) ? page.items : [],
            currentPage: Number.isFinite(page?.currentPage) ? page.currentPage : 0,
            totalItems: Number.isFinite(page?.totalItems) ? page.totalItems : 0,
            totalPages: Number.isFinite(page?.totalPages) ? page.totalPages : 0,
            pageSize: Number.isFinite(page?.pageSize) ? page.pageSize : 8,
            hasNext: Boolean(page?.hasNext),
            hasPrevious: Boolean(page?.hasPrevious)
        };
    }

    static _normalizeListResponse(response) {
        if (Array.isArray(response)) {
            return response;
        }

        if (Array.isArray(response?.data)) {
            return response.data;
        }

        if (Array.isArray(response?.data?.items)) {
            return response.data.items;
        }

        return [];
    }

    /**
     * Obtiene todos los proyectos.
     * @returns {Promise<Object>}
     */
    static async getProjects(page = 0, size = 8) {
        const params = new URLSearchParams();
        params.set('page', String(page));
        params.set('size', String(size));
        const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.PROJECTS}?${params.toString()}`;
        const response = await Utils.makeRequest('GET', url, null, true);
        return ProjectService._normalizePageResponse(response);
    }

    /**
     * Crea un nuevo proyecto.
     * @param {Object} project
     * @returns {Promise<Object>}
     */
    static async createProject(project) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECTS;
        const response = await Utils.makeRequest('POST', url, {
            code: project?.code || null,
            name: project?.name ?? '',
            description: project?.description ?? '',
            startDate: project?.startDate ?? '',
            endDate: project?.endDate || null,
            status: project?.status ?? 'ACTIVO',
            type: project?.type ?? ''
        });

        Utils.clearCache();
        return response;
    }

    static async updateProject(projectId, project) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_BY_ID.replace(':id', projectId);
        const response = await Utils.makeRequest('PUT', url, {
            code: project?.code || null,
            name: project?.name ?? '',
            description: project?.description ?? '',
            startDate: project?.startDate ?? '',
            endDate: project?.endDate || null,
            status: project?.status ?? 'ACTIVO',
            type: project?.type ?? ''
        });

        Utils.clearCache();
        return response?.data || response;
    }

    static async deleteProject(projectId) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_BY_ID.replace(':id', projectId);
        const response = await Utils.makeRequest('DELETE', url);
        Utils.clearCache();
        return response;
    }

    static async getProjectTeam(projectId, options = {}) {
        const params = new URLSearchParams();
        const surname = typeof options?.surname === 'string' ? options.surname.trim() : '';
        if (surname) params.set('surname', surname);
        params.set('sortDir', options?.sortDir === 'desc' ? 'desc' : 'asc');

        const urlBase = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_TEAM.replace(':id', projectId);
        const url = params.toString() ? `${urlBase}?${params.toString()}` : urlBase;
        const response = await Utils.makeRequest('GET', url, null, true);
        return ProjectService._normalizeListResponse(response);
    }

    static async addProjectMember(projectId, staffId) {
        const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.PROJECT_TEAM.replace(':id', projectId)}/${staffId}`;
        const response = await Utils.makeRequest('POST', url);
        Utils.clearCache();
        return ProjectService._normalizeListResponse(response);
    }

    static async removeProjectMember(projectId, staffId) {
        const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.PROJECT_TEAM.replace(':id', projectId)}/${staffId}`;
        const response = await Utils.makeRequest('DELETE', url);
        Utils.clearCache();
        return ProjectService._normalizeListResponse(response);
    }

    static async getProjectTasks(projectId) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_TASKS.replace(':id', projectId);
        const response = await Utils.makeRequest('GET', url, null, true);
        return ProjectService._normalizeListResponse(response);
    }

    static async getProjectBeneficiaries(projectId, page = 0, size = 8) {
        const params = new URLSearchParams();
        params.set('page', String(page));
        params.set('size', String(size));
        const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.PROJECT_BENEFICIARIES.replace(':id', projectId)}?${params.toString()}`;
        const response = await Utils.makeRequest('GET', url, null, true);
        return ProjectService._normalizePageResponse(response);
    }

    static async createProjectBeneficiary(projectId, beneficiary, options = {}) {
        const params = new URLSearchParams();
        if (options?.confirmExisting) {
            params.set('confirmExisting', 'true');
        }

        const urlBase = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_BENEFICIARIES.replace(':id', projectId);
        const url = params.toString() ? `${urlBase}?${params.toString()}` : urlBase;
        const response = await Utils.makeRequest('POST', url, {
            name: beneficiary?.name ?? '',
            surname1: beneficiary?.surname1 ?? '',
            surname2: beneficiary?.surname2 ?? '',
            dni: beneficiary?.dni ?? '',
            address: beneficiary?.address ?? '',
            postalCode: beneficiary?.postalCode ?? null,
            phone: beneficiary?.phone ?? null,
            email: beneficiary?.email ?? ''
        });
        Utils.clearCache();
        return response?.data || response;
    }

    static async createTask(projectId, task) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_TASKS.replace(':id', projectId);
        const response = await Utils.makeRequest('POST', url, {
            title: task?.title ?? '',
            description: task?.description ?? '',
            status: task?.status ?? 'POR_HACER',
            dueDate: task?.dueDate || null,
            assignedStaffId: task?.assignedStaffId ?? null
        });
        Utils.clearCache();
        return response?.data || response;
    }

    static async updateTaskStatus(projectId, taskId, status) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_TASK_STATUS
            .replace(':projectId', projectId)
            .replace(':taskId', taskId);
        const response = await Utils.makeRequest('PUT', url, { status });
        Utils.clearCache();
        return response?.data || response;
    }

    static async createTaskIncident(projectId, taskId, incident) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_TASK_INCIDENTS
            .replace(':projectId', projectId)
            .replace(':taskId', taskId);
        const response = await Utils.makeRequest('POST', url, {
            title: incident?.title ?? '',
            description: incident?.description ?? '',
            severity: incident?.severity ?? 'MEDIA'
        });
        Utils.clearCache();
        return response?.data || response;
    }

    static async updateTaskIncidentStatus(projectId, taskId, incidentId, status) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECT_TASK_INCIDENT_STATUS
            .replace(':projectId', projectId)
            .replace(':taskId', taskId)
            .replace(':incidentId', incidentId);
        const response = await Utils.makeRequest('PUT', url, { status });
        Utils.clearCache();
        return response?.data || response;
    }
}

window.ProjectService = ProjectService;

if (typeof AppScripts !== 'undefined') AppScripts.register('project.service');
