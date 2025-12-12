import React from 'react';
import {useAuth} from "../../../context/AuthContext.tsx";

const Header: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="fixed top-0 left-0 w-full z-50 px-4">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between
                  bg-gradient-to-r from-green-700/90 via-emerald-600/90 to-teal-600/90
                  backdrop-blur-xl rounded-2xl mt-4 shadow-2xl
                  border border-white/20 hover:border-white/30 transition-all duration-300">

        {/* Logo */}
        <div className="text-2xl font-black text-white flex items-center gap-3">
          <span className="text-3xl animate-bounce">🏸</span>
          <span className="tracking-tight drop-shadow-lg">Reservas</span>
        </div>

        {/* Navigation */}
        <nav className="flex items-center gap-6 text-white font-semibold">
          {user ? (
            <>
              <a
                href="/reservations"
                className="text-white hover:text-emerald-200 transition-all duration-200 hover:scale-105"
              >
                Mis Reservas
              </a>

              <a
                href="/profile"
                className="text-white hover:text-emerald-200 transition-all duration-200 hover:scale-105"
              >
                Perfil
              </a>

              {user.role === 'ADMIN' && (
                <a
                  href="/admin"
                  className="text-white hover:text-emerald-200 transition-all duration-200 hover:scale-105"
                >
                  Admin
                </a>
              )}

              <button
                onClick={logout}
                className="bg-white/20 hover:bg-white/30 text-white border border-white/40
                     px-5 py-2.5 rounded-xl transition-all duration-200
                     backdrop-blur-sm shadow-lg hover:shadow-xl hover:scale-105
                     hover:border-white/60 font-medium"
              >
                Cerrar Sesión
              </button>
            </>
          ) : (
            <>
              <a
                href="/login"
                className="text-white hover:text-emerald-200 transition-all duration-200 hover:scale-105"
              >
                Iniciar Sesión
              </a>

              <a
                href="/register"
                className="bg-white text-green-700 hover:bg-emerald-50 hover:text-green-800
                     font-bold px-5 py-2.5 rounded-xl transition-all duration-200
                     shadow-lg hover:shadow-xl hover:scale-105 border border-white/30"
              >
                Registrarse
              </a>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};
export default Header;