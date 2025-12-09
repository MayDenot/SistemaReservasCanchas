import React from 'react';
import './Header.css';
import {useAuth} from "../../../context/AuthContext.tsx";

const Header: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="header">
      <div className="logo">🏸 Reservas</div>
      <nav className="nav">
        {user ? (
          <>
            <a href="/reservations">Mis Reservas</a>
            <a href="/profile">Perfil</a>
            {user.role === 'ADMIN' && <a href="/admin">Admin</a>}
            <button onClick={logout}>Cerrar Sesión</button>
          </>
        ) : (
          <>
            <a href="/login">Iniciar Sesión</a>
            <a href="/register">Registrarse</a>
          </>
        )}
      </nav>
    </header>
  );
};
export default Header;