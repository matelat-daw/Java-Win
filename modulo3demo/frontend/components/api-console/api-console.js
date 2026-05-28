const templateUrl = new URL('./api-console.html', import.meta.url);

export async function renderApiConsole(mount, options) {
    const response = await fetch(templateUrl);
    mount.innerHTML = await response.text();

    const statusBadge = mount.querySelector('[data-console-status]');
    const output = mount.querySelector('[data-console-output]');
    const endpointSelect = mount.querySelector('[data-endpoint-select]');
    const runButton = mount.querySelector('[data-run-request]');
    const quickButtons = Array.from(mount.querySelectorAll('[data-console-endpoint]'));

    statusBadge.textContent = options.working ? 'Procesando' : 'Lista';
    output.textContent = formatResponse(options.lastResponse);

    runButton.addEventListener('click', () => options.onRequest(endpointSelect.value));

    for (const button of quickButtons) {
        button.addEventListener('click', () => options.onRequest(button.dataset.consoleEndpoint));
    }
}

function formatResponse(lastResponse) {
    const content = typeof lastResponse.body === 'string'
        ? lastResponse.body
        : JSON.stringify(lastResponse.body, null, 2);

    return `${lastResponse.title}\n${lastResponse.status}\n\n${content}`;
}