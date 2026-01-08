import '@testing-library/jest-dom'

// Mock localStorage
const localStorageMock = {
  getItem: (key) => localStorageMock.store[key] || null,
  setItem: (key, value) => {
    localStorageMock.store[key] = value.toString();
  },
  removeItem: (key) => {
    delete localStorageMock.store[key];
  },
  clear: () => {
    localStorageMock.store = {};
  },
  store: {},
};

global.localStorage = localStorageMock;

// Mock sessionStorage
const sessionStorageMock = {
  getItem: (key) => sessionStorageMock.store[key] || null,
  setItem: (key, value) => {
    sessionStorageMock.store[key] = value.toString();
  },
  removeItem: (key) => {
    delete sessionStorageMock.store[key];
  },
  clear: () => {
    sessionStorageMock.store = {};
  },
  store: {},
};

global.sessionStorage = sessionStorageMock;

