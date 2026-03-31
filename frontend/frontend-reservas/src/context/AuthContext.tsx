import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { User, UserRequest, UserResponse, LoginResponse } from '../api/types/user.types';
import { authService } from '../api/services/authService';
import api from "../api/axiosConfig.ts";

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isClubAdmin: boolean;
  isSuperAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  register: (userData: UserRequest) => Promise<UserResponse>;
  registerClubAdmin: (userData: UserRequest, clubData?: any) => Promise<UserResponse>;
  validateToken: () => Promise<boolean>;
  isLoading: boolean;
  error: string | null;
  clearError: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  // Estado inicial: cargar desde localStorage si existe
  const [user, setUser] = useState<User | null>(() => {
    try {
      const savedUser = localStorage.getItem('userData');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('authToken');
  });

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Computar propiedades basadas en el usuario
  const isAuthenticated = !!user && !!token;
  const isClubAdmin = user?.userRole === 'CLUB_ADMIN';
  const isSuperAdmin = user?.userRole === 'SUPER_ADMIN';

  // Función para limpiar errores
  const clearError = () => setError(null);

  // Función para validar token
  const validateToken = async (): Promise<boolean> => {
    const storedToken = localStorage.getItem('authToken');
    if (!storedToken) {
      return false;
    }

    try {
      const isValid = await authService.validate(storedToken);
      return isValid;
    } catch (error) {
      console.error('Error validating token:', error);
      return false;
    }
  };

  // Función auxiliar para guardar datos de autenticación
  const saveAuthData = (newToken: string, newUser: User) => {
    localStorage.setItem('authToken', newToken);
    localStorage.setItem('userData', JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
  };

  // Función auxiliar para limpiar datos de autenticación
  const clearAuthData = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setToken(null);
    setUser(null);
    setError(null);
  };

  // Inicializar autenticación al cargar
  useEffect(() => {
    const initializeAuth = async () => {
      setError(null);

      const storedToken = localStorage.getItem('authToken');
      const storedUser = localStorage.getItem('userData');

      if (storedToken && storedUser) {
        try {
          let isValid = true;

          // Validar token si el endpoint existe
          try {
            isValid = await validateToken();
          } catch (validationError) {
            console.warn('Token validation endpoint might not be implemented');
            // Para desarrollo, si falla la validación, asumimos válido
          }

          if (isValid) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);
            setToken(storedToken);
            console.log('Usuario autenticado desde localStorage:', parsedUser.userRole);
          } else {
            clearAuthData();
            console.log('Token inválido, limpiando datos');
          }
        } catch (error) {
          console.error('Error initializing auth:', error);
          clearAuthData();
        }
      }

      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);

    try {
      const response: LoginResponse = await authService.login({ email, password });

      if (!response.token) {
        throw new Error('No se recibió token de autenticación');
      }

      // Guardar datos de autenticación
      saveAuthData(response.token, response.user);

      console.log('Login exitoso, rol:', response.user.userRole);

    } catch (error: any) {
      console.error('Login error:', error);

      let errorMessage = 'Error al iniciar sesión';

      if (error.response) {
        if (error.response.status === 401) {
          errorMessage = 'Credenciales incorrectas';
        } else if (error.response.status === 403) {
          errorMessage = 'Cuenta no activa o sin permisos';
        } else if (error.response.status === 400) {
          errorMessage = 'Datos de entrada inválidos';
        } else if (error.response.data?.message) {
          errorMessage = error.response.data.message;
        }
      } else if (error.request) {
        errorMessage = 'No se pudo conectar con el servidor';
      } else {
        errorMessage = error.message || 'Error desconocido';
      }

      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    clearAuthData();
    console.log('Logout exitoso');
  };

  const register = async (userData: UserRequest): Promise<UserResponse> => {
    setIsLoading(true);
    setError(null);

    try {
      // Asegurar que los nuevos usuarios sean USER por defecto
      const registrationData = {
        ...userData,
        userRole: 'USER' as const,
        createdAt: new Date().toISOString(),
      };

      const response = await authService.register(registrationData);

      // Auto-login después del registro
      try {
        await login(userData.email, userData.password);
        console.log('Registro de usuario y login automático exitoso');
      } catch (loginError) {
        console.warn('Registro exitoso pero login automático falló:', loginError);
        // No lanzamos error aquí para no interrumpir el flujo
      }

      return response;

    } catch (error: any) {
      console.error('Register error:', error);

      let errorMessage = 'Error al registrarse';

      if (error.response) {
        if (error.response.status === 409) {
          errorMessage = 'El email ya está registrado';
        } else if (error.response.status === 400) {
          errorMessage = 'Datos de entrada inválidos';
        } else if (error.response.data?.message) {
          errorMessage = error.response.data.message;
        }
      } else if (error.request) {
        errorMessage = 'No se pudo conectar con el servidor';
      } else {
        errorMessage = error.message || 'Error desconocido';
      }

      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const registerClubAdmin = async (userData: UserRequest): Promise<UserResponse> => {
    setIsLoading(true);
    setError(null);

    try {
      // Asegurar que sea CLUB_ADMIN
      const registrationData = {
        ...userData,
        userRole: 'CLUB_ADMIN' as const,
        createdAt: new Date().toISOString(),
      };

      const response = await authService.register(registrationData);

      // Auto-login después del registro
      try {
        await login(userData.email, userData.password);
        console.log('Registro de club admin y login automático exitoso');
      } catch (loginError) {
        console.warn('Registro exitoso pero login automático falló:', loginError);
      }

      return response;

    } catch (error: any) {
      console.error('RegisterClubAdmin error:', error);

      let errorMessage = 'Error al registrar el club';

      if (error.response) {
        if (error.response.status === 409) {
          errorMessage = 'El email ya está registrado';
        } else if (error.response.status === 403) {
          errorMessage = 'No tienes permisos para crear una cuenta de club';
        } else if (error.response.status === 400) {
          errorMessage = 'Datos de entrada inválidos';
        } else if (error.response.data?.message) {
          errorMessage = error.response.data.message;
        }
      } else if (error.request) {
        errorMessage = 'No se pudo conectar con el servidor';
      } else {
        errorMessage = error.message || 'Error desconocido';
      }

      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // Interceptor para agregar token a las peticiones
  useEffect(() => {
    if (token) {
      // Aquí podrías configurar axios interceptor si usas axios
      // Ejemplo:
      api.interceptors.request.use(config => {
         config.headers.Authorization = `Bearer ${token}`;
         return config;
      });
    }
  }, [token]);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isClubAdmin,
        isSuperAdmin,
        login,
        logout,
        register,
        registerClubAdmin,
        validateToken,
        isLoading,
        error,
        clearError,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};