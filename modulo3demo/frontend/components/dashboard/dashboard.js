const templateUrl = new URL('./dashboard.html', import.meta.url);

export async function renderDashboard(mount, options) {
    const response = await fetch(templateUrl);
    mount.innerHTML = await response.text();

    const userMetric = mount.querySelector('[data-metric-user]');
    const rolesMetric = mount.querySelector('[data-metric-roles]');
    const stateMetric = mount.querySelector('[data-metric-state]');
    const roleBadge = mount.querySelector('[data-role-badge]');
    const endpointButtons = Array.from(mount.querySelectorAll('[data-endpoint]'));

    function syncSession(session) {
        userMetric.textContent = session.username;
        rolesMetric.textContent = session.roles.join(', ');
        stateMetric.textContent = session.roles.includes('ROLE_ADMIN') ? 'Administrador' : 'Autenticado';
        roleBadge.textContent = session.roles.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER';
        roleBadge.className = session.roles.includes('ROLE_ADMIN') ? 'badge rounded-pill text-bg-warning text-dark' : 'badge rounded-pill text-bg-success';
    }

    syncSession(options.session);

    for (const button of endpointButtons) {
        button.addEventListener('click', () => options.onQuickRequest(button.dataset.endpoint));
    }

    return {
        syncSession,
    };
}