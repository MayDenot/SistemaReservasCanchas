import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.tsx';

const BecomeClubAdminPage: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-extrabold text-gray-900 mb-4">
            Conviértete en Administrador de Club
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Gestiona tus canchas, recibe reservas y administra tu negocio deportivo
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Opción 1: Ya tengo cuenta */}
          <div className="bg-white rounded-2xl shadow-xl p-8 border border-gray-200">
            <div className="flex items-center justify-center h-16 w-16 bg-gradient-to-br from-blue-600 to-cyan-600 rounded-2xl mb-6 mx-auto">
              <span className="material-icons text-3xl text-white">person</span>
            </div>
            <h3 className="text-2xl font-bold text-center text-gray-900 mb-4">
              Ya tengo una cuenta
            </h3>
            <p className="text-gray-600 mb-6 text-center">
              Si ya tienes una cuenta en nuestra plataforma, puedes crear un club directamente.
            </p>

            {isAuthenticated ? (
              <Link
                to="/create-club"
                className="w-full flex justify-center py-3 px-4 bg-gradient-to-br from-blue-600 to-cyan-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-cyan-700 transition-all duration-300 transform hover:scale-[1.02]"
              >
                <div className="flex items-center">
                  <span className="material-icons mr-2">add_business</span>
                  Crear Mi Club
                </div>
              </Link>
            ) : (
              <>
                <Link
                  to="/login"
                  className="w-full flex justify-center py-3 px-4 bg-gradient-to-br from-blue-600 to-cyan-600 text-white font-bold rounded-xl hover:from-blue-700 hover:to-cyan-700 transition-all duration-300 transform hover:scale-[1.02] mb-4"
                >
                  <div className="flex items-center">
                    <span className="material-icons mr-2">login</span>
                    Iniciar Sesión
                  </div>
                </Link>
                <p className="text-sm text-gray-500 text-center">
                  Luego podrás crear tu club
                </p>
              </>
            )}
          </div>

          {/* Opción 2: Soy nuevo */}
          <div className="bg-white rounded-2xl shadow-xl p-8 border border-gray-200">
            <div className="flex items-center justify-center h-16 w-16 bg-gradient-to-br from-green-600 to-emerald-600 rounded-2xl mb-6 mx-auto">
              <span className="material-icons text-3xl text-white">person_add</span>
            </div>
            <h3 className="text-2xl font-bold text-center text-gray-900 mb-4">
              Soy nuevo aquí
            </h3>
            <p className="text-gray-600 mb-6 text-center">
              Crea una cuenta nueva como administrador de club en un solo proceso.
            </p>
            <Link
              to="/register-club-admin"
              className="w-full flex justify-center py-3 px-4 bg-gradient-to-br from-green-600 to-emerald-600 text-white font-bold rounded-xl hover:from-green-700 hover:to-emerald-700 transition-all duration-300 transform hover:scale-[1.02]"
            >
              <div className="flex items-center">
                <span className="material-icons mr-2">person_add_alt</span>
                Registrarme como Administrador
              </div>
            </Link>
          </div>
        </div>

        <div className="mt-12 text-center">
          <Link
            to="/"
            className="inline-flex items-center text-blue-600 hover:text-blue-500 font-medium"
          >
            <span className="material-icons mr-2">arrow_back</span>
            Volver al inicio
          </Link>
        </div>
      </div>
    </div>
  );
};

export default BecomeClubAdminPage;