import api from './axios';

export const getTasks = () => api.get('/api/annotator/tasks');

export const getTaskDetail = (taskId) => api.get(`/api/annotator/tasks/${taskId}/pairs`);

export const submitAnnotation = (taskId, textItemId, label) =>
  api.post(`/api/annotator/tasks/${taskId}/annotate`, { textItemId, label });

export const getStats = () => api.get('/api/annotator/stats');
