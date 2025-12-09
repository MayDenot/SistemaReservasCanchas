import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from "../context/AuthContext.tsx";

interface AdminRouteProps {
  children: React.ReactNode;
}

const AdminRoute: React.FC<AdminRouteProps> = ({ children }) => {
  const { isAuthenticated, user, isLoading } = useAuth(); // Usa isLoading
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Verificar si el usuario tiene rol de administrador
  // Ajusta esta condición según cómo almacenes el rol en tu usuario
  const isAdmin = user?.role === 'ADMIN';

  if (!isAdmin) {
    // Redirigir a home si no es admin
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export default AdminRoute;