// hooks/useRoleGuard.ts
import { useNavigate } from 'react-router-dom';
import { useUserRoles } from './useUserRoles';

export const useRoleGuard = (allowedRoles: ('USER' | 'CLUB_ADMIN' | 'SUPER_ADMIN')[]) => {
  const { isAuthenticated, hasAnyRole, isLoading } = useUserRoles();
  const navigate = useNavigate();

  const checkAccess = () => {
    if (isLoading) return 'loading';
    if (!isAuthenticated) return 'unauthenticated';
    if (!hasAnyRole(allowedRoles)) return 'unauthorized';
    return 'granted';
  };

  const redirectIfNoAccess = (redirectTo: string = '/login') => {
    const access = checkAccess();

    if (access === 'unauthenticated') {
      navigate(redirectTo);
    } else if (access === 'unauthorized') {
      navigate('/unauthorized');
    }

    return access;
  };

  return {
    checkAccess,
    redirectIfNoAccess,
    hasAccess: checkAccess() === 'granted',
    isLoading,
  };
};