// tests/userContext.test.jsx
import React from 'react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import { UserProvider, useUser } from '../src/context/UserContext.jsx';

function TestConsumer() {
  const { user, login, logout, isAuthenticated, authHeaders, authFetch, loading } = useUser();

  return (
    <div>
      <div data-testid="loading">{String(loading)}</div>
      <div data-testid="auth">{String(isAuthenticated)}</div>
      <div data-testid="user">{user ? JSON.stringify(user) : 'null'}</div>
      <div data-testid="headers">{JSON.stringify(authHeaders())}</div>

      {/* ✅ login с expiresAt */}
      <button
        onClick={() =>
          login({
            token: 'abc',
            tokenType: 'Bearer',
            email: 'x@test.com',
            expiresAt: Date.now() + 60 * 60 * 1000, // +1h
          })
        }
      >
        login
      </button>

      <button onClick={() => logout()}>logout</button>

      {/* ✅ setUser също с expiresAt */}
      <button
        onClick={() =>
          login({
            token: 't123',
            tokenType: 'Bearer',
            expiresAt: Date.now() + 60 * 60 * 1000, // +1h
          })
        }
      >
        setUser
      </button>

      <button
        onClick={async () => {
          await authFetch('/test', { method: 'GET' });
        }}
      >
        callAuthFetch
      </button>

      <button
        onClick={async () => {
          await authFetch('/test2', {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain', 'X-Custom': '1' },
            body: 'hello',
          });
        }}
      >
        callAuthFetchCustom
      </button>
    </div>
  );
}

function renderWithProvider() {
  return render(
    <UserProvider>
      <TestConsumer />
    </UserProvider>
  );
}

describe('UserContext', () => {
  let fetchMock;

  beforeEach(() => {
    localStorage.clear();

    fetchMock = vi.fn(async () => ({
      ok: true,
      status: 200,
      json: async () => ({ ok: true }),
      text: async () => 'ok',
    }));

    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads user from localStorage on mount (valid JSON)', async () => {
    localStorage.setItem('user', JSON.stringify({ token: 'stored', tokenType: 'Bearer' }));

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('true');
    });

    expect(screen.getByTestId('user').textContent).toContain('"token":"stored"');
  });

  it('removes invalid JSON from localStorage', async () => {
    localStorage.setItem('user', '{ invalid json');

    renderWithProvider();

    await waitFor(() => {
      // След mount -> loading става false
      expect(screen.getByTestId('loading').textContent).toBe('false');
    });

    expect(localStorage.getItem('user')).toBe(null);
    expect(screen.getByTestId('auth').textContent).toBe('false');
  });

  it('login saves user to state + localStorage', async () => {
    const user = userEvent.setup();
    renderWithProvider();

    await user.click(screen.getByText('login'));

    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('true');
    });

    expect(localStorage.getItem('user')).toContain('"token":"abc"');
  });

  it('logout clears user from state + localStorage', async () => {
    const user = userEvent.setup();
    renderWithProvider();

    await user.click(screen.getByText('login'));
    await waitFor(() => expect(screen.getByTestId('auth').textContent).toBe('true'));

    await user.click(screen.getByText('logout'));

    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('false');
    });

    expect(localStorage.getItem('user')).toBe(null);
  });

  it('authHeaders returns Authorization when token exists', async () => {
    const user = userEvent.setup();
    renderWithProvider();

    await user.click(screen.getByText('setUser'));

    await waitFor(() => {
      expect(screen.getByTestId('headers').textContent).toContain('Bearer t123');
    });
  });

  it('authFetch adds Authorization + default Content-Type application/json', async () => {
    const user = userEvent.setup();
    renderWithProvider();

    await user.click(screen.getByText('setUser'));

    await user.click(screen.getByText('callAuthFetch'));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalled();
    });

    const [url, options] = fetchMock.mock.calls[0];

    expect(url).toBe('/test');
    expect(options.headers.Authorization).toBe('Bearer t123');
    expect(options.headers['Content-Type']).toBe('application/json');
  });

  it('authFetch keeps custom Content-Type if provided', async () => {
    const user = userEvent.setup();
    renderWithProvider();

    await user.click(screen.getByText('setUser'));

    await user.click(screen.getByText('callAuthFetchCustom'));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalled();
    });

    const [url, options] = fetchMock.mock.calls[0];

    expect(url).toBe('/test2');
    expect(options.headers.Authorization).toBe('Bearer t123');
    expect(options.headers['Content-Type']).toBe('text/plain');
    expect(options.headers['X-Custom']).toBe('1');
  });

  it('useUser throws if used outside provider', () => {
    function Bad() {
      useUser();
      return null;
    }

    expect(() => render(<Bad />)).toThrow('useUser must be used within a UserProvider');
  });
});
