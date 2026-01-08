import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';

import { useTokenExpiration } from '../src/hooks/useTokenExpiration.js';

// ---------- mocks ----------
const navigateMock = vi.fn();
let userMock = null;
const logoutMock = vi.fn();

vi.mock('react-router', async () => {
  const actual = await vi.importActual('react-router');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock('../src/context/UserContext', async () => {
  const actual = await vi.importActual('../src/context/UserContext');
  return {
    ...actual,
    useUser: () => ({
      user: userMock,
      logout: logoutMock,
    }),
  };
});

function wrapper({ children }) {
  return <MemoryRouter>{children}</MemoryRouter>;
}

describe('useTokenExpiration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    userMock = null;
  });

  afterEach(() => {
    vi.runOnlyPendingTimers();
    vi.useRealTimers();
  });

  it('does nothing if user has no expiresAt', () => {
    userMock = { token: 'abc', tokenType: 'Bearer' }; // No expiresAt

    const { result } = renderHook(() => useTokenExpiration(), { wrapper });

    act(() => {
      vi.advanceTimersByTime(60000); // Advance 1 minute
    });

    expect(logoutMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
    expect(result.current.handleExpiredToken).toBeDefined();
  });

  it('does nothing if user is null', () => {
    userMock = null;

    const { result } = renderHook(() => useTokenExpiration(), { wrapper });

    act(() => {
      vi.advanceTimersByTime(60000);
    });

    expect(logoutMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
    expect(result.current.handleExpiredToken).toBeDefined();
  });

  it('immediately logs out if token is already expired', async () => {
    const expiredTime = Date.now() - 10000; // 10 seconds ago
    userMock = {
      token: 'expired-token',
      tokenType: 'Bearer',
      expiresAt: expiredTime,
    };

    renderHook(() => useTokenExpiration(), { wrapper });

    await waitFor(() => {
      expect(logoutMock).toHaveBeenCalledWith('expired');
    });

    expect(navigateMock).toHaveBeenCalledWith('/login', {
      state: {
        message: 'Your session has expired. Please log in again.',
        returnUrl: window.location.pathname,
      },
    });
  });

  it('logs out when token expires during monitoring', async () => {
    const futureExpiry = Date.now() + 10000; // Expires in 10 seconds
    userMock = {
      token: 'soon-to-expire',
      tokenType: 'Bearer',
      expiresAt: futureExpiry,
    };

    renderHook(() => useTokenExpiration(), { wrapper });

    // Fast forward past expiry
    act(() => {
      vi.advanceTimersByTime(15000); // Advance 15 seconds
    });

    await waitFor(() => {
      expect(logoutMock).toHaveBeenCalledWith('expired');
    });

    expect(navigateMock).toHaveBeenCalledWith('/login', {
      state: {
        message: 'Your session has expired. Please log in again.',
        returnUrl: window.location.pathname,
      },
    });
  });

  it('shows warning when token is about to expire', async () => {
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => { });

    const futureExpiry = Date.now() + 4 * 60 * 1000; // Expires in 4 minutes
    userMock = {
      token: 'about-to-expire',
      tokenType: 'Bearer',
      expiresAt: futureExpiry,
    };

    renderHook(() => useTokenExpiration(5), { wrapper }); // Warning at 5 minutes

    // Advance to trigger warning check (30 seconds interval)
    act(() => {
      vi.advanceTimersByTime(30000);
    });

    await waitFor(() => {
      expect(consoleWarnSpy).toHaveBeenCalledWith(expect.stringContaining('Session expires in'));
    });

    consoleWarnSpy.mockRestore();
  });

  it('does not show duplicate warnings', async () => {
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => { });

    const futureExpiry = Date.now() + 4 * 60 * 1000; // Expires in 4 minutes
    userMock = {
      token: 'about-to-expire',
      tokenType: 'Bearer',
      expiresAt: futureExpiry,
    };

    renderHook(() => useTokenExpiration(5), { wrapper });

    // Advance multiple times
    act(() => {
      vi.advanceTimersByTime(30000); // First check
    });

    act(() => {
      vi.advanceTimersByTime(30000); // Second check
    });

    await waitFor(() => {
      expect(consoleWarnSpy).toHaveBeenCalledTimes(1);
    });

    consoleWarnSpy.mockRestore();
  });

  it('allows custom warning threshold', async () => {
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => { });

    const futureExpiry = Date.now() + 8 * 60 * 1000; // Expires in 8 minutes
    userMock = {
      token: 'long-lived',
      tokenType: 'Bearer',
      expiresAt: futureExpiry,
    };

    renderHook(() => useTokenExpiration(10), { wrapper }); // Warning at 10 minutes

    act(() => {
      vi.advanceTimersByTime(30000);
    });

    await waitFor(() => {
      expect(consoleWarnSpy).toHaveBeenCalledWith(expect.stringContaining('Session expires in'));
    });

    consoleWarnSpy.mockRestore();
  });

  it('checks expiration every 30 seconds', async () => {
    const futureExpiry = Date.now() + 60 * 60 * 1000; // Expires in 1 hour
    userMock = {
      token: 'valid-token',
      tokenType: 'Bearer',
      expiresAt: futureExpiry,
    };

    const { unmount } = renderHook(() => useTokenExpiration(), { wrapper });

    // Advance several intervals
    act(() => {
      vi.advanceTimersByTime(30000); // 30 seconds
    });

    act(() => {
      vi.advanceTimersByTime(30000); // Another 30 seconds
    });

    // Should not have logged out yet (token still valid)
    expect(logoutMock).not.toHaveBeenCalled();

    unmount();
  });

  it('cleans up interval on unmount', () => {
    const futureExpiry = Date.now() + 60 * 60 * 1000;
    userMock = {
      token: 'valid-token',
      tokenType: 'Bearer',
      expiresAt: futureExpiry,
    };

    const { unmount } = renderHook(() => useTokenExpiration(), { wrapper });

    unmount();

    // Advance timers after unmount
    act(() => {
      vi.advanceTimersByTime(60000);
    });

    // Should not trigger logout after unmount
    expect(logoutMock).not.toHaveBeenCalled();
  });

  it('handleExpiredToken can be called manually with custom reason', async () => {
    userMock = { token: 'abc', tokenType: 'Bearer' };

    const { result } = renderHook(() => useTokenExpiration(), { wrapper });

    act(() => {
      result.current.handleExpiredToken('server-rejected');
    });

    await waitFor(() => {
      expect(logoutMock).toHaveBeenCalledWith('server-rejected');
    });

    expect(navigateMock).toHaveBeenCalledWith('/login', {
      state: {
        message: 'Authentication failed. Please log in again.',
        returnUrl: window.location.pathname,
      },
    });
  });
});
