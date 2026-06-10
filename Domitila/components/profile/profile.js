import { escapeHtml, loadTemplate, renderTemplate } from '../shared/template.js';

export async function profileHtml({ profile, apiOrigin }) {
    const tpl = await loadTemplate(new URL('./profile.html', import.meta.url));
    const p = profile || {};

    const profileImg = String(p.profileImg || '').trim();
    const profileImageUrl = profileImg
        ? `${apiOrigin}/api/images/${encodeURIComponent(profileImg).replaceAll('%2F', '/')}`
        : `${apiOrigin}/api/images/default/other.png`;

    const status = (p.active === false || p.emailVerified === false)
        ? 'Inactivo / No verificado'
        : 'Activo / Verificado';

    const bday = p.bday ? escapeHtml(String(p.bday)) : '—';

    return renderTemplate(tpl, {
        profileImageUrl: escapeHtml(profileImageUrl),
        email: escapeHtml(p.email || ''),
        role: escapeHtml(p.role || ''),
        nick: escapeHtml(p.nick || ''),
        name: escapeHtml(p.name || ''),
        surname1: escapeHtml(p.surname1 || ''),
        surname2: escapeHtml(p.surname2 || '—'),
        phone: escapeHtml(p.phone || ''),
        gender: escapeHtml(p.gender || ''),
        bday,
        status: escapeHtml(status)
    });
}

