import api from './axios';

export const login = (username, password) =>
  api.post('/api/auth/login', { username, password });

export const refreshToken = (refreshToken) =>
  api.post('/api/auth/refresh', { refreshToken });
