import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useUserRoles } from '../../../hooks/useUserRoles';
import UserMenu from "./UserMenu.tsx";

const Header: React.FC = () => {
  const { isAuthenticated, isClubAdmin, isRegularUser } = useUserRoles();
  const { user } = useAuth();

  const navLinks = [
    { show: true, label: 'Canchas', to: '/courts' },
    { show: isRegularUser, label: 'Mis Reservas', to: '/my-reservations' },
    { show: isClubAdmin, label: 'Panel del Club', to: '/club/dashboard' }
  ].filter(link => link.show);

  return (
    <header className="fixed top-0 left-0 w-full z-50 px-4">
      <div
        className="max-w-7xl mx-auto mt-4 px-6 py-4
        flex items-center justify-between
        bg-gradient-to-r from-green-700/90 via-emerald-600/90 to-teal-600/90
        backdrop-blur-xl rounded-2xl shadow-2xl border border-white/20"
      >
        {/* LOGO */}
        <Link to="/" className="flex items-center gap-3 text-white">
          <span className="text-3xl">🏸</span>
          <span className="text-2xl font-black tracking-tight">Reservas</span>
        </Link>

        {/* NAV (solo si hay más de un link) */}
        {navLinks.length > 1 && (
          <nav className="hidden md:flex gap-6 font-medium">
            {navLinks.map(link => (
              <Link
                key={link.to}
                to={link.to}
                className="text-white hover:text-emerald-200 transition"
              >
                {link.label}
              </Link>
            ))}
          </nav>
        )}

        {/* USER AREA */}
        <div className="flex items-center gap-4">
          {isAuthenticated && user ? (
            <UserMenu
              user={user}
              navLinks={navLinks}
            />
          ) : (
            <>
              <Link
                to="/login"
                className="text-white hover:text-emerald-200 transition"
              >
                Iniciar sesión
              </Link>

              <Link
                to="/register"
                className="bg-white text-green-700 font-bold
                px-4 py-2 rounded-xl hover:bg-emerald-50 transition shadow"
              >
                Registrarse
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};
export default Header;