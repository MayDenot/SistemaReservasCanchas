import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import type {UserResponse} from "../../api/types/user.types.ts";
import type {ReservationResponse} from "../../api/types/reservation.types.ts";
import type {CourtResponse} from "../../api/types/court.types.ts";
import {useAuth} from "../../context/AuthContext.tsx";

const AdminDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalCourts: 0,
    totalReservations: 0,
    activeReservations: 0,
    revenue: 0
  });
  const [recentUsers, setRecentUsers] = useState<UserResponse[]>([]);
  const [recentReservations, setRecentReservations] = useState<ReservationResponse[]>([]);
  const [recentCourts, setRecentCourts] = useState<CourtResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState<'today' | 'week' | 'month' | 'year'>('today');

  useEffect(() => {
    loadDashboardData();
  }, [timeRange]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);

      const mockStats = {
        totalUsers: 1245,
        totalCourts: 89,
        totalReservations: 3421,
        activeReservations: 156,
        revenue: 45230.50
      };

      const mockUsers: UserResponse[] = [
        {
          id: BigInt(1),
          email: 'usuario1@email.com',
          userRole: 'USER',
          name: "María Perez",
          phone: '+54 11 1234-5678',
          createdAt: new Date().toISOString()
        },
        {
          id: BigInt(2),
          email: 'usuario2@email.com',
          userRole: 'USER',
          name: "Juan Gonzalez",
          phone: '+54 11 8765-4321',
          createdAt: new Date().toISOString()
        }
      ];

      const mockReservations: ReservationResponse[] = [
        {
          id: BigInt(1),
          userId: BigInt(1),
          courtId: BigInt(1),
          clubId: BigInt(1),
          startTime: new Date(),
          endTime: new Date(Date.now() + 2 * 60 * 60 * 1000),
          status: 'CONFIRMED',
          paymentStatus: 'CONFIRMED',
          createdAt: new Date()
        }
      ];

      const mockCourts: CourtResponse[] = [
        {
          id: BigInt(1),
          clubId: BigInt(1),
          name: 'Cancha Central de Tenis',
          type: "INDOOR",
          pricePerHour: BigInt(1500),
          isActive: true,
        }
      ];

      setStats(mockStats);
      setRecentUsers(mockUsers);
      setRecentReservations(mockReservations);
      setRecentCourts(mockCourts);

    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS'
    }).format(amount);
  };

  const formatDate = (date: Date) => {
    return new Date(date).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadge = (status: string) => {
    const config: Record<string, string> = {
      'PENDING': 'bg-orange-100 text-orange-800',
      'CONFIRMED': 'bg-green-100 text-green-800',
      'CANCELLED': 'bg-red-100 text-red-800'
    };
    return config[status] || 'bg-gray-100 text-gray-800';
  };

  const getPaymentBadge = (status: string) => {
    const config: Record<string, string> = {
      'PENDING': 'bg-orange-100 text-orange-800',
      'CONFIRMED': 'bg-green-100 text-green-800',
      'FAILED': 'bg-red-100 text-red-800',
      'CANCELLED': 'bg-gray-100 text-gray-800',
      'REFUNDED': 'bg-blue-100 text-blue-800'
    };
    return config[status] || 'bg-gray-100 text-gray-800';
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-green-600"></div>
          <p className="mt-4 text-gray-600 text-lg">Cargando panel de administración...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 py-8 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="bg-white/95 backdrop-blur-sm rounded-3xl shadow-2xl p-8 mb-8 border-l-4 border-green-500">
          <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-8">
            <div>
              <h1 className="text-4xl md:text-5xl font-black bg-gradient-to-r from-gray-900 via-green-600 to-emerald-600 bg-clip-text text-transparent mb-4">
                Panel de Administración
              </h1>
              <p className="text-gray-600 text-lg">
                Bienvenido, {user?.firstName} {user?.lastName}
              </p>
            </div>
            <div className="space-y-4">
              <div className="flex items-center gap-4">
                <label className="text-gray-700 font-bold">Período:</label>
                <select
                  value={timeRange}
                  onChange={(e) => setTimeRange(e.target.value as any)}
                  className="bg-white border-2 border-gray-200 rounded-xl px-4 py-2 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                >
                  <option value="today">Hoy</option>
                  <option value="week">Esta semana</option>
                  <option value="month">Este mes</option>
                  <option value="year">Este año</option>
                </select>
              </div>
              <div className="text-gray-600 font-bold flex items-center gap-3">
                <span>⏰</span>
                {new Date().toLocaleDateString('es-ES', {
                  weekday: 'long',
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                })}
              </div>
            </div>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {[
            {
              title: 'Ingresos',
              value: formatCurrency(stats.revenue),
              change: '+12.5% vs período anterior',
              icon: '💰',
              color: 'from-green-500 to-emerald-500'
            },
            {
              title: 'Usuarios',
              value: stats.totalUsers.toLocaleString(),
              change: '+8 nuevos hoy',
              icon: '👥',
              color: 'from-blue-500 to-cyan-500'
            },
            {
              title: 'Canchas',
              value: stats.totalCourts.toString(),
              change: `${stats.totalCourts - 5} activas`,
              icon: '🎾',
              color: 'from-orange-500 to-amber-500'
            },
            {
              title: 'Reservas',
              value: stats.totalReservations.toLocaleString(),
              change: `${stats.activeReservations} activas`,
              icon: '📅',
              color: 'from-teal-500 to-cyan-500'
            }
          ].map((stat, index) => (
            <div
              key={index}
              className="bg-white rounded-2xl shadow-xl p-8 border-2 border-transparent hover:border-green-100 transition-all duration-500 hover:-translate-y-3 relative overflow-hidden group"
            >
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r via-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>

              <div className="flex items-center gap-6 mb-8">
                <div className={`w-16 h-16 bg-gradient-to-br ${stat.color} rounded-2xl flex items-center justify-center text-2xl text-white shadow-lg group-hover:scale-110 group-hover:rotate-6 transition-all duration-500`}>
                  {stat.icon}
                </div>
                <div>
                  <h3 className="text-xl font-bold text-gray-900">{stat.title}</h3>
                </div>
              </div>

              <div className="text-3xl font-black text-gray-900 mb-4">{stat.value}</div>
              <div className="text-green-600 font-bold flex items-center gap-2">
                <span>📈</span>
                {stat.change}
              </div>
            </div>
          ))}
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Tablas */}
          <div className="lg:col-span-2 space-y-8">
            {/* Usuarios recientes */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <div className="flex justify-between items-center mb-8">
                <h3 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                  <span className="text-3xl">👥</span>
                  Usuarios recientes
                </h3>
                <Link to="/admin/users" className="text-green-600 hover:text-green-700 font-bold flex items-center gap-2 group">
                  Ver todos
                  <span className="group-hover:translate-x-2 transition-transform">→</span>
                </Link>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                  <tr className="bg-gradient-to-r from-gray-50 to-gray-100">
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Usuario</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Email</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Rol</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Fecha registro</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Acciones</th>
                  </tr>
                  </thead>
                  <tbody>
                  {recentUsers.map((user) => (
                    <tr key={user.id.toString()} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-4">
                          <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-full flex items-center justify-center text-white font-bold text-lg">
                            {user.name?.charAt(0).toUpperCase() || 'U'}
                          </div>
                          <div>
                            <div className="font-bold text-gray-900">{user.name}</div>
                          </div>
                        </div>
                      </td>
                      <td className="py-4 px-6">{user.email}</td>
                      <td className="py-4 px-6">
                          <span className={`px-3 py-1 rounded-full text-sm font-bold ${user.userRole === 'ADMIN' ? 'bg-gradient-to-r from-orange-100 to-amber-100 text-orange-700' : 'bg-gradient-to-r from-blue-100 to-cyan-100 text-blue-700'}`}>
                            {user.userRole === 'USER' ? 'Usuario' : 'Admin'}
                          </span>
                      </td>
                      <td className="py-4 px-6">{new Date(user.createdAt).toLocaleDateString()}</td>
                      <td className="py-4 px-6">
                        <div className="flex gap-2">
                          <button className="w-10 h-10 bg-gray-100 hover:bg-gray-200 rounded-xl flex items-center justify-center transition-colors" title="Ver detalles">
                            <span>👁️</span>
                          </button>
                          <button className="w-10 h-10 bg-blue-100 hover:bg-blue-200 text-blue-600 rounded-xl flex items-center justify-center transition-colors" title="Editar">
                            <span>✏️</span>
                          </button>
                          <button className="w-10 h-10 bg-red-100 hover:bg-red-200 text-red-600 rounded-xl flex items-center justify-center transition-colors" title="Eliminar">
                            <span>🗑️</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Reservas recientes */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <div className="flex justify-between items-center mb-8">
                <h3 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                  <span className="text-3xl">📅</span>
                  Reservas recientes
                </h3>
                <Link to="/admin/reservations" className="text-green-600 hover:text-green-700 font-bold flex items-center gap-2 group">
                  Ver todas
                  <span className="group-hover:translate-x-2 transition-transform">→</span>
                </Link>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                  <tr className="bg-gradient-to-r from-gray-50 to-gray-100">
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">ID</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Cancha</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Fecha/Hora</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Estado</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Pago</th>
                    <th className="py-4 px-6 text-left text-gray-700 font-bold">Acciones</th>
                  </tr>
                  </thead>
                  <tbody>
                  {recentReservations.map((reservation) => (
                    <tr key={reservation.id.toString()} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                      <td className="py-4 px-6 font-bold text-gray-900">
                        #{reservation.id.toString().slice(-6)}
                      </td>
                      <td className="py-4 px-6">Cancha #{reservation.courtId.toString()}</td>
                      <td className="py-4 px-6">{formatDate(reservation.startTime)}</td>
                      <td className="py-4 px-6">
                          <span className={`px-3 py-1 rounded-full text-sm font-bold ${getStatusBadge(reservation.status)}`}>
                            {reservation.status === 'PENDING' ? 'Pendiente' :
                              reservation.status === 'CONFIRMED' ? 'Confirmada' : 'Cancelada'}
                          </span>
                      </td>
                      <td className="py-4 px-6">
                          <span className={`px-3 py-1 rounded-full text-sm font-bold ${getPaymentBadge(reservation.paymentStatus)}`}>
                            {reservation.paymentStatus === 'PENDING' ? 'Pendiente' :
                              reservation.paymentStatus === 'CONFIRMED' ? 'Pagado' :
                                reservation.paymentStatus === 'FAILED' ? 'Fallido' :
                                  reservation.paymentStatus === 'CANCELLED' ? 'Cancelado' : 'Reembolsado'}
                          </span>
                      </td>
                      <td className="py-4 px-6">
                        <div className="flex gap-2">
                          <button className="w-10 h-10 bg-gray-100 hover:bg-gray-200 rounded-xl flex items-center justify-center transition-colors" title="Ver detalles">
                            <span>👁️</span>
                          </button>
                          <button className="w-10 h-10 bg-blue-100 hover:bg-blue-200 text-blue-600 rounded-xl flex items-center justify-center transition-colors" title="Editar">
                            <span>✏️</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* Sidebar derecha */}
          <div className="space-y-8">
            {/* Acciones rápidas */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
                <span className="text-3xl">⚡</span>
                Acciones rápidas
              </h3>

              <div className="grid grid-cols-1 gap-4">
                {[
                  { icon: '👤', title: 'Crear usuario', desc: 'Agregar nuevo usuario al sistema', to: '/admin/users/create' },
                  { icon: '➕', title: 'Agregar cancha', desc: 'Registrar nueva cancha', to: '/admin/courts/create' },
                  { icon: '🏢', title: 'Gestionar clubes', desc: 'Administrar clubes registrados', to: '/admin/clubs' },
                  { icon: '📊', title: 'Generar reporte', desc: 'Crear reporte de actividad', to: '/admin/reports' }
                ].map((action, index) => (
                  <Link
                    key={index}
                    to={action.to}
                    className="bg-white border-2 border-gray-200 hover:border-green-500 p-6 rounded-xl transition-all duration-300 hover:scale-[1.02] hover:shadow-lg group"
                  >
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 bg-gradient-to-br from-blue-100 to-cyan-100 rounded-xl flex items-center justify-center group-hover:scale-110 group-hover:rotate-6 transition-all duration-500">
                        <span className="text-2xl">{action.icon}</span>
                      </div>
                      <div>
                        <h4 className="font-bold text-gray-900">{action.title}</h4>
                        <p className="text-gray-600 text-sm">{action.desc}</p>
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            </div>

            {/* Canchas más populares */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
                <span className="text-3xl">📈</span>
                Canchas más populares
              </h3>

              <div className="space-y-6">
                {recentCourts.map((court) => (
                  <div key={court.id.toString()} className="flex justify-between items-center p-4 bg-gray-50 rounded-xl">
                    <div>
                      <h4 className="font-bold text-gray-900">{court.name}</h4>
                      <div className="flex gap-4 mt-2">
                        <span className="text-gray-600 flex items-center gap-2">
                          <span>🎾</span>
                          {court.type}
                        </span>
                        <span className="text-gray-600">
                          ${court.pricePerHour}/hora
                        </span>
                      </div>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-green-600">42</div>
                      <div className="text-sm text-gray-600">reservas</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Actividad reciente */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
                <span className="text-3xl">🔄</span>
                Actividad reciente
              </h3>

              <div className="space-y-6">
                {[
                  { icon: '✅', color: 'text-green-500', text: 'Nuevo usuario registrado: "María González"', time: 'Hace 5 minutos' },
                  { icon: '⚠️', color: 'text-yellow-500', text: 'Reserva #456789 cancelada por el usuario', time: 'Hace 30 minutos' },
                  { icon: 'ℹ️', color: 'text-blue-500', text: 'Nueva cancha agregada: "Cancha de Pádel 3"', time: 'Hace 2 horas' }
                ].map((activity, index) => (
                  <div key={index} className="flex gap-4">
                    <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-2xl ${activity.color} bg-opacity-10 ${activity.color.replace('text-', 'bg-')}`}>
                      {activity.icon}
                    </div>
                    <div>
                      <p className="text-gray-900">{activity.text}</p>
                      <p className="text-gray-600 text-sm mt-1">{activity.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;