import { createContext, useContext, useState, useEffect, useCallback } from 'react';

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

    const isTokenExpired = useCallback(() => {
        if (!user?.expiresAt) return false;
        return Date.now() >= user.expiresAt;
    }, [user]);

    const login = (userData) => {
        // Validate that expiresAt is present and in the future
        if (!userData.expiresAt) {
            console.warn('Login response missing expiresAt timestamp');
        }
        if (userData.expiresAt && userData.expiresAt <= Date.now()) {
            console.error('Received expired token from server');
            return;
        }

        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
    };

    // Login with pre-generated token (for OAuth)
    const loginWithToken = (userData) => {
        if (!userData.token || !userData.expiresAt) {
            console.error('OAuth login data missing required fields');
            return;
        }

        // Validate token is not expired
        if (userData.expiresAt <= Date.now()) {
            console.error('Received expired token from OAuth');
            return;
        }

        const userWithToken = {
            id: userData.id,
            email: userData.email,
            firstName: userData.firstName,
            lastName: userData.lastName,
            role: userData.role,
            token: userData.token,
            tokenType: 'Bearer',
            expiresAt: userData.expiresAt,
        };

        setUser(userWithToken);
        localStorage.setItem('user', JSON.stringify(userWithToken));
        console.log('User logged in via OAuth:', { email: userWithToken.email, role: userWithToken.role });
    };

    const logout = (reason = null) => {
        setUser(null);
        localStorage.removeItem('user');
        if (reason) {
            sessionStorage.setItem('logoutReason', reason);
        }
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
        loginWithToken,
        logout,
        isAuthenticated: !!user,
        isTokenExpired,
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