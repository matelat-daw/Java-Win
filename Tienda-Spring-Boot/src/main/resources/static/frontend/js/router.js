window.TIENDA_ROUTER = {
    routes: {
        inicio: 'components/home/home.html',
        catalogo: 'components/catalogo/catalogo.html',
        detalle: 'components/detalle/detalle.html',
        nuevo: 'components/nuevo-producto/nuevo-producto.html',
        editar: 'components/editar-producto/editar-producto.html',
        resumen: 'components/resumen/resumen.html',
        contacto: 'components/contacto/contacto.html'
    },

    parse() {
        const hash = (location.hash || '#/inicio').replace(/^#\/?/, '');
        const segments = hash.split('/').filter(Boolean);
        const page = segments[0] || 'inicio';
        const id = segments[1] || null;
        return { page, id };
    },

    async render() {
        const app = document.getElementById('app');
        const { page, id } = this.parse();
        const templatePath = this.routes[page] || this.routes.inicio;
        const response = await fetch(`${window.TIENDA_CONFIG.frontendBase}${templatePath}`);
        const html = await response.text();
        app.innerHTML = html;

        document.querySelectorAll('[data-route]').forEach(link => {
            link.classList.toggle('active', link.dataset.route === page);
        });

        const mount = window.TIENDA_COMPONENTS && window.TIENDA_COMPONENTS[page];
        if (typeof mount === 'function') {
            await mount({ app, page, id });
        }
    }
};
