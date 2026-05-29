window.TIENDA_API = {
    async request(path, options = {}) {
        const normalizedPath = path.replace(/^\/+/, '');
        const url = `${window.TIENDA_CONFIG.apiBase.replace(/\/+$/, '')}/${normalizedPath}`;

        const response = await fetch(url, {
            headers: {
                ...(options.headers || {})
            },
            ...options
        });

        const contentType = response.headers.get('content-type') || '';
        const payload = contentType.includes('application/json') ? await response.json() : await response.text();

        if (!response.ok) {
            const message = typeof payload === 'object' && payload !== null
                ? payload.message || Object.values(payload).join(', ')
                : payload || 'Error inesperado';
            throw new Error(message);
        }

        return payload;
    },

    getInfo() {
        return this.request('/store/info');
    },

    getCategories() {
        return this.request('/categories');
    },

    getProducts(category = '') {
        const query = category ? `?category=${encodeURIComponent(category)}` : '';
        return this.request(`/products${query}`);
    },

    getProduct(id) {
        return this.request(`/products/${id}`);
    },

    getSummary() {
        return this.request('/summary');
    },

    createProduct(formData) {
        return this.request('/products', {
            method: 'POST',
            body: formData
        });
    },

    updateProduct(id, formData) {
        return this.request(`/products/${id}`, {
            method: 'POST',
            body: formData
        });
    },

    deleteProduct(id) {
        return this.request(`/products/${id}`, {
            method: 'DELETE'
        });
    },

    sendContact(payload) {
        return this.request('/contact', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
    }
};
