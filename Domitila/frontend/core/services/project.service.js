/**
 * project.service.js - Servicio para gestionar proyectos
 */

class ProjectService {
    /**
     * Obtiene todos los proyectos.
     * @returns {Promise<Array>}
     */
    static async getProjects() {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECTS;
        return Utils.makeRequest('GET', url, null, true);
    }

    /**
     * Crea un nuevo proyecto.
     * @param {Object} project
     * @returns {Promise<Object>}
     */
    static async createProject(project) {
        const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.PROJECTS;
        const response = await Utils.makeRequest('POST', url, {
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
}

window.ProjectService = ProjectService;

if (typeof AppScripts !== 'undefined') AppScripts.register('project.service');
