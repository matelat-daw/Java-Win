const templateUrl = new URL('./shell.html', import.meta.url);

export async function renderShell(mount, handlers) {
    const response = await fetch(templateUrl);
    mount.innerHTML = await response.text();

    const root = mount.querySelector('.app-shell');
    const mainSlot = mount.querySelector('[data-main-slot]');
    const summarySlot = mount.querySelector('[data-summary-slot]');
    const consoleSlot = mount.querySelector('[data-console-slot]');
    const userName = mount.querySelector('[data-user-name]');
    const userRoles = mount.querySelector('[data-user-roles]');
    const sessionBadge = mount.querySelector('[data-session-badge]');
    const workingIndicator = mount.querySelector('[data-working-indicator]');
    const bannerEyebrow = mount.querySelector('[data-banner-eyebrow]');
    const bannerTitle = mount.querySelector('[data-banner-title]');
    const bannerDescription = mount.querySelector('[data-banner-description]');
    const navButtons = Array.from(mount.querySelectorAll('[data-nav]'));
    const logoutButton = mount.querySelector('[data-action-logout]');
    const apiButton = mount.querySelector('[data-action-nav="api"]');

    for (const button of navButtons) {
        button.addEventListener('click', () => handlers.onNavigate(button.dataset.nav));
    }

    apiButton.addEventListener('click', () => handlers.onNavigate('api'));
    logoutButton.addEventListener('click', () => handlers.onLogout());

    return {
        root,
        mainSlot,
        summarySlot,
        consoleSlot,
        setNavigation(section, session) {
            navButtons.forEach(button => {
                button.classList.toggle('active', button.dataset.nav === section);
            });

            apiButton.classList.toggle('disabled', !session);
            logoutButton.disabled = !session;
        },
        setSession(session) {
            if (!session) {
                userName.textContent = 'Sin autenticación';
                userRoles.textContent = 'Usa usuario / 1234 o admin / admin123';
                sessionBadge.textContent = 'Invitado';
                sessionBadge.className = 'badge rounded-pill text-bg-secondary';
                return;
            }

            userName.textContent = session.username;
            userRoles.textContent = session.roles.length > 0 ? session.roles.join(' · ') : 'Sin roles';
            sessionBadge.textContent = session.roles.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER';
            sessionBadge.className = session.roles.includes('ROLE_ADMIN')
                ? 'badge rounded-pill text-bg-warning text-dark'
                : 'badge rounded-pill text-bg-success';
        },
        setBanner({ eyebrow, title, description }) {
            bannerEyebrow.textContent = eyebrow;
            bannerTitle.textContent = title;
            bannerDescription.textContent = description;
        },
        setLastResponse(lastResponse) {
            summarySlot.innerHTML = `
                <section class="app-card">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <h2 class="card-title">Última respuesta</h2>
                        <span class="badge ${lastResponse.ok === null ? 'text-bg-secondary' : lastResponse.ok ? 'text-bg-success' : 'text-bg-danger'}">${lastResponse.status}</span>
                    </div>
                    <div class="small muted-copy mb-2">${lastResponse.title}</div>
                    <pre class="response-box p-3 mb-0">${escapeHtml(lastResponse.body)}</pre>
                </section>
            `;
        },
        setWorking(isWorking) {
            workingIndicator.textContent = isWorking ? 'Procesando...' : 'Listo';
            workingIndicator.className = isWorking
                ? 'badge rounded-pill text-bg-info text-dark'
                : 'badge rounded-pill text-bg-dark border border-white border-opacity-10';
        },
    };
}

function escapeHtml(text) {
    return String(text)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}