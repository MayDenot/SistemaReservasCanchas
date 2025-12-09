import React, {createContext, useContext, useState, useEffect, type ReactNode} from 'react';
import type {User, UserRequest, UserResponse, LoginResponse} from '../api/types/user.types';
import {authService} from '../api/services/authService';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  register: (userData: UserRequest) => Promise<UserResponse>;
  validateToken: () => Promise<boolean>;
  isLoading: boolean;
  error: string | null;
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

export const AuthProvider: React.FC<AuthProviderProps> = ({children}) => {
  // Estado inicial: cargar desde localStorage si existe
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('userData');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('authToken');
  });

  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);

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

  // Inicializar autenticación al cargar
  useEffect(() => {
    const initializeAuth = async () => {
      setError(null);

      const storedToken = localStorage.getItem('authToken');
      const storedUser = localStorage.getItem('userData');

      if (storedToken && storedUser) {
        try {
          // Validar token si el endpoint existe
          let isValid = true;

          // Intentar validar, pero si falla asumir válido (para desarrollo)
          try {
            isValid = await validateToken();
          } catch (validationError) {
            console.warn('Token validation endpoint might not be implemented');
            // Continuar con la autenticación usando el token almacenado
          }

          if (isValid) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);
            setToken(storedToken);
            setIsAuthenticated(true);
            console.log('Usuario autenticado desde localStorage');
          } else {
            // Token inválido, limpiar todo
            localStorage.removeItem('authToken');
            localStorage.removeItem('userData');
            setToken(null);
            setUser(null);
            setIsAuthenticated(false);
          }
        } catch (error) {
          console.error('Error initializing auth:', error);
          localStorage.removeItem('authToken');
          localStorage.removeItem('userData');
          setToken(null);
          setUser(null);
          setIsAuthenticated(false);
        }
      } else {
        // No hay datos de autenticación almacenados
        setIsAuthenticated(false);
      }

      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);

    try {
      const response: LoginResponse = await authService.login({email, password});

      if (!response.token) {
        throw new Error('No se recibió token de autenticación');
      }

      // Guardar token
      localStorage.setItem('authToken', response.token);
      setToken(response.token);

      // Guardar usuario
      setUser(response.user);
      localStorage.setItem('userData', JSON.stringify(response.user));

      setIsAuthenticated(true);

      console.log('Login exitoso');

    } catch (error: any) {
      console.error('Login error:', error);

      // Manejo de errores específicos
      let errorMessage = 'Error al iniciar sesión';

      if (error.response) {
        // Error de la API
        if (error.response.status === 401) {
          errorMessage = 'Credenciales incorrectas';
        } else if (error.response.status === 400) {
          errorMessage = 'Datos de entrada inválidos';
        } else if (error.response.data?.message) {
          errorMessage = error.response.data.message;
        }
      } else if (error.request) {
        errorMessage = 'No se pudo conectar con el servidor';
      }

      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    // Limpiar localStorage
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');

    // Resetear estado
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
    setError(null);

    console.log('Logout exitoso');
  };

  const register = async (userData: UserRequest): Promise<UserResponse> => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await authService.register(userData);

      try {
        await login(userData.email, userData.password);
        console.log('Registro y login automático exitoso');
      } catch (loginError) {
        console.warn('Registro exitoso pero login automático falló');
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
      }

      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider value={{
      user,
      token,
      isAuthenticated,
      login,
      logout,
      register,
      validateToken,
      isLoading,
      error
    }}>
      {children}
    </AuthContext.Provider>
  );
};