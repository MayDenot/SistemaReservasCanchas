// hooks/useUserRoles.ts
import { useAuth } from '../context/AuthContext';

export const useUserRoles = () => {
  const { user, isAuthenticated, isLoading } = useAuth(); // Agrega isLoading

  // Propiedades computadas
  const isClubAdmin = user?.userRole === 'CLUB_ADMIN';
  const isSuperAdmin = user?.userRole === 'SUPER_ADMIN';
  const isRegularUser = user?.userRole === 'USER';

  // Funciones para verificar roles
  const hasRole = (role: 'USER' | 'CLUB_ADMIN' | 'SUPER_ADMIN') => {
    return user?.userRole === role;
  };

  const hasAnyRole = (roles: ('USER' | 'CLUB_ADMIN' | 'SUPER_ADMIN')[]) => {
    return roles.some(role => user?.userRole === role);
  };

  const hasAllRoles = (roles: ('USER' | 'CLUB_ADMIN' | 'SUPER_ADMIN')[]) => {
    return roles.every(role => user?.userRole === role);
  };

  return {
    // Usuario y autenticación
    user,
    isAuthenticated,
    isLoading, // Exporta isLoading

    // Propiedades de rol (para uso rápido)
    userRole: user?.userRole,
    isClubAdmin,
    isSuperAdmin,
    isRegularUser,

    // Funciones de verificación
    hasRole,
    hasAnyRole,
    hasAllRoles,

    // Alias comunes
    canManageClub: isClubAdmin || isSuperAdmin,
    canManageSystem: isSuperAdmin,
    canMakeReservations: isAuthenticated,
  };
};