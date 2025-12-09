import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

// Interceptor para agregar token a todas las requests
// En tu axiosConfig.ts, agrega logs
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    console.log('📤 Enviando request a:', config.url);
    console.log('🔑 Token encontrado:', token ? 'SÍ (' + token.substring(0, 20) + '...)' : 'NO');

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('✅ Authorization header agregado');
    }

    return config;
  },
  (error) => {
    console.error('❌ Error en interceptor:', error);
    return Promise.reject(error);
  }
);

// Interceptor para manejar respuestas
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Token expirado o inválido
            localStorage.removeItem('authToken');
            localStorage.removeItem('userData');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;