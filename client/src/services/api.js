const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

class ApiError extends Error {
    constructor(message, status, errors = null) {
        super(message);
        this.status = status;
        this.errors = errors;
        this.name = 'ApiError';
    }
}

async function handleResponse(response) {
    const contentType = response.headers.get('content-type');
    const isJson = contentType && contentType.includes('application/json');
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
        const message = data.message || data.error || 'An error occurred';
        const errors = data.errors || null;
        throw new ApiError(message, response.status, errors);
    }

    return data;
}

async function apiRequest(endpoint, options = {}) {
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
        ...options,
    };

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
        return await handleResponse(response);
    } catch (error) {
        if (error instanceof ApiError) {
            throw error;
        }
        throw new ApiError('Network error. Please check your connection.', 0);
    }
}

export const authApi = {
    register: (data) =>
        apiRequest('/parents/register', {
            method: 'POST',
            body: JSON.stringify(data),
        }),

    login: (data) =>
        apiRequest('/parents/login', {
            method: 'POST',
            body: JSON.stringify(data),
        }),
};

export { ApiError };
