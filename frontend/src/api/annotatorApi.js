import api from './axios';

export const getTasks = () => api.get('/api/annotator/tasks');

export const getTaskDetail = (taskId, page = 0) =>
  api.get(`/api/annotator/tasks/${taskId}/pairs`, { params: { page, size: 1 } });

export const submitAnnotation = (taskId, textItemId, label, duration) =>
  api.post(`/api/annotator/tasks/${taskId}/annotate`, { textItemId, label, duration });

export const getStats = () => api.get('/api/annotator/stats');
