import { useEffect, useRef, useCallback } from 'react';
import { useUser } from '../context/UserContext';
import { useNavigate } from 'react-router';

/**
 * Hook to monitor token expiration and auto-logout
 * @param {number} warningMinutes - Minutes before expiration to show warning (default: 5)
 */
export function useTokenExpiration(warningMinutes = 5) {
  const { user, logout } = useUser();
  const navigate = useNavigate();
  const checkIntervalRef = useRef(null);
  const warningShownRef = useRef(false);

  const handleExpiredToken = useCallback((reason = 'expired') => {
    logout(reason);
    const message = reason === 'expired'
      ? 'Your session has expired. Please log in again.'
      : 'Authentication failed. Please log in again.';

    navigate('/login', {
      state: {
        message,
        returnUrl: window.location.pathname
      }
    });
  }, [logout, navigate]);

  useEffect(() => {
    if (!user?.expiresAt) return;

    const checkExpiration = () => {
      const now = Date.now();
      const expiresAt = user.expiresAt;
      const timeUntilExpiry = expiresAt - now;

      // Token expired
      if (timeUntilExpiry <= 0) {
        clearInterval(checkIntervalRef.current);
        handleExpiredToken('expired');
        return;
      }

      const warningThreshold = warningMinutes * 60 * 1000;
      if (timeUntilExpiry <= warningThreshold && !warningShownRef.current) {
        warningShownRef.current = true;
        const minutesLeft = Math.ceil(timeUntilExpiry / 60000);
        console.warn(`Session expires in ${minutesLeft} minute(s)`);
      }
    };

    // Check immediately
    checkExpiration();

    // Check every 30 seconds
    checkIntervalRef.current = setInterval(checkExpiration, 30000);

    return () => {
      if (checkIntervalRef.current) {
        clearInterval(checkIntervalRef.current);
      }
      warningShownRef.current = false;
    };
  }, [user, warningMinutes, handleExpiredToken]);

  return { handleExpiredToken };
}
