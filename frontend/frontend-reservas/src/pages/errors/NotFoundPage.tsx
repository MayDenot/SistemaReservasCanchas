import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 via-white to-gray-100">
      <div className="text-center max-w-2xl mx-auto px-4">
        <div className="mb-8">
          <div className="text-9xl font-black text-gray-300 mb-4">404</div>
          <h1 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
            Página no encontrada
          </h1>
          <p className="text-lg text-gray-600 mb-8">
            Lo sentimos, la página que estás buscando no existe o ha sido movida.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          <div className="bg-white p-6 rounded-2xl shadow-lg border-l-4 border-green-500">
            <div className="text-4xl mb-4">🏠</div>
            <h3 className="font-bold text-gray-900 mb-2">Ir al inicio</h3>
            <p className="text-gray-600 text-sm">Vuelve a la página principal</p>
          </div>

          <div className="bg-white p-6 rounded-2xl shadow-lg border-l-4 border-blue-500">
            <div className="text-4xl mb-4">🎾</div>
            <h3 className="font-bold text-gray-900 mb-2">Buscar canchas</h3>
            <p className="text-gray-600 text-sm">Encuentra canchas disponibles</p>
          </div>

          <div className="bg-white p-6 rounded-2xl shadow-lg border-l-4 border-orange-500">
            <div className="text-4xl mb-4">📞</div>
            <h3 className="font-bold text-gray-900 mb-2">Contactar soporte</h3>
            <p className="text-gray-600 text-sm">Necesitas ayuda adicional</p>
          </div>
        </div>

        <div className="space-y-4">
          <Link
            to="/"
            className="inline-flex items-center justify-center w-full md:w-auto bg-gradient-to-r from-green-600 to-emerald-600 text-white font-bold py-4 px-8 rounded-xl hover:from-green-700 hover:to-emerald-700 transition-all duration-300 transform hover:scale-[1.02] shadow-lg"
          >
            <span className="material-icons mr-2">home</span>
            Volver al inicio
          </Link>

          <div className="flex flex-wrap justify-center gap-4 mt-6">
            <Link
              to="/courts"
              className="inline-flex items-center text-green-600 hover:text-green-700 font-medium"
            >
              <span className="material-icons mr-2">search</span>
              Buscar canchas
            </Link>
            <Link
              to="/login"
              className="inline-flex items-center text-blue-600 hover:text-blue-700 font-medium"
            >
              <span className="material-icons mr-2">login</span>
              Iniciar sesión
            </Link>
            <Link
              to="/register"
              className="inline-flex items-center text-purple-600 hover:text-purple-700 font-medium"
            >
              <span className="material-icons mr-2">person_add</span>
              Registrarse
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;