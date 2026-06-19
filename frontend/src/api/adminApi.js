import api from './axios';

export const getDashboardStats = () => api.get('/api/admin/dashboard/stats');

export const getDatasets = () => api.get('/api/admin/datasets');

export const getDatasetDetail = (id) => api.get(`/api/admin/datasets/${id}`);

export const uploadDataset = (formData) =>
  api.post('/api/admin/datasets/upload', formData);

export const assignAnnotators = (datasetId, annotatorIds) =>
  api.post(`/api/admin/datasets/${datasetId}/assign`, { annotatorIds });

export const removeAnnotator = (datasetId, userId) =>
  api.delete(`/api/admin/datasets/${datasetId}/annotators/${userId}`);

export const getAnnotations = (textItemId) =>
  api.get('/api/admin/annotations', { params: { textItemId } });

export const updateAnnotation = (id, data) =>
  api.put(`/api/admin/annotations/${id}`, data);

export const getUsers = () => api.get('/api/admin/users');

export const createUser = (userData) => api.post('/api/admin/users', userData);

export const updateUser = (id, userData) => api.put(`/api/admin/users/${id}`, userData);

export const deleteUser = (id) => api.delete(`/api/admin/users/${id}`);

export const getDatasetMetrics = (id) => api.get(`/api/admin/datasets/${id}/metrics`);

export const exportDataset = (id, format) =>
  api.get(`/api/admin/datasets/${id}/export?format=${format}`, { responseType: 'blob' });

export const trainModel = (formData) =>
  api.post('/api/admin/nlp/train', formData);

export const testModel = (formData) =>
  api.post('/api/admin/nlp/test', formData);

export const getNlpLogs = () => api.get('/api/admin/nlp/logs');

export const downloadModel = (logId) =>
  api.get(`/api/admin/nlp/models/${logId}/download`, { responseType: 'blob' });
