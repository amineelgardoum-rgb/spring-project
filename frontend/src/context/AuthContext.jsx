import { createContext, useState, useCallback } from 'react';
import { login as loginApi, refreshToken as refreshApi } from '../api/authApi';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => localStorage.getItem('username') || null);
  const [token, setToken] = useState(() => localStorage.getItem('token') || null);
  const [refreshTokenValue, setRefreshTokenValue] = useState(() => localStorage.getItem('refreshToken') || null);
  const [role, setRole] = useState(() => localStorage.getItem('role') || null);

  const login = useCallback(async (username, password) => {
    const res = await loginApi(username, password);
    const { token, refreshToken, username: uname, role: r } = res.data;
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('username', uname);
    localStorage.setItem('role', r);
    setToken(token);
    setRefreshTokenValue(refreshToken);
    setUser(uname);
    setRole(r);
    return r;
  }, []);

  const refresh = useCallback(async () => {
    if (!refreshTokenValue) return false;
    try {
      const res = await refreshApi(refreshTokenValue);
      const { token: newToken, refreshToken: newRefresh, username: uname, role: r } = res.data;
      localStorage.setItem('token', newToken);
      localStorage.setItem('refreshToken', newRefresh);
      localStorage.setItem('username', uname);
      localStorage.setItem('role', r);
      setToken(newToken);
      setRefreshTokenValue(newRefresh);
      setUser(uname);
      setRole(r);
      return true;
    } catch {
      logout();
      return false;
    }
  }, [refreshTokenValue]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('darkMode');
    document.documentElement.classList.remove('dark');
    setToken(null);
    setRefreshTokenValue(null);
    setUser(null);
    setRole(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, role, login, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}
