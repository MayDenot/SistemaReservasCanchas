import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useUserRoles } from '../../hooks/useUserRoles';
import { clubService } from "../../api/services/clubService.ts";
import type { CourtResponse } from '../../api/types/court.types.ts';
import type { ReservationResponse } from '../../api/types/reservation.types.ts';

interface ClubStats {
  totalCourts: number;
  activeCourts: number;
  maintenanceCourts: number;
  totalReservations: number;
  activeReservations: number;
  pendingReservations: number;
  revenue: number;
  monthlyRevenue: number;
  weeklyRevenue: number;
  clubName: string;
}

const ClubDashboardPage: React.FC = () => {
  const { user } = useUserRoles();
  const navigate = useNavigate();

  const [clubData, setClubData] = useState<any>(null);
  const [clubStats, setClubStats] = useState<ClubStats>({
    totalCourts: 0,
    activeCourts: 0,
    maintenanceCourts: 0,
    totalReservations: 0,
    activeReservations: 0,
    pendingReservations: 0,
    revenue: 0,
    monthlyRevenue: 0,
    weeklyRevenue: 0,
    clubName: 'Cargando...'
  });

  const [recentCourts, setRecentCourts] = useState<CourtResponse[]>([]);
  const [recentReservations, setRecentReservations] = useState<ReservationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTimeRange, setSelectedTimeRange] = useState<'today' | 'week' | 'month'>('today');

  useEffect(() => {
    const loadClubData = async () => {
      setLoading(true);
      setError(null);

      try {
        // 1. Obtener el club del usuario actual
        const myClub = await clubService.getMyClub();

        if (!myClub) {
          // El usuario no es administrador de ningún club
          setLoading(false);
          setError('No tienes un club asignado como administrador');
          return;
        }

        setClubData(myClub);

        // Datos mock temporales hasta que implementes los endpoints
        setClubStats({
          totalCourts: 8,
          activeCourts: 6,
          maintenanceCourts: 1,
          totalReservations: 156,
          activeReservations: 12,
          pendingReservations: 3,
          revenue: 45230,
          monthlyRevenue: 125600,
          weeklyRevenue: 32450,
          clubName: myClub.name || 'Mi Club'
        });

        // Datos mock de canchas
        const mockCourts: CourtResponse[] = [
          {
            id: BigInt(1),
            clubId: myClub.id,
            clubName: myClub.name || 'Club Deportivo',
            name: 'Cancha de Tenis 1',
            type: "INDOOR",
            pricePerHour: BigInt(1500),
            isActive: true
          },
          {
            id: BigInt(2),
            clubId: myClub.id,
            clubName: myClub.name || 'Club Deportivo',
            name: 'Cancha de Pádel 1',
            type: "OUTDOOR",
            pricePerHour: BigInt(1200),
            isActive: true
          },
          {
            id: BigInt(3),
            clubId: myClub.id,
            clubName: myClub.name || 'Club Deportivo',
            name: 'Cancha de Fútbol 5',
            type: "OUTDOOR",
            pricePerHour: BigInt(2000),
            isActive: true
          },
          {
            id: BigInt(4),
            clubId: myClub.id,
            clubName: myClub.name || 'Club Deportivo',
            name: 'Cancha de Tenis 2',
            type: "INDOOR",
            pricePerHour: BigInt(1500),
            isActive: false
          },
        ];
        setRecentCourts(mockCourts.slice(0, 4));

        // Datos mock de reservas
        const mockReservations: ReservationResponse[] = [
          {
            id: BigInt(1),
            userId: BigInt(10),
            courtId: BigInt(2),
            clubId: myClub.id,
            startTime: new Date('2024-01-15T14:00:00'),
            endTime: new Date('2024-01-15T16:00:00'),
            status: 'CONFIRMED',
            paymentStatus: 'CONFIRMED',
            createdAt: new Date('2024-01-14T10:00:00'),
            amount: 3200 // Nota: Este campo no existe en tu interfaz, lo agregamos temporalmente
          } as any,
          {
            id: BigInt(2),
            userId: BigInt(11),
            courtId: BigInt(1),
            clubId: myClub.id,
            startTime: new Date('2024-01-15T10:00:00'),
            endTime: new Date('2024-01-15T11:30:00'),
            status: 'PENDING',
            paymentStatus: 'PENDING',
            createdAt: new Date('2024-01-14T09:30:00'),
            amount: 2400
          } as any,
          {
            id: BigInt(3),
            userId: BigInt(12),
            courtId: BigInt(3),
            clubId: myClub.id,
            startTime: new Date('2024-01-15T18:00:00'),
            endTime: new Date('2024-01-15T20:00:00'),
            status: 'CONFIRMED',
            paymentStatus: 'CONFIRMED',
            createdAt: new Date('2024-01-14T15:00:00'),
            amount: 5600
          } as any,
        ];
        setRecentReservations(mockReservations);

      } catch (error: any) {
        console.error('Error general al cargar datos del club:', error);
        setError('Error al cargar los datos del club. Por favor, intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    };

    loadClubData();
  }, [selectedTimeRange, navigate]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  const formatDateTime = (date: Date) => {
    return new Date(date).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadge = (status: string) => {
    const config: Record<string, { class: string; text: string }> = {
      'ACTIVE': { class: 'bg-green-100 text-green-800', text: 'Activa' },
      'INACTIVE': { class: 'bg-gray-100 text-gray-800', text: 'Inactiva' },
      'CONFIRMED': { class: 'bg-green-100 text-green-800', text: 'Confirmada' },
      'PENDING': { class: 'bg-orange-100 text-orange-800', text: 'Pendiente' },
      'CANCELLED': { class: 'bg-red-100 text-red-800', text: 'Cancelada' }
    };

    return config[status] || { class: 'bg-gray-100 text-gray-800', text: 'Desconocido' };
  };

  const getPaymentBadge = (status: string) => {
    const config: Record<string, { class: string; text: string }> = {
      'CONFIRMED': { class: 'bg-green-100 text-green-800', text: 'Pagado' },
      'PENDING': { class: 'bg-orange-100 text-orange-800', text: 'Pendiente' },
      'REFUNDED': { class: 'bg-blue-100 text-blue-800', text: 'Reembolsado' },
      'FAILED': { class: 'bg-red-100 text-red-800', text: 'Fallido' }
    };

    return config[status] || { class: 'bg-gray-100 text-gray-800', text: 'Desconocido' };
  };

  const getCourtTypeText = (type: "OUTDOOR" | "INDOOR") => {
    return type === "OUTDOOR" ? "Exterior" : "Interior";
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-cyan-50">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600 text-lg">Cargando panel del club...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-cyan-50">
        <div className="max-w-md w-full bg-white rounded-3xl shadow-2xl p-8 text-center">
          <div className="text-6xl mb-6">🏢</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Club no encontrado</h2>
          <p className="text-gray-600 mb-6">{error}</p>
          <div className="space-y-4">
            <Link
              to="/"
              className="block w-full bg-blue-600 text-white py-3 rounded-xl font-bold hover:bg-blue-700 transition-colors"
            >
              Volver al inicio
            </Link>
            <Link
              to="/register-club"
              className="block w-full bg-green-600 text-white py-3 rounded-xl font-bold hover:bg-green-700 transition-colors"
            >
              Registrar un club
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 py-8 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="bg-white/95 backdrop-blur-sm rounded-3xl shadow-2xl p-8 mb-8 border-l-4 border-blue-500">
          <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-8">
            <div>
              <div className="flex items-center gap-4 mb-2">
                <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-cyan-600 rounded-2xl flex items-center justify-center">
                  <span className="material-icons text-3xl text-white">business</span>
                </div>
                <div>
                  <h1 className="text-4xl md:text-5xl font-black text-gray-900 mb-2">
                    {clubStats.clubName}
                  </h1>
                  <div className="flex flex-wrap items-center gap-3">
                    <p className="text-gray-600">
                      Bienvenido, <span className="font-bold text-blue-600">{user?.name}</span>
                    </p>
                    <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-bold">
                      Administrador
                    </span>
                  </div>
                </div>
              </div>

              {clubData && (
                <div className="mt-4 flex flex-wrap gap-4 text-sm text-gray-600">
                  <div className="flex items-center gap-2">
                    <span className="material-icons text-blue-500">location_on</span>
                    <span>{clubData.address || 'Dirección no especificada'}</span>
                  </div>
                  {clubData.phone && (
                    <div className="flex items-center gap-2">
                      <span className="material-icons text-blue-500">phone</span>
                      <span>{clubData.phone}</span>
                    </div>
                  )}
                  {clubData.openingTime && clubData.closingTime && (
                    <div className="flex items-center gap-2">
                      <span className="material-icons text-blue-500">schedule</span>
                      <span>
                        {clubData.openingTime?.slice(0, 5)} - {clubData.closingTime?.slice(0, 5)}
                      </span>
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="flex flex-wrap gap-4">
              <div className="flex items-center gap-4">
                <label className="text-gray-700 font-bold">Período:</label>
                <select
                  value={selectedTimeRange}
                  onChange={(e) => setSelectedTimeRange(e.target.value as any)}
                  className="bg-white border-2 border-gray-200 rounded-xl px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="today">Hoy</option>
                  <option value="week">Esta semana</option>
                  <option value="month">Este mes</option>
                </select>
              </div>

              <div className="flex gap-2">
                <Link
                  to="/club/courts"
                  className="bg-white border-2 border-blue-500 text-blue-600 hover:bg-blue-50 font-bold px-6 py-3 rounded-xl transition-colors flex items-center gap-2"
                >
                  <span className="material-icons">sports</span>
                  Canchas
                </Link>
                <Link
                  to="/club/settings"
                  className="bg-gray-100 hover:bg-gray-200 text-gray-800 font-bold px-6 py-3 rounded-xl transition-colors flex items-center gap-2"
                >
                  <span className="material-icons">settings</span>
                  Configuración
                </Link>
              </div>
            </div>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {[
            {
              title: 'Ingresos',
              value: formatCurrency(clubStats.revenue),
              change: `+${formatCurrency(clubStats.monthlyRevenue)} este mes`,
              icon: '💰',
              color: 'bg-gradient-to-br from-green-500 to-emerald-500',
              to: '/club/finances'
            },
            {
              title: 'Canchas',
              value: clubStats.totalCourts.toString(),
              change: `${clubStats.activeCourts} activas, ${clubStats.maintenanceCourts} inactivas`,
              icon: '🎾',
              color: 'bg-gradient-to-br from-blue-500 to-cyan-500',
              to: '/club/courts'
            },
            {
              title: 'Reservas',
              value: clubStats.totalReservations.toString(),
              change: `${clubStats.activeReservations} confirmadas, ${clubStats.pendingReservations} pendientes`,
              icon: '📅',
              color: 'bg-gradient-to-br from-orange-500 to-amber-500',
              to: '/club/reservations'
            },
            {
              title: 'Ingresos semanales',
              value: formatCurrency(clubStats.weeklyRevenue),
              change: selectedTimeRange === 'today' ? 'Hoy' : selectedTimeRange === 'week' ? 'Esta semana' : 'Este mes',
              icon: '📊',
              color: 'bg-gradient-to-br from-purple-500 to-pink-500',
              to: '/club/analytics'
            }
          ].map((stat, index) => (
            <Link
              key={index}
              to={stat.to}
              className="card border-2 border-transparent hover:border-blue-300 hover:-translate-y-3 relative overflow-hidden group transition-all duration-300"
            >
              <div className="flex items-center justify-between mb-6">
                <div className={`w-14 h-14 ${stat.color} rounded-2xl flex items-center justify-center text-2xl text-white shadow-lg group-hover:scale-110 group-hover:rotate-6 transition-all duration-500`}>
                  {stat.icon}
                </div>
              </div>

              <div className="text-3xl font-black text-gray-900 mb-2">{stat.value}</div>
              <div className="text-lg font-bold text-gray-700 mb-3">{stat.title}</div>
              <div className="text-blue-600 font-bold flex items-center gap-2 text-sm">
                <span>📈</span>
                {stat.change}
              </div>
            </Link>
          ))}
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Sección izquierda - Canchas */}
          <div className="lg:col-span-2 space-y-8">
            {/* Canchas del club */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <div className="flex justify-between items-center mb-8">
                <h3 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                  <span className="text-3xl">🎾</span>
                  Mis Canchas
                </h3>
                <Link to="/club/courts" className="text-blue-600 hover:text-blue-700 font-bold flex items-center gap-2 group">
                  Gestionar canchas
                  <span className="group-hover:translate-x-2 transition-transform">→</span>
                </Link>
              </div>

              {recentCourts.length === 0 ? (
                <div className="text-center py-8">
                  <div className="text-5xl mb-4">🏢</div>
                  <h4 className="text-lg font-bold text-gray-900 mb-2">No hay canchas registradas</h4>
                  <p className="text-gray-600 mb-6">Comienza agregando tu primera cancha</p>
                  <Link
                    to="/club/courts/new"
                    className="inline-flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-xl font-bold hover:bg-blue-700 transition-colors"
                  >
                    <span className="material-icons">add</span>
                    Agregar cancha
                  </Link>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {recentCourts.map((court) => {
                    const status = court.isActive ?
                      getStatusBadge('ACTIVE') :
                      getStatusBadge('INACTIVE');

                    return (
                      <div
                        key={court.id.toString()}
                        className="bg-gray-50 rounded-xl p-6 border-2 border-transparent hover:border-blue-300 transition-all duration-300"
                      >
                        <div className="flex justify-between items-start mb-4">
                          <div>
                            <h4 className="font-bold text-gray-900 text-lg">{court.name}</h4>
                            <div className="flex items-center gap-3 mt-2">
                              <span className="text-gray-600">{getCourtTypeText(court.type)}</span>
                              <span className={`px-3 py-1 rounded-full text-xs font-bold ${status.class}`}>
                                {status.text}
                              </span>
                            </div>
                          </div>
                          <div className="text-right">
                            <div className="text-xl font-bold text-gray-900">
                              ${Number(court.pricePerHour).toLocaleString()}
                            </div>
                            <div className="text-sm text-gray-600">/hora</div>
                          </div>
                        </div>

                        <div className="mt-6">
                          <div className="text-sm text-gray-600 mb-2">Club: {court.clubName}</div>
                          <div className="flex gap-2">
                            <Link
                              to={`/club/courts/${court.id}`}
                              className="flex-1 px-4 py-2 bg-blue-100 text-blue-700 hover:bg-blue-200 rounded-lg font-medium transition-colors text-sm text-center"
                            >
                              Editar
                            </Link>
                            <Link
                              to={`/club/courts/${court.id}/reservations`}
                              className="flex-1 px-4 py-2 bg-green-100 text-green-700 hover:bg-green-200 rounded-lg font-medium transition-colors text-sm text-center"
                            >
                              Reservas
                            </Link>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Reservas recientes */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <div className="flex justify-between items-center mb-8">
                <h3 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                  <span className="text-3xl">📅</span>
                  Reservas Recientes
                </h3>
                <Link to="/club/reservations" className="text-blue-600 hover:text-blue-700 font-bold flex items-center gap-2 group">
                  Ver todas las reservas
                  <span className="group-hover:translate-x-2 transition-transform">→</span>
                </Link>
              </div>

              {recentReservations.length === 0 ? (
                <div className="text-center py-8">
                  <div className="text-5xl mb-4">📅</div>
                  <h4 className="text-lg font-bold text-gray-900 mb-2">No hay reservas recientes</h4>
                  <p className="text-gray-600">Todavía no hay reservas en tu club</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                    <tr className="bg-gradient-to-r from-gray-50 to-gray-100">
                      <th className="py-4 px-6 text-left text-gray-700 font-bold">ID</th>
                      <th className="py-4 px-6 text-left text-gray-700 font-bold">Fecha/Hora</th>
                      <th className="py-4 px-6 text-left text-gray-700 font-bold">Estado</th>
                      <th className="py-4 px-6 text-left text-gray-700 font-bold">Pago</th>
                    </tr>
                    </thead>
                    <tbody>
                    {recentReservations.map((reservation) => {
                      const status = getStatusBadge(reservation.status);
                      const paymentStatus = getPaymentBadge(reservation.paymentStatus);

                      return (
                        <tr key={reservation.id.toString()} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                          <td className="py-4 px-6 font-bold text-gray-900">
                            #{reservation.id.toString().slice(-6)}
                          </td>
                          <td className="py-4 px-6">
                            {formatDateTime(reservation.startTime)}
                          </td>
                          <td className="py-4 px-6">
                              <span className={`px-3 py-1 rounded-full text-xs font-bold ${status.class}`}>
                                {status.text}
                              </span>
                          </td>
                          <td className="py-4 px-6">
                              <span className={`px-3 py-1 rounded-full text-xs font-bold ${paymentStatus.class}`}>
                                {paymentStatus.text}
                              </span>
                          </td>
                        </tr>
                      );
                    })}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>

          {/* Sidebar derecha - Acciones rápidas */}
          <div className="space-y-8">
            {/* Acciones rápidas */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
                <span className="text-3xl">⚡</span>
                Acciones Rápidas
              </h3>

              <div className="grid grid-cols-1 gap-4">
                {[
                  { icon: '➕', title: 'Agregar cancha', desc: 'Registrar nueva cancha en el club', to: '/club/courts/new' },
                  { icon: '👥', title: 'Gestionar staff', desc: 'Administrar empleados del club', to: '/club/staff' },
                  { icon: '💰', title: 'Ver ingresos', desc: 'Reportes financieros detallados', to: '/club/finances' },
                  { icon: '📊', title: 'Generar reporte', desc: 'Crear reporte de actividad', to: '/club/reports' }
                ].map((action, index) => (
                  <Link
                    key={index}
                    to={action.to}
                    className="bg-white border-2 border-gray-200 hover:border-blue-300 p-6 rounded-xl transition-all duration-300 hover:scale-[1.02] hover:shadow-lg group"
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

            {/* Horario del club */}
            <div className="bg-white rounded-2xl shadow-xl p-8">
              <h3 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-3">
                <span className="text-3xl">⏰</span>
                Horario del Club
              </h3>

              {clubData && (
                <div className="space-y-6">
                  <div className="bg-blue-50 rounded-xl p-6">
                    <div className="flex justify-between items-center mb-4">
                      <div className="font-bold text-gray-900">Horario actual</div>
                      <Link to="/club/settings" className="text-blue-600 hover:text-blue-700 text-sm font-medium">
                        Editar
                      </Link>
                    </div>
                    {clubData.openingTime && clubData.closingTime ? (
                      <div className="text-center">
                        <div className="text-3xl font-black text-blue-600 mb-2">
                          {clubData.openingTime.slice(0, 5)} - {clubData.closingTime.slice(0, 5)}
                        </div>
                        <div className="text-gray-600">Apertura - Cierre</div>
                      </div>
                    ) : (
                      <div className="text-center py-4">
                        <p className="text-gray-600">Horario no configurado</p>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ClubDashboardPage;