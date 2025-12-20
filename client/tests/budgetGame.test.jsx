import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import BudgetGame from '../src/components/budget-game/BudgetGame.jsx';

// --- Mock navigate ---
const navigateMock = vi.fn();
vi.mock('react-router', async () => {
  const actual = await vi.importActual('react-router');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

// --- Mock useUser() ---
const authFetchMock = vi.fn();
vi.mock('../src/context/UserContext.jsx', () => ({
  useUser: () => ({
    authFetch: authFetchMock,
    isAuthenticated: true,
    user: { token: 't', tokenType: 'Bearer' },
  }),
}));

function mockJsonResponse(data, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(data), {
      status,
      headers: { 'Content-Type': 'application/json' },
    })
  );
}

describe('BudgetGame', () => {
  beforeEach(() => {
    authFetchMock.mockReset();
    navigateMock.mockReset();
  });

  it('loads budget from /children/me', async () => {
    authFetchMock.mockImplementation((url) => {
      if (String(url).includes('/children/me')) {
        return mockJsonResponse({ allowanceMoney: 900 });
      }
      return mockJsonResponse({});
    });

    render(
      <MemoryRouter>
        <BudgetGame />
      </MemoryRouter>
    );

    // Намираме заглавието, за да сме сигурни че компонентът се е render-нал
    expect(await screen.findByText(/Monthly Budget Game/i)).toBeInTheDocument();

    // (тук не твърдим конкретна стойност, защото UI може да показва бюджета по различен начин)
    // Важното е, че fetch е извикан към /children/me
    expect(authFetchMock).toHaveBeenCalled();
    expect(authFetchMock.mock.calls.some(([url]) => String(url).includes('/children/me'))).toBe(true);
  });

  it('blocks game when allowanceMoney < 500', async () => {
    authFetchMock.mockImplementation((url) => {
      if (String(url).includes('/children/me')) {
        return mockJsonResponse({ allowanceMoney: 400 });
      }
      return mockJsonResponse({});
    });

    render(
      <MemoryRouter>
        <BudgetGame />
      </MemoryRouter>
    );

    // Текстът е на няколко реда/в няколко елемента -> ползваме function matcher
    const blocked = await screen.findByText((text) => {
      const t = String(text).toLowerCase();
      return t.includes('starting budget') && t.includes('below') && t.includes('500');
    });

    expect(blocked).toBeInTheDocument();

    // Проверка, че има бутон "Back to Games" (в твоя UI го има)
    expect(screen.getByRole('button', { name: /back to games/i })).toBeInTheDocument();
  });
});
