import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { useUser } from '../context/UserContext';

/**
 * Handles OAuth callback from Google.
 * Parses token from URL fragment and logs user in.
 */
export function GoogleCallbackPage() {
  const navigate = useNavigate();
  const { loginWithToken } = useUser();
  const [error, setError] = useState(null);

  useEffect(() => {
    const handleCallback = () => {
      // Parse URL fragment (format: #token=xxx&expiresAt=yyy&id=zzz...)
      const fragment = window.location.hash.substring(1);
      const params = new URLSearchParams(fragment);

      const token = params.get('token');
      const expiresAt = params.get('expiresAt');
      const id = params.get('id');
      const email = params.get('email');
      const firstName = params.get('firstName');
      const lastName = params.get('lastName');
      const role = params.get('role');

      // Check for errors in query string
      const searchParams = new URLSearchParams(window.location.search);
      const errorParam = searchParams.get('error');
      const errorMessage = searchParams.get('message');

      if (errorParam) {
        setError(errorMessage || 'Authentication failed. Please try again.');
        setTimeout(() => navigate('/login'), 3000);
        return;
      }

      if (!token) {
        setError('No authentication token received. Please try again.');
        setTimeout(() => navigate('/login'), 3000);
        return;
      }

      // Store auth data and redirect
      const userData = {
        id,
        email,
        firstName,
        lastName,
        role,
        token,
        expiresAt: parseInt(expiresAt),
      };

      console.log('Google OAuth successful, logging in user:', { email, role });
      loginWithToken(userData);
      navigate('/'); // Redirect to home page
    };

    handleCallback();
  }, [navigate, loginWithToken]);

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '50px', fontFamily: 'Arial, sans-serif' }}>
        <h2 style={{ color: '#d32f2f' }}>Authentication Failed</h2>
        <p style={{ color: '#666' }}>{error}</p>
        <p style={{ color: '#999', fontSize: '14px' }}>Redirecting to login...</p>
      </div>
    );
  }

  return (
    <div style={{ textAlign: 'center', padding: '50px', fontFamily: 'Arial, sans-serif' }}>
      <h2 style={{ color: '#4285f4' }}>Signing you in...</h2>
      <p style={{ color: '#666' }}>Please wait while we complete your authentication.</p>
      <div style={{ marginTop: '20px' }}>
        <div style={{
          border: '4px solid #f3f3f3',
          borderTop: '4px solid #4285f4',
          borderRadius: '50%',
          width: '40px',
          height: '40px',
          animation: 'spin 1s linear infinite',
          margin: '0 auto'
        }}></div>
      </div>
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
