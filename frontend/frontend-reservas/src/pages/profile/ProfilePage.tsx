import React, { useState, useEffect } from 'react';
import type { ProfileRequest, ProfileResponse } from "../../api/types/user.types.ts";
import { useAuth } from "../../context/AuthContext.tsx";

const ProfilePage: React.FC = () => {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState<ProfileRequest>({
    name: '',
    phone: '',
    currentPassword: '',
    newPassword: ''
  });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (user) {
      const profileResponse: ProfileResponse = {
        id: BigInt(user.id),
        email: user.email,
        name: user.name,
        phone: user.phone || '',
        createdAt: new Date(user.createdAt),
        isActive: true
      };

      setProfile(profileResponse);
      setFormData({
        name: profileResponse.name,
        phone: profileResponse.phone,
        currentPassword: '',
        newPassword: ''
      });
      setLoading(false);
    }
  }, [user]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (formData.newPassword) {
      if (formData.newPassword !== confirmPassword) {
        setError('Las nuevas contraseñas no coinciden');
        return;
      }

      if (formData.newPassword.length < 6) {
        setError('La nueva contraseña debe tener al menos 6 caracteres');
        return;
      }

      if (!formData.currentPassword) {
        setError('Debes ingresar tu contraseña actual para cambiarla');
        return;
      }
    }

    const profileData: ProfileRequest = {
      name: formData.name,
      phone: formData.phone,
      currentPassword: formData.currentPassword,
      newPassword: formData.newPassword || ""
    };

    try {
      if (profile) {
        const updatedProfile: ProfileResponse = {
          ...profile,
          name: profileData.name,
          phone: profileData.phone
        };

        setProfile(updatedProfile);
        setEditing(false);
        setSuccess('Perfil actualizado correctamente');

        setFormData(prev => ({
          ...prev,
          currentPassword: '',
          newPassword: ''
        }));
        setConfirmPassword('');
      }
    } catch (err: any) {
      setError(err.message || 'Error al actualizar el perfil');
    }
  };

  const getUserRole = () => {
    return user?.role || 'USER';
  };

  const getRoleDisplayName = (role: string) => {
    const roles: Record<string, string> = {
      'USER': 'Usuario',
      'ADMIN': 'Administrador',
      'CLUB_OWNER': 'Dueño de Club'
    };
    return roles[role] || role;
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-sport">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-white"></div>
          <p className="mt-4 text-white text-lg">Cargando perfil...</p>
        </div>
      </div>
    );
  }

  if (!profile || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-sport px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-20 h-20 bg-gradient-to-br from-red-500 to-pink-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="text-3xl">⚠️</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar el perfil</h3>
          <p className="text-gray-600">No se pudo cargar la información del perfil</p>
        </div>
      </div>
    );
  }

  const userRole = getUserRole();

  return (
    <div className="min-h-screen bg-gradient-sport py-8 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="mb-10 text-white">
          <h1 className="text-white text-4xl md:text-5xl font-black mb-4">Mi Perfil</h1>
          <p className="text-white/90 text-xl">Gestiona tu información personal y configuración</p>
        </div>

        <div className="bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl p-8 animate-fade-in">
          {/* Profile Header */}
          <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-8 mb-12 pb-12 border-b border-gray-100">
            <div className="flex items-start gap-8">
              <div className="relative group">
                <div className="w-32 h-32 bg-gradient-sport rounded-full flex items-center justify-center text-4xl text-white font-bold shadow-2xl group-hover:scale-110 group-hover:rotate-6 transition-all duration-500">
                  {profile.name ? profile.name.charAt(0).toUpperCase() : 'U'}
                </div>
              </div>
              <div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">{profile.name}</h2>
                <p className="text-gray-600 text-lg mb-6">{profile.email}</p>
                <div className="flex flex-wrap gap-3">
                  <span className="bg-gradient-to-r from-blue-100 to-cyan-100 text-blue-700 px-4 py-2 rounded-full font-bold flex items-center gap-2">
                    <span className="material-icons text-lg">👤</span>
                    {getRoleDisplayName(userRole)}
                  </span>
                  <span className={`px-4 py-2 rounded-full font-bold flex items-center gap-2 ${profile.isActive ? 'bg-gradient-to-r from-green-100 to-emerald-100 text-green-700' : 'bg-gradient-to-r from-red-100 to-pink-100 text-red-700'}`}>
                    <span className={`w-2 h-2 rounded-full ${profile.isActive ? 'bg-green-500' : 'bg-red-500'}`}></span>
                    {profile.isActive ? 'Activo' : 'Inactivo'}
                  </span>
                </div>
              </div>
            </div>
            <button
              onClick={() => setEditing(!editing)}
              className={`font-bold px-8 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 ${editing ? 'bg-gradient-to-r from-red-500 to-pink-500 hover:from-red-600 hover:to-pink-600 text-white' : 'bg-gradient-sport hover:from-green-700 hover:to-emerald-700 text-white'}`}
            >
              {editing ? (
                <div className="flex items-center gap-3">
                  <span className="material-icons">close</span>
                  Cancelar
                </div>
              ) : (
                <div className="flex items-center gap-3">
                  <span className="material-icons">edit</span>
                  Editar Perfil
                </div>
              )}
            </button>
          </div>

          {!editing ? (
            <div className="mb-12">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                {[
                  { icon: 'mail_outline', label: 'Email', value: profile.email },
                  { icon: 'phone', label: 'Teléfono', value: profile.phone || 'No especificado' },
                  { icon: 'badge', label: 'Rol', value: getRoleDisplayName(userRole) },
                  { icon: 'calendar_today', label: 'Miembro desde', value: formatDate(profile.createdAt) },
                  { icon: 'check_circle', label: 'Estado', value: profile.isActive ? 'Activo' : 'Inactivo' }
                ].map((item, index) => (
                  <div
                    key={index}
                    className="card border-2 border-transparent hover:border-sport hover:-translate-y-2"
                  >
                    <div className="flex items-center gap-4 mb-4">
                      <div className="w-12 h-12 bg-gradient-to-br from-blue-100 to-cyan-100 rounded-xl flex items-center justify-center">
                        <span className="material-icons text-2xl text-blue-600">{item.icon}</span>
                      </div>
                      <div>
                        <div className="text-sm text-gray-500 font-semibold uppercase tracking-wider">{item.label}</div>
                        <div className="text-lg font-bold text-white">{item.value}</div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="mb-12">
              {error && (
                <div className="error border-l-4 border-red-500 p-6 rounded-2xl mb-8">
                  <div className="flex items-center gap-4">
                    <span className="material-icons text-red-500 text-3xl">error</span>
                    <p className="text-red-700 font-bold">{error}</p>
                  </div>
                </div>
              )}
              {success && (
                <div className="success border-l-4 border-green-500 p-6 rounded-2xl mb-8">
                  <div className="flex items-center gap-4">
                    <span className="material-icons text-green-500 text-3xl">check_circle</span>
                    <p className="text-green-700 font-bold">{success}</p>
                  </div>
                </div>
              )}

              {/* Información personal */}
              <div className="mb-12">
                <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
                  <span className="material-icons text-3xl text-blue-600">person</span>
                  Información personal
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  <div>
                    <label className="block text-gray-700 font-bold mb-4">Nombre completo</label>
                    <input
                      type="text"
                      value={formData.name}
                      onChange={handleInputChange}
                      name="name"
                      required
                      className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all"
                      placeholder="Ej: Juan Pérez"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700 font-bold mb-4">Teléfono</label>
                    <input
                      type="tel"
                      value={formData.phone}
                      onChange={handleInputChange}
                      name="phone"
                      className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all"
                      placeholder="+54 11 1234-5678"
                    />
                  </div>
                </div>
              </div>

              {/* Cambiar contraseña */}
              <div className="mb-12">
                <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
                  <span className="material-icons text-3xl text-blue-600">lock</span>
                  Cambiar contraseña
                </h3>
                <p className="text-gray-600 mb-8">
                  Deja estos campos en blanco si no deseas cambiar la contraseña
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
                  <div>
                    <label className="block text-gray-700 font-bold mb-4">
                      Contraseña actual <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="password"
                      value={formData.currentPassword}
                      onChange={handleInputChange}
                      name="currentPassword"
                      className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all"
                      placeholder="••••••••"
                    />
                    <div className="text-sm text-gray-500 mt-2">
                      * Requerida solo si vas a cambiar la contraseña
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  <div>
                    <label className="block text-gray-700 font-bold mb-4">Nueva contraseña</label>
                    <input
                      type="password"
                      value={formData.newPassword}
                      onChange={handleInputChange}
                      name="newPassword"
                      className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all"
                      placeholder="Mínimo 6 caracteres"
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700 font-bold mb-4">Confirmar nueva contraseña</label>
                    <input
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all"
                      placeholder="Repite la nueva contraseña"
                    />
                  </div>
                </div>
              </div>

              {/* Botones */}
              <div className="flex gap-6">
                <button
                  type="button"
                  onClick={() => {
                    setEditing(false);
                    setFormData({
                      name: profile.name,
                      phone: profile.phone,
                      currentPassword: '',
                      newPassword: ''
                    });
                    setConfirmPassword('');
                    setError(null);
                    setSuccess(null);
                  }}
                  className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-4 px-8 rounded-2xl transition-all duration-300 transform hover:scale-105"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="flex-1 bg-gradient-sport hover:from-green-700 hover:to-emerald-700 text-white font-bold py-4 px-8 rounded-2xl transition-all duration-300 transform hover:scale-105"
                >
                  <div className="flex items-center justify-center gap-3">
                    <span className="material-icons">save</span>
                    Guardar cambios
                  </div>
                </button>
              </div>
            </form>
          )}

          {/* Acciones rápidas */}
          <div>
            <h3 className="text-2xl font-bold text-gray-900 mb-8">Acciones rápidas</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {[
                { icon: 'event', title: 'Mis Reservas', desc: 'Ver y gestionar todas tus reservas', onClick: () => window.location.href = '/reservations' },
                ...(userRole === 'CLUB_OWNER' ? [{ icon: 'business', title: 'Mis Clubes', desc: 'Gestionar clubes y canchas', onClick: () => window.location.href = '/clubs' }] : []),
                ...(userRole === 'ADMIN' ? [{ icon: 'dashboard', title: 'Panel de Administración', desc: 'Acceder al panel de control', onClick: () => window.location.href = '/admin' }] : []),
                { icon: 'logout', title: 'Cerrar Sesión', desc: 'Salir de tu cuenta', onClick: logout }
              ].map((action, index) => (
                <button
                  key={index}
                  onClick={action.onClick}
                  className="bg-white border-3 border-gray-200 hover:border-sport p-6 rounded-2xl text-left transition-all duration-500 hover:-translate-y-3 hover:shadow-xl group"
                >
                  <div className="flex items-center gap-4 mb-4">
                    <div className="w-12 h-12 bg-gradient-to-br from-green-100 to-emerald-100 rounded-xl flex items-center justify-center group-hover:scale-110 group-hover:rotate-6 transition-all duration-500">
                      <span className="material-icons text-2xl text-green-600">{action.icon}</span>
                    </div>
                    <div>
                      <h4 className="text-lg font-bold text-gray-900">{action.title}</h4>
                      <p className="text-gray-600 text-sm">{action.desc}</p>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;