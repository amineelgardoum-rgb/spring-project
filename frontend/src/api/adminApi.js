import api from './axios';

export const getUsers = () => api.get('/api/admin/users');

export const createUser = (userData) => api.post('/api/admin/users', userData);

export const updateUser = (id, userData) => api.put(`/api/admin/users/${id}`, userData);

export const deleteUser = (id) => api.delete(`/api/admin/users/${id}`);

export const getDatasets = () => api.get('/api/admin/datasets');

export const uploadDataset = (formData) =>
  api.post('/api/admin/datasets', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const getDatasetDetail = (id) => api.get(`/api/admin/datasets/${id}`);

export const assignAnnotators = (datasetId, annotatorIds) =>
  api.post(`/api/admin/datasets/${datasetId}/assign`, { annotatorIds });

export const getMetrics = () => api.get('/api/admin/metrics');

export const exportData = (format) => api.get(`/api/admin/export?format=${format}`);

export const triggerTraining = (config) => api.post('/api/admin/nlp/train', config);
