/**
 * project.service.js - Servicio para gestionar proyectos
 */

class ProjectService {
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
     * @returns {Promise<Array>}
     */
    static async getProjects() {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECTS;
        const response = await Utils.makeRequest('GET', url, null, true);
        return ProjectService._normalizeListResponse(response);
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
