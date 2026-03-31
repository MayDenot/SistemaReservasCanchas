import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useUserRoles } from '../../hooks/useUserRoles';

interface RoleBasedRouteProps {
  children: React.ReactNode;
  allowedRoles: ('USER' | 'CLUB_ADMIN' | 'SUPER_ADMIN')[];
  redirectTo?: string;
}

const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({
                                                         children,
                                                         allowedRoles,
                                                         redirectTo = '/login'
                                                       }) => {
  const { isAuthenticated, isLoading, hasAnyRole } = useUserRoles();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
          <p className="mt-4 text-gray-600">Verificando permisos...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  if (!hasAnyRole(allowedRoles)) {
    return <Navigate to="/unauthorized" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

export default RoleBasedRoute;