import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type { ReservationResponse } from "../../api/types/reservation.types.ts";
import { reservationService } from "../../api/services/reservationService.ts";

const ReservationsPage: React.FC = () => {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState<ReservationResponse[]>([]);
  const [filteredReservations, setFilteredReservations] = useState<ReservationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'upcoming' | 'past' | 'cancelled'>('upcoming');
  const [selectedReservation, setSelectedReservation] = useState<ReservationResponse | null>(null);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    loadReservations();
  }, []);

  useEffect(() => {
    filterReservations();
  }, [reservations, activeTab]);

  const loadReservations = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await reservationService.getUserReservations();
      setReservations(data);
    } catch (err: any) {
      setError(err.message || 'Error al cargar las reservas');
      console.error('Error loading reservations:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterReservations = () => {
    const now = new Date();
    let filtered = [...reservations];

    switch (activeTab) {
      case 'upcoming':
        filtered = filtered.filter(res =>
          new Date(res.startTime) > now &&
          res.status === 'CONFIRMED'
        );
        break;
      case 'past':
        filtered = filtered.filter(res =>
          new Date(res.startTime) <= now &&
          res.status === 'CONFIRMED'
        );
        break;
      case 'cancelled':
        filtered = filtered.filter(res => res.status === 'CANCELLED');
        break;
    }

    filtered.sort((a, b) =>
      new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
    );

    setFilteredReservations(filtered);
  };

  const handleCancelReservation = async () => {
    if (!selectedReservation) return;

    try {
      setCancelling(true);
      await reservationService.cancelReservation(selectedReservation.id);
      await loadReservations();
      setShowCancelModal(false);
      setSelectedReservation(null);
    } catch (err: any) {
      alert(err.message || 'Error al cancelar la reserva');
      console.error('Error cancelling reservation:', err);
    } finally {
      setCancelling(false);
    }
  };

  const openCancelModal = (reservation: ReservationResponse) => {
    setSelectedReservation(reservation);
    setShowCancelModal(true);
  };

  const getStatusBadge = (status: string) => {
    const statusConfig: Record<string, { class: string; icon: string; text: string }> = {
      'PENDING': { class: 'bg-orange-100 text-orange-800', icon: 'schedule', text: 'Pendiente' },
      'CONFIRMED': { class: 'bg-green-100 text-green-800', icon: 'check_circle', text: 'Confirmada' },
      'CANCELLED': { class: 'bg-red-100 text-red-800', icon: 'cancel', text: 'Cancelada' }
    };

    const config = statusConfig[status] || { class: 'bg-gray-100 text-gray-800', icon: 'help', text: status };

    return (
      <span className={`px-3 py-1 rounded-full text-sm font-bold flex items-center gap-2 ${config.class}`}>
        <span className="material-icons text-sm">{config.icon}</span> {config.text}
      </span>
    );
  };

  const getPaymentBadge = (status: string) => {
    const statusConfig: Record<string, { class: string; icon: string; text: string }> = {
      'PENDING': { class: 'bg-orange-100 text-orange-800', icon: 'schedule', text: 'Pendiente' },
      'CONFIRMED': { class: 'bg-green-100 text-green-800', icon: 'payments', text: 'Pagado' },
      'FAILED': { class: 'bg-red-100 text-red-800', icon: 'error', text: 'Fallido' },
      'CANCELLED': { class: 'bg-gray-100 text-gray-800', icon: 'block', text: 'Cancelado' },
      'REFUNDED': { class: 'bg-blue-100 text-blue-800', icon: 'replay', text: 'Reembolsado' }
    };

    const config = statusConfig[status] || { class: 'bg-gray-100 text-gray-800', icon: 'help', text: status };

    return (
      <span className={`px-3 py-1 rounded-full text-sm font-bold flex items-center gap-2 ${config.class}`}>
        <span className="material-icons text-sm">{config.icon}</span> {config.text}
      </span>
    );
  };

  const formatDateTime = (date: Date) => {
    const dateObj = new Date(date);
    return {
      date: dateObj.toLocaleDateString('es-ES', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }),
      time: dateObj.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit'
      }),
      fullDateTime: dateObj.toLocaleString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    };
  };

  const calculateDuration = (startTime: Date, endTime: Date): number => {
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end.getTime() - start.getTime();
    return Math.round(diffMs / (1000 * 60 * 60));
  };

  const calculateTotalPrice = (reservation: ReservationResponse): number => {
    const pricePerHour = 100;
    const duration = calculateDuration(reservation.startTime, reservation.endTime);
    return duration * pricePerHour;
  };

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-20 h-20 bg-gradient-to-br from-red-500 to-pink-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="material-icons text-3xl text-white">error</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar las reservas</h3>
          <p className="text-gray-600 mb-8">{error}</p>
          <button
            onClick={loadReservations}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors flex items-center gap-2 justify-center mx-auto"
          >
            <span className="material-icons">refresh</span>
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-green-600"></div>
          <p className="mt-4 text-gray-600 text-lg">Cargando tus reservas...</p>
        </div>
      </div>
    );
  }

  const tabCounts = {
    upcoming: reservations.filter(r => new Date(r.startTime) > new Date() && r.status === 'CONFIRMED').length,
    past: reservations.filter(r => new Date(r.startTime) <= new Date() && r.status === 'CONFIRMED').length,
    cancelled: reservations.filter(r => r.status === 'CANCELLED').length
  };

  // Configuración de tabs con colores y estilos
  const tabsConfig = {
    upcoming: {
      label: 'Próximas',
      icon: 'event_available',
      color: 'text-white',
      bgColor: 'bg-blue-500',
      borderColor: 'bg-blue-500',
      countColor: 'bg-white text-blue-500',
      iconColor: 'text-white'
    },
    past: {
      label: 'Historial',
      icon: 'history',
      color: 'text-white',
      bgColor: 'bg-blue-500',
      borderColor: 'bg-blue-500',
      countColor: 'bg-white text-blue-500',
      iconColor: 'text-white'
    },
    cancelled: {
      label: 'Canceladas',
      icon: 'cancel',
      color: 'text-white',
      bgColor: 'bg-blue-500',
      borderColor: 'bg-blue-500',
      countColor: 'bg-white text-blue-500',
      iconColor: 'text-white'
    }
  };

  return (
    <div className="min-h-screen bg-gradient-sport py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 mb-10 text-white">
          <div>
            <h1 className="text-white text-4xl md:text-5xl font-black mb-3 drop-shadow-lg">Mis Reservas</h1>
            <p className="text-white/90 text-lg flex items-center gap-2">
              <span className="material-icons">calendar_today</span>
              Gestiona todas tus reservas de canchas
            </p>
          </div>
          <button
            onClick={() => navigate('/reservations/new')}
            className="bg-white text-green-600 hover:bg-gray-100 font-bold px-8 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 hover:shadow-xl shadow-lg flex items-center gap-3 group"
          >
            <span className="material-icons group-hover:rotate-90 transition-transform duration-300">add</span>
            Nueva reserva
          </button>
        </div>

        {/* TABS */}
        <div className="mb-8">
          <div className="bg-gradient-to-r from-blue-50 to-blue-100/80 backdrop-blur-sm rounded-2xl p-2 flex gap-2 border border-blue-200">
            {(['upcoming', 'past', 'cancelled'] as const).map((tab) => {
              const config = tabsConfig[tab];
              const isActive = activeTab === tab;

              return (
                <button
                  key={tab}
                  className={`flex-1 py-4 px-4 rounded-xl transition-all duration-300 flex items-center justify-center gap-3 group ${isActive
                    ? `${config.bgColor } shadow-lg transform scale-[1.02] text-white hover:bg-blue-400`
                    : 'text-blue-800 bg-white hover:bg-blue-50 border border-blue-100'}`}
                  onClick={() => setActiveTab(tab)}
                >
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center transition-all duration-300 ${isActive
                    ? 'bg-white/20'
                    : 'bg-blue-50'}`}>
            <span className={`material-icons ${isActive ? 'text-white' : 'text-blue-600'}`}>
              {config.icon}
            </span>
                  </div>
                  <div className="flex flex-col items-start">
            <span className={`font-bold ${isActive ? 'text-white' : 'text-blue-800'}`}>
              {config.label}
            </span>
                    <span className={`text-sm font-bold px-2 py-0.5 rounded-full ${isActive
                      ? 'bg-white/30 text-white'
                      : 'bg-blue-100 text-blue-700'}`}>
              {tabCounts[tab]}
            </span>
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Lista de reservas */}
        <div className="bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl p-8">
          {filteredReservations.length === 0 ? (
            <div className="text-center py-16">
              <div className="w-24 h-24 bg-gradient-to-br from-gray-200 to-gray-300 rounded-full flex items-center justify-center mx-auto mb-6">
                <span className="material-icons text-5xl text-gray-600">
                  {activeTab === 'upcoming' ? 'event_available' : activeTab === 'past' ? 'history' : 'cancel'}
                </span>
              </div>
              <h3 className="text-2xl font-bold text-gray-900 mb-4">
                {activeTab === 'upcoming' ? 'No hay reservas próximas' :
                  activeTab === 'past' ? 'Aún no has completado ninguna reserva' :
                    'No has cancelado ninguna reserva'}
              </h3>
              <p className="text-gray-600 mb-8 max-w-md mx-auto">
                {activeTab === 'upcoming' ? 'Programa tu primera reserva para verla aquí' :
                  'Tus reservas aparecerán en esta sección'}
              </p>
              {activeTab === 'upcoming' && (
                <button
                  onClick={() => navigate('/reservations/new')}
                  className="bg-gradient-sport hover:from-green-700 hover:to-emerald-700 text-white font-bold px-8 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 hover:shadow-xl shadow-lg flex items-center gap-2 justify-center mx-auto group"
                >
                  <span className="material-icons group-hover:rotate-90 transition-transform duration-300">add</span>
                  Crear primera reserva
                </button>
              )}
            </div>
          ) : (
            <div className="space-y-6">
              {filteredReservations.map((reservation) => {
                const formattedStart = formatDateTime(reservation.startTime);
                const formattedEnd = formatDateTime(reservation.endTime);
                const duration = calculateDuration(reservation.startTime, reservation.endTime);
                const totalPrice = calculateTotalPrice(reservation);

                return (
                  <div
                    key={reservation.id.toString()}
                    className="bg-white border-2 border-gray-100 hover:border-green-200 rounded-2xl p-6 hover:shadow-xl transition-all duration-500 hover:-translate-y-1"
                  >
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-6">
                      <div>
                        <div className="flex items-center gap-3 mb-3">
                          <div className="w-12 h-12 bg-gradient-sport rounded-xl flex items-center justify-center">
                            <span className="material-icons text-white">sports</span>
                          </div>
                          <div>
                            <h3 className="text-xl font-bold text-gray-900">
                              Reserva #{reservation.id.toString().slice(-6)}
                            </h3>
                            <p className="text-sm text-gray-600">{formattedStart.date}</p>
                          </div>
                        </div>
                        <div className="flex flex-wrap gap-4 text-gray-600 ml-14">
                          <span className="flex items-center gap-2 bg-gray-50 px-3 py-1.5 rounded-lg">
                            <span className="material-icons text-green-600 text-sm">schedule</span>
                            {formattedStart.time} - {formattedEnd.time}
                          </span>
                          <span className="flex items-center gap-2 bg-gray-50 px-3 py-1.5 rounded-lg">
                            <span className="material-icons text-green-600 text-sm">hourglass_empty</span>
                            {duration} hora{duration !== 1 ? 's' : ''}
                          </span>
                        </div>
                      </div>
                      <div className="flex flex-wrap gap-3">
                        {getStatusBadge(reservation.status)}
                        {getPaymentBadge(reservation.paymentStatus)}
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                      {[
                        { icon: 'sports', label: 'Cancha ID', value: reservation.courtId.toString(), color: 'text-blue-500' },
                        { icon: 'business', label: 'Club ID', value: reservation.clubId.toString(), color: 'text-purple-500' },
                        { icon: 'payments', label: 'Estimado', value: `$${totalPrice.toFixed(2)}`, color: 'text-green-500' },
                        { icon: 'create', label: 'Creada', value: formatDateTime(reservation.createdAt).fullDateTime, color: 'text-gray-500' }
                      ].map((item, index) => (
                        <div key={index} className="flex items-center gap-3 bg-gray-50 p-4 rounded-xl hover:bg-gray-100 transition-colors">
                          <span className={`material-icons ${item.color}`}>{item.icon}</span>
                          <div className="flex-1">
                            <div className="text-xs text-gray-500 font-medium">{item.label}</div>
                            <div className="font-bold text-gray-900">{item.value}</div>
                          </div>
                        </div>
                      ))}
                    </div>

                    <div className="flex gap-4">
                      <Link
                        to={`/reservations/${reservation.id}`}
                        className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-800 font-bold py-3 px-6 rounded-xl transition-colors text-center flex items-center justify-center gap-2 group"
                      >
                        <span className="material-icons text-sm group-hover:scale-110 transition-transform">visibility</span>
                        Ver detalles
                      </Link>

                      {reservation.status === 'CONFIRMED' &&
                        new Date(reservation.startTime) > new Date() && (
                          <button
                            onClick={() => openCancelModal(reservation)}
                            className="flex-1 bg-gradient-to-r from-red-500 to-pink-500 hover:from-red-600 hover:to-pink-600 text-white font-bold py-3 px-6 rounded-xl transition-all duration-300 hover:scale-105 hover:shadow-lg flex items-center justify-center gap-2 group"
                          >
                            <span className="material-icons group-hover:animate-pulse">cancel</span>
                            Cancelar reserva
                          </button>
                        )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Modal de cancelación */}
      {showCancelModal && selectedReservation && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
          <div className="bg-white rounded-3xl max-w-md w-full p-8 animate-fade-in shadow-2xl">
            <div className="flex justify-between items-center mb-8">
              <h3 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center">
                  <span className="material-icons text-red-500">warning</span>
                </div>
                Cancelar Reserva
              </h3>
              <button
                onClick={() => setShowCancelModal(false)}
                className="text-gray-400 hover:text-gray-600 text-2xl material-icons"
              >
                close
              </button>
            </div>

            <div className="mb-8">
              <p className="text-gray-700 mb-4">
                ¿Estás seguro de que deseas cancelar la reserva{' '}
                <strong className="text-green-600">#{selectedReservation.id.toString().slice(-6)}</strong>
                {' '}programada para el día{' '}
                <strong>{formatDateTime(selectedReservation.startTime).date}</strong>
                {' '}a las <strong>{formatDateTime(selectedReservation.startTime).time}</strong>?
              </p>
              <div className="bg-red-50 border border-red-200 p-4 rounded-xl mb-6">
                <div className="flex items-center gap-3">
                  <span className="material-icons text-red-500">error</span>
                  <p className="text-red-700 text-sm font-medium">
                    Esta acción no se puede deshacer. Podrían aplicarse cargos por cancelación según las políticas del club.
                  </p>
                </div>
              </div>

              <div className="space-y-4">
                <div className="flex justify-between items-center py-3 border-b border-gray-100">
                  <span className="text-gray-600 flex items-center gap-2">
                    <span className="material-icons text-sm text-green-600">hourglass_empty</span>
                    Duración:
                  </span>
                  <span className="font-bold text-lg">{calculateDuration(selectedReservation.startTime, selectedReservation.endTime)} horas</span>
                </div>
                <div className="flex justify-between items-center py-3 border-b border-gray-100">
                  <span className="text-gray-600 flex items-center gap-2">
                    <span className="material-icons text-sm text-blue-600">sports</span>
                    Cancha ID:
                  </span>
                  <span className="font-bold text-lg">{selectedReservation.courtId.toString()}</span>
                </div>
                <div className="flex justify-between items-center py-3">
                  <span className="text-gray-600 flex items-center gap-2">
                    <span className="material-icons text-sm text-purple-600">business</span>
                    Club ID:
                  </span>
                  <span className="font-bold text-lg">{selectedReservation.clubId.toString()}</span>
                </div>
              </div>
            </div>

            <div className="flex gap-4">
              <button
                onClick={() => setShowCancelModal(false)}
                className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-800 font-bold py-3 px-6 rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center gap-2 group"
                disabled={cancelling}
              >
                <span className="material-icons text-sm group-hover:-translate-x-1 transition-transform">arrow_back</span>
                Mantener reserva
              </button>
              <button
                onClick={handleCancelReservation}
                className="flex-1 bg-gradient-to-r from-red-500 to-pink-500 hover:from-red-600 hover:to-pink-600 text-white font-bold py-3 px-6 rounded-xl transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 group"
                disabled={cancelling}
              >
                {cancelling ? (
                  <div className="flex items-center justify-center gap-3">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                    Cancelando...
                  </div>
                ) : (
                  <>
                    <span className="material-icons group-hover:scale-110 transition-transform">check</span>
                    Sí, cancelar reserva
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReservationsPage;