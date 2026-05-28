const templateUrl = new URL('./auth-panel.html', import.meta.url);

export async function renderAuthPanel(mount, options) {
    const response = await fetch(templateUrl);
    mount.innerHTML = await response.text();

    const form = mount.querySelector('[data-login-form]');
    const loginButton = mount.querySelector('[data-login-button]');
    const loginStatus = mount.querySelector('[data-login-status]');
    const usernameInput = mount.querySelector('#username');
    const passwordInput = mount.querySelector('#password');

    if (options.error) {
        loginStatus.textContent = options.error;
        loginStatus.classList.add('text-danger');
    }

    loginButton.disabled = Boolean(options.working);
    loginButton.textContent = options.working ? 'Validando...' : 'Entrar';

    form.addEventListener('submit', async event => {
        event.preventDefault();

        loginButton.disabled = true;
        loginButton.textContent = 'Validando...';
        loginStatus.textContent = 'Verificando credenciales...';
        loginStatus.classList.remove('text-danger');

        await options.onLogin({
            username: usernameInput.value.trim(),
            password: passwordInput.value,
        });
    });
}