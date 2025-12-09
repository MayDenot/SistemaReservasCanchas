import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import type { CourtResponse } from "../../api/types/court.types.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import { courtService } from "../../api/services/courtService.ts";

const CourtDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [court, setCourt] = useState<CourtResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);

  useEffect(() => {
    if (id) {
      loadCourtDetails();
    }
  }, [id]);

  useEffect(() => {
    if (selectedDate && court) {
      loadAvailableSlots();
    }
  }, [selectedDate, court]);

  const loadCourtDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const courtId = BigInt(id!);
      const data = await courtService.getCourtById(courtId);
      setCourt(data);

      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const dateString = tomorrow.toISOString().split('T')[0];
      setSelectedDate(dateString);

    } catch (err: any) {
      setError(err.message || 'Error al cargar los detalles de la cancha');
      console.error('Error loading court details:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadAvailableSlots = async () => {
    if (!court || !selectedDate) return;

    try {
      setLoadingSlots(true);
      const dateObj = new Date(selectedDate);
      const slots = await courtService.getCourtAvailability(court.id, dateObj);
      setAvailableSlots(slots);
    } catch (err) {
      console.error('Error loading available slots:', err);
      setAvailableSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleReservation = (timeSlot?: string) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/courts/${id}` } });
      return;
    }

    if (!court) return;

    const queryParams = new URLSearchParams({
      courtId: court.id.toString(),
      date: selectedDate
    });

    if (timeSlot) {
      queryParams.append('startTime', timeSlot);
    }

    navigate(`/reservations/new?${queryParams.toString()}`);
  };

  const getSportIcon = (sport: string) => {
    switch (sport) {
      case 'TENNIS': return '🎾';
      case 'PADDLE': return '🏓';
      case 'FOOTBALL': return '⚽';
      case 'BASKETBALL': return '🏀';
      default: return '🏃';
    }
  };

  const formatTimeSlot = (time: string) => {
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  };

  const getMinDate = (): string => {
    return new Date().toISOString().split('T')[0];
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-white"></div>
          <p className="mt-4 text-white text-lg">Cargando detalles de la cancha...</p>
        </div>
      </div>
    );
  }

  if (error || !court) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500 px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-20 h-20 bg-gradient-to-br from-red-500 to-pink-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="text-3xl">⚠️</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar la cancha</h3>
          <p className="text-gray-600 mb-8">{error || 'Cancha no encontrada'}</p>
          <div className="flex flex-col sm:flex-row gap-4">
            <button
              onClick={loadCourtDetails}
              className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors"
            >
              Reintentar
            </button>
            <Link
              to="/courts"
              className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-3 px-6 rounded-xl transition-colors text-center"
            >
              Volver a canchas
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500 py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-3 text-white mb-8">
          <Link to="/" className="hover:text-gray-200 transition-colors">Inicio</Link>
          <span className="text-white/50">/</span>
          <Link to="/courts" className="hover:text-gray-200 transition-colors">Canchas</Link>
          <span className="text-white/50">/</span>
          <span className="font-semibold">{court.name}</span>
        </nav>

        <div className="bg-white rounded-3xl shadow-2xl p-8 animate-slide-up">
          {/* Court Header */}
          <div className="mb-10">
            <div className="flex flex-wrap justify-between items-start gap-6 mb-8">
              <h1 className="text-3xl md:text-4xl lg:text-5xl font-black bg-gradient-to-r from-gray-900 via-green-600 to-emerald-600 bg-clip-text text-transparent">
                {court.name}
              </h1>
              <span className={`inline-flex items-center gap-2 px-6 py-3 rounded-full text-white font-bold text-sm ${court.isActive ? 'bg-gradient-to-r from-green-500 to-emerald-500' : 'bg-gradient-to-r from-red-500 to-pink-500'}`}>
                <span className="w-2 h-2 rounded-full bg-white animate-pulse"></span>
                {court.isActive ? 'Disponible' : 'No disponible'}
              </span>
            </div>

            {/* Details Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {[
                { icon: getSportIcon(court.type), title: 'Deporte', value: court.type },
                { icon: '💰', title: 'Precio por hora', value: `$${Number(court.pricePerHour).toFixed(2)}` },
                { icon: '🏢', title: 'Club', value: court.clubId?.toString() || 'Sin club asignado' }
              ].map((detail, index) => (
                <div
                  key={index}
                  className="bg-gradient-to-br from-gray-50 to-white rounded-2xl p-6 border-2 border-transparent hover:border-green-100 transition-all duration-500 hover:-translate-y-2 shadow-lg hover:shadow-xl"
                >
                  <div className="flex items-center gap-5">
                    <div className="w-16 h-16 bg-gradient-to-br from-green-500 to-emerald-500 rounded-2xl flex items-center justify-center text-2xl shadow-lg">
                      {detail.icon}
                    </div>
                    <div>
                      <h4 className="text-sm text-gray-500 font-semibold uppercase tracking-wider">{detail.title}</h4>
                      <p className="text-xl font-bold text-gray-900">{detail.value}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Reservation Section */}
          <div className="border-t border-gray-100 pt-10">
            <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
              <span className="text-3xl">📅</span>
              Reservar cancha
            </h3>

            <div className="bg-gray-50 rounded-2xl p-8">
              <div className="mb-8">
                <label className="block text-gray-700 font-bold mb-3">Seleccionar fecha</label>
                <input
                  type="date"
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  min={getMinDate()}
                  className="w-full max-w-xs bg-white border-2 border-gray-200 rounded-xl px-4 py-3 text-lg focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-200 transition-all"
                />
              </div>

              {loadingSlots ? (
                <div className="text-center py-8">
                  <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
                  <p className="mt-4 text-gray-600">Cargando horarios disponibles...</p>
                </div>
              ) : availableSlots.length > 0 ? (
                <div className="mb-8">
                  <label className="block text-gray-700 font-bold mb-4">Horarios disponibles</label>
                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
                    {availableSlots.map((slot, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => handleReservation(slot)}
                        className="bg-white border-2 border-gray-200 hover:border-green-500 hover:bg-green-500 hover:text-white text-gray-900 font-bold py-4 rounded-xl transition-all duration-300 hover:scale-105 active:scale-95 shadow-md hover:shadow-lg"
                      >
                        {formatTimeSlot(slot)}
                      </button>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="text-center py-8 bg-white rounded-xl border-2 border-gray-100">
                  <p className="text-gray-600">No hay horarios disponibles para esta fecha</p>
                </div>
              )}

              <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
                <button
                  type="button"
                  onClick={() => handleReservation()}
                  disabled={!court.isActive || availableSlots.length === 0}
                  className={`flex items-center gap-3 font-bold text-lg px-10 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 active:scale-95 ${!court.isActive || availableSlots.length === 0 ? 'bg-gray-300 cursor-not-allowed' : 'bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white shadow-lg hover:shadow-xl'}`}
                >
                  <span className="text-xl">🔐</span>
                  Reservar ahora
                </button>

                {!isAuthenticated && (
                  <p className="text-gray-600 flex items-center gap-2">
                    <span className="text-xl">ℹ️</span>
                    Debes iniciar sesión para reservar
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CourtDetailPage;