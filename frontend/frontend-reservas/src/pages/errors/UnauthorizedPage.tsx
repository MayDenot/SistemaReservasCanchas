import React from 'react';
import { Link } from 'react-router-dom';
import { useUserRoles } from '../../hooks/useUserRoles';

const UnauthorizedPage: React.FC = () => {
  const { isAuthenticated, userRole } = useUserRoles();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="bg-white rounded-3xl shadow-2xl p-12 max-w-md w-full text-center">
        <div className="text-6xl mb-6">🔒</div>
        <h1 className="text-3xl font-black text-gray-900 mb-4">Acceso Denegado</h1>

        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6 text-left">
          <p className="text-red-700">
            {isAuthenticated
              ? `Tu rol actual (${userRole}) no tiene permiso para acceder a esta página.`
              : 'Debes iniciar sesión para acceder a esta página.'}
          </p>
        </div>

        <div className="space-y-4">
          {isAuthenticated ? (
            <>
              <Link
                to="/"
                className="block w-full bg-green-600 text-white py-3 rounded-xl font-bold hover:bg-green-700 transition-colors"
              >
                Ir al Inicio
              </Link>
              {userRole === 'CLUB_ADMIN' && (
                <Link
                  to="/club/dashboard"
                  className="block w-full bg-blue-600 text-white py-3 rounded-xl font-bold hover:bg-blue-700 transition-colors"
                >
                  Ir al Panel del Club
                </Link>
              )}
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="block w-full bg-green-600 text-white py-3 rounded-xl font-bold hover:bg-green-700 transition-colors"
              >
                Iniciar Sesión
              </Link>
              <Link
                to="/"
                className="block w-full bg-gray-200 text-gray-800 py-3 rounded-xl font-bold hover:bg-gray-300 transition-colors"
              >
                Volver al Inicio
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;