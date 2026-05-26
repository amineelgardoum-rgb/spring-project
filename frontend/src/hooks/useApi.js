import { useState, useCallback } from 'react';
import api from '../api/axios';

export function useApi(endpoint) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const execute = useCallback(async (...args) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api[endpoint.method || 'get'](endpoint.url, ...args);
      setData(res.data);
      return res.data;
    } catch (err) {
      setError(err.response?.data?.error || err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [endpoint.method, endpoint.url]);

  return { data, loading, error, execute };
}
