import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Variable para rastrear si ya intentamos recargar el token
//let isRefreshing = false;
//let failedQueue: any[] = [];

// const processQueue = (error: any, token: string | null = null) => {
//   failedQueue.forEach(prom => {
//     if (error) {
//       prom.reject(error);
//     } else {
//       prom.resolve(token);
//     }
//   });
//   failedQueue = [];
// };

api.interceptors.request.use(
  (config) => {
    // IMPORTANTE: Quitar withCredentials si no es necesario
    // config.withCredentials = true; // <- COMENTA ESTA LÍNEA

    const token = localStorage.getItem('authToken');
    console.log('📤 REQUEST:', {
      url: config.url,
      method: config.method,
      hasToken: !!token,
    });

    // AGREGAR SIEMPRE EL TOKEN SI EXISTE
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      console.warn('⚠️ No hay token disponible. El endpoint puede requerir autenticación.');

      // Para endpoints que deberían funcionar sin auth, continuar sin token
      if (config.url?.includes('/clubs') || config.url?.includes('/courts')) {
        console.log('✅ Continuando sin token (endpoint debería ser público)');
      }
    }

    return config;
  },
  (error) => {
    console.error('❌ Error en request:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    console.log(`✅ RESPONSE ${response.status}: ${response.config.url}`);
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    const url = originalRequest?.url || '';
    const status = error.response?.status;

    console.error('❌ Error en response:', {
      url,
      status,
      message: error.message,
      data: error.response?.data
    });

    // Si es 401 y es un endpoint de clubes/canchas
    if (status === 401 && (url.includes('/clubs') || url.includes('/courts'))) {
      console.warn('⚠️ 401 en endpoint que debería ser público:', url);

      // IMPORTANTE: No redirigir, solo mostrar un mensaje amigable
      // Podemos retornar un error específico para manejar en el componente
      return Promise.reject(new Error('ENDPOINT_REQUIRES_AUTH'));
    }

    // Para otros endpoints, manejar autenticación normal
    if (status === 401 && !url.includes('/login') && !url.includes('/register')) {
      console.warn('🔒 401 Unauthorized - Token inválido o expirado');

      // Limpiar token
      localStorage.removeItem('authToken');
      localStorage.removeItem('userData');

      // Redirigir a login
      window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
    }

    return Promise.reject(error);
  }
);

export default api;