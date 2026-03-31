import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';

interface UserMenuProps {
  user: any;
  navLinks: { label: string; to: string }[];
}

const UserMenu: React.FC<UserMenuProps> = ({ user, navLinks }) => {
  const { logout } = useAuth();
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // Cerrar al hacer click fuera
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div ref={menuRef} className="relative">
      {/* Trigger */}
      <button
        onClick={() => setOpen(prev => !prev)}
        className="flex items-center gap-2 bg-white/20 px-4 py-2 rounded-xl
        hover:bg-white/30 transition shadow-md focus:outline-none"
      >
        <span className="text-white font-medium truncate max-w-[120px]">
          {user.name ?? 'Usuario'}
        </span>
        <span
          className={`material-icons text-white text-sm transition-transform ${
            open ? 'rotate-180' : ''
          }`}
        >
          expand_more
        </span>
      </button>

      {/* Dropdown */}
      {open && (
        <div
          className="absolute right-0 mt-3 w-56 bg-white rounded-xl shadow-2xl z-50"
        >
          <div className="px-4 py-3 text-sm text-gray-600">
            Sesión iniciada como
            <div className="font-semibold text-gray-900 truncate">
              {user.email}
            </div>
          </div>

          <div className="border-t">
            {navLinks.map(link => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setOpen(false)}
                className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
              >
                {link.label}
              </Link>
            ))}

            <Link
              to="/profile"
              onClick={() => setOpen(false)}
              className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
            >
              Perfil
            </Link>
          </div>

          <div className="border-t">
            <button
              onClick={() => {
                setOpen(false);
                logout();
              }}
              className="w-full text-left px-4 py-2 text-gray-700 hover:text-red-600 bg-gray-100 hover:bg-gray-200"
            >
              Cerrar sesión
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserMenu;