import { createContext, useContext, useState, useEffect } from 'react';

const UserContext = createContext(null);

export function UserProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            try {
                const parsed = JSON.parse(storedUser);
                setUser(parsed);
            } catch (error) {
                localStorage.removeItem('user');
            }
        }
        setLoading(false);
    }, []);

    const login = (userData) => {
        // userData is expected to include token and tokenType ("Bearer")
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('user');
    };

    // Build Authorization header from stored user
    const authHeaders = () => {
        if (!user?.token) return {};
        const type = user.tokenType || 'Bearer';
        return { Authorization: `${type} ${user.token}` };
    };

    // Wrapper around fetch that adds Authorization automatically
    const authFetch = async (url, options = {}) => {
        const headers = {
            ...(options.headers || {}),
            ...authHeaders(),
            'Content-Type': options?.headers?.['Content-Type'] || 'application/json',
        };

        const res = await fetch(url, { ...options, headers });
        return res;
    };

    const value = {
        user,
        login,
        logout,
        isAuthenticated: !!user,
        loading,
        authHeaders,
        authFetch,
    };

    return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
}

export function useUser() {
    const context = useContext(UserContext);
    if (!context) {
        throw new Error('useUser must be used within a UserProvider');
    }
    return context;
}