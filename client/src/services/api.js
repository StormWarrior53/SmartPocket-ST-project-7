const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

class ApiError extends Error {
    constructor(message, status, errors = null) {
        super(message);
        this.status = status;
        this.errors = errors;
        this.name = 'ApiError';
    }
}

let logoutCallback = null;

export function setApiLogoutCallback(callback) {
    logoutCallback = callback;
}

async function request(url, options = {}) {
    const res = await fetch(url, {
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options,
    });
    let body = null;
    try { body = await res.json(); } catch { }
    if (!res.ok) {
        const msg = body?.message || body?.error || `Request failed: ${res.status}`;
        throw new ApiError(msg, res.status, body?.errors || null);
    }
    return body;
}

async function handleResponse(response) {
    const contentType = response.headers.get('content-type');
    const isJson = contentType && contentType.includes('application/json');
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
        const message = data.message || data.error || 'An error occurred';
        const errors = data.errors || null;

        // Handle authentication errors
        if (response.status === 401 || response.status === 403) {
            if (logoutCallback) {
                logoutCallback('server-rejected');
            }
        }

        throw new ApiError(message, response.status, errors);
    }

    return data;
}

async function apiRequest(endpoint, options = {}) {
    // Get auth token from localStorage
    const authHeaders = {};
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
        try {
            const user = JSON.parse(storedUser);
            if (user?.token) {
                const tokenType = user.tokenType || 'Bearer';
                authHeaders.Authorization = `${tokenType} ${user.token}`;
            }
        } catch (e) {
            console.error('Failed to parse stored user:', e);
        }
    }

    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders,
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

    // keep a single parent login endpoint
    login: (data) =>
        apiRequest('/parents/login', {
            method: 'POST',
            body: JSON.stringify(data),
        }),
};

export const childApi = {
    setupPattern: (data) =>
        request(`${API_BASE_URL}/children/setup-pattern`, {
            method: "POST",
            body: JSON.stringify(data),
        }),
    login: (data) =>
        request(`${API_BASE_URL}/children/login`, {
            method: "POST",
            body: JSON.stringify(data),
        }),
    logout: () =>
        request(`${API_BASE_URL}/children/logout`, {
            method: "POST",
            body: JSON.stringify({}),
        }),
};

export const quizApi = {
    getQuizByExerciseId: (exerciseId) =>
        apiRequest(`/exercises/${exerciseId}/quiz`, {
            method: 'GET',
        }),
    submitQuiz: (quizId, answers) =>
        apiRequest(`/quizzes/${quizId}/submit`, {
            method: 'POST',
            body: JSON.stringify({ answers }),
        }),
    getAllQuizzes: () =>
        apiRequest('/quizzes', {
            method: 'GET',
        }),
    getQuizById: (quizId) =>
        apiRequest(`/quizzes/${quizId}`, {
            method: 'GET',
        }),
    createQuiz: (quizData) =>
        apiRequest('/quizzes', {
            method: 'POST',
            body: JSON.stringify(quizData),
        }),
    updateQuiz: (quizId, quizData) =>
        apiRequest(`/quizzes/${quizId}`, {
            method: 'PUT',
            body: JSON.stringify(quizData),
        }),
    deleteQuiz: (quizId, force = false) =>
        apiRequest(`/quizzes/${quizId}?force=${force}`, {
            method: 'DELETE',
        }),
    getQuizAttempts: (quizId) =>
        apiRequest(`/quizzes/${quizId}/attempts`, {
            method: 'GET',
        }),
    getMyQuizAttempts: () =>
        apiRequest('/children/me/quiz-attempts', {
            method: 'GET',
        }),
};

// Budget Game Configuration API
export const budgetGameApi = {
    /**
     * Get the currently active game configuration
     * Called by BudgetGame.jsx when the game loads
     */
    getActiveConfig: () =>
        apiRequest('/budget-game-config/active', { method: 'GET' }),

    /**
     * Admin functions - for the admin panel
     */
    getAllConfigs: () =>
        apiRequest('/budget-game-configs', { method: 'GET' }),

    getConfigById: (id) =>
        apiRequest(`/budget-game-configs/${id}`, { method: 'GET' }),

    createConfig: (data) =>
        apiRequest('/budget-game-configs', {
            method: 'POST',
            body: JSON.stringify(data),
        }),

    updateConfig: (id, data) =>
        apiRequest(`/budget-game-configs/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        }),

    deleteConfig: (id) =>
        apiRequest(`/budget-game-configs/${id}`, { method: 'DELETE' }),

    setActiveConfig: (id) =>
        apiRequest(`/budget-game-configs/${id}/activate`, { method: 'PATCH' }),
};

export { ApiError };