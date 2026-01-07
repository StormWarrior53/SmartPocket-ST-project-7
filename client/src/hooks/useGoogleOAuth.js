import { useState } from 'react';

/**
 * Custom hook for Google OAuth login.
 * Handles server-side OAuth flow by redirecting to backend.
 *
 * @returns {Object} Object containing initiateGoogleLogin function, loading state, and error state
 */
export function useGoogleOAuth() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Initiates Google OAuth login flow.
   * Redirects user to backend OAuth endpoint which then redirects to Google.
   */
  const initiateGoogleLogin = () => {
    setIsLoading(true);
    setError(null);

    try {
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

      // Redirect to backend OAuth endpoint
      // Spring Security will handle the OAuth flow and redirect to Google
      const oauthUrl = `${apiBaseUrl.replace('/api', '')}/oauth2/authorization/google`;

      console.log('Redirecting to Google OAuth:', oauthUrl);
      window.location.href = oauthUrl;
    } catch (err) {
      console.error('Failed to initiate Google login:', err);
      setError(err.message);
      setIsLoading(false);
    }
  };

  return {
    initiateGoogleLogin,
    isLoading,
    error,
  };
}
