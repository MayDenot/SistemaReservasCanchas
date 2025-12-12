import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import type { CourtResponse } from "../../api/types/court.types.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import { courtService } from "../../api/services/courtService.ts";
import { clubService } from "../../api/services/clubService.ts";

const CourtDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [court, setCourt] = useState<CourtResponse | null>(null);
  const [clubName, setClubName] = useState<string>('Cargando...');
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

      if (!id) {
        throw new Error('ID de cancha no proporcionado');
      }

      console.log('🔍 Cargando cancha con ID:', id);
      console.log('🔧 Tipo de ID:', typeof id);

      // Usar BigInt como la API espera
      const courtId = BigInt(id);
      console.log('📞 Llamando a courtService.getCourtById con ID (bigint):', courtId);
      console.log('🔧 courtId.toString():', courtId.toString());

      try {
        const data = await courtService.getCourtById(courtId);
        console.log('✅ Cancha cargada:', data);
        setCourt(data);

        // Cargar el nombre del club
        if (data.clubId) {
          console.log('🔍 clubId de la cancha:', data.clubId);
          await loadClubName(data.clubId);
        } else {
          setClubName('Sin club asignado');
        }

        // Establecer fecha por defecto (mañana)
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const dateString = tomorrow.toISOString().split('T')[0];
        setSelectedDate(dateString);

      } catch (serviceError: any) {
        console.error('❌ Error en courtService:', serviceError);
        console.error('❌ Detalles del error:', {
          message: serviceError.message,
          response: serviceError.response,
          config: serviceError.config
        });
        throw serviceError;
      }

    } catch (err: any) {
      console.error('❌ Error completo en loadCourtDetails:', err);

      let errorMessage = 'Error al cargar los detalles de la cancha';

      if (err.response) {
        // Error de respuesta HTTP
        console.error('📊 Detalles de respuesta:', {
          status: err.response.status,
          statusText: err.response.statusText,
          data: err.response.data,
          headers: err.response.headers
        });

        if (err.response.status === 404) {
          errorMessage = 'Cancha no encontrada';
        } else if (err.response.status === 403) {
          errorMessage = 'No tienes permisos para ver esta cancha';
        } else if (err.response.status === 500) {
          errorMessage = 'Error interno del servidor';
        }
      } else if (err.request) {
        // Error de red
        console.error('🌐 Error de red:', err.request);
        errorMessage = 'Error de conexión con el servidor';
      }

      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const loadClubName = async (clubId: bigint) => {
    try {
      console.log('🔍 Buscando club con ID (bigint):', clubId);
      console.log('🔧 clubId.toString():', clubId.toString());

      const club = await clubService.getClubById(clubId);
      console.log('✅ Club encontrado:', club);
      setClubName(club.name);
    } catch (err: any) {
      console.error('❌ Error cargando nombre del club:', err);

      // Mostrar detalles del error
      if (err.response) {
        console.error('📊 Error respuesta club:', {
          status: err.response.status,
          data: err.response.data,
          url: err.config?.url
        });
      }

      // Mostrar ID si no se puede obtener el nombre
      setClubName(`Club #${clubId}`);
    }
  };

  const loadAvailableSlots = async () => {
    if (!court || !selectedDate) return;

    try {
      setLoadingSlots(true);
      console.log('📅 Buscando slots para cancha:', court.id, 'fecha:', selectedDate);
      const dateObj = new Date(selectedDate);
      const slots = await courtService.getCourtAvailability(court.id, dateObj);
      console.log('✅ Slots encontrados:', slots);
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
      case 'TENNIS': return 'sports_tennis';
      case 'PADDLE': return 'sports';
      case 'FOOTBALL': return 'sports_soccer';
      case 'BASKETBALL': return 'sports_basketball';
      default: return 'sports';
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

  // Pantalla de carga mejorada
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-sport">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-white"></div>
          <p className="mt-4 text-white text-lg">Cargando detalles de la cancha...</p>
          <p className="mt-2 text-white/70 text-sm">ID: {id}</p>
        </div>
      </div>
    );
  }

  // Pantalla de error mejorada
  if (error || !court) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-sport px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-20 h-20 bg-gradient-to-br from-red-500 to-pink-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="material-icons text-3xl text-white">error</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar la cancha</h3>
          <p className="text-gray-600 mb-4">{error || 'Cancha no encontrada'}</p>
          <p className="text-gray-500 text-sm mb-6">ID solicitado: {id}</p>
          <div className="flex flex-col sm:flex-row gap-4">
            <button
              onClick={loadCourtDetails}
              className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors flex items-center gap-2 justify-center"
            >
              <span className="material-icons">refresh</span>
              Reintentar
            </button>
            <Link
              to="/courts"
              className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-3 px-6 rounded-xl transition-colors text-center flex items-center gap-2 justify-center"
            >
              <span className="material-icons">arrow_back</span>
              Volver a canchas
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Renderizado principal
  return (
    <div className="min-h-screen bg-gradient-sport py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-3 text-white mb-4">
          <Link to="/" className="text-white hover:text-gray-300 transition-colors flex items-center gap-1">
            <span className="material-icons text-sm">home</span>
            Inicio
          </Link>
          <span className="text-white/50">/</span>
          <Link to="/courts" className="text-white hover:text-gray-300 transition-colors flex items-center gap-1">
            <span className="material-icons text-sm">sports</span>
            Canchas
          </Link>
          <span className="text-white/50">/</span>
          <span className="font-semibold flex items-center gap-1">
            <span className="material-icons text-sm">chevron_right</span>
            {court.name}
          </span>
        </nav>

        <div className="bg-white rounded-3xl shadow-2xl p-8">
          {/* Court Header */}
          <div className="mb-10">
            <div className="flex flex-wrap justify-between items-start gap-6 mb-8">
              <h1 className="text-3xl md:text-4xl lg:text-5xl font-black text-gradient-sport">
                {court.name}
              </h1>
              <span className={`inline-flex items-center gap-2 px-6 py-3 rounded-full text-white font-bold text-sm ${court.isActive ? 'bg-gradient-sport' : 'bg-gradient-to-r from-red-500 to-pink-500'}`}>
                <span className="material-icons text-sm">circle</span>
                {court.isActive ? 'Disponible' : 'No disponible'}
              </span>
            </div>

            {/* Details Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {[
                { icon: getSportIcon(court.type), title: 'Deporte', value: court.type },
                { icon: 'payments', title: 'Precio por hora', value: `$${Number(court.pricePerHour).toFixed(2)}` },
                { icon: 'business', title: 'Club', value: clubName }
              ].map((detail, index) => (
                <div
                  key={index}
                  className="card border-2 border-transparent hover:border-sport hover:-translate-y-2 shadow-lg hover:shadow-xl"
                >
                  <div className="flex items-center gap-5">
                    <div className="w-16 h-16 bg-gradient-sport rounded-2xl flex items-center justify-center shadow-lg">
                      <span className="material-icons text-2xl text-white">{detail.icon}</span>
                    </div>
                    <div>
                      <h4 className="text-sm text-gray-500 font-semibold uppercase tracking-wider">{detail.title}</h4>
                      <p className="text-xl font-bold text-gray-50">{detail.value}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Reservation Section */}
          <div className="border-t border-gray-100 pt-10">
            <h3 className="text-2xl font-bold text-gray-900 mb-8 flex items-center gap-3">
              <span className="material-icons text-green-600">event</span>
              Reservar cancha
            </h3>

            <div className="bg-gray-50 rounded-2xl p-8">
              <div className="mb-8">
                <label className="block text-gray-700 font-bold mb-3 flex items-center gap-2">
                  <span className="material-icons text-green-600">calendar_today</span>
                  Seleccionar fecha
                </label>
                <input
                  type="date"
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  min={getMinDate()}
                  className="w-full text-black max-w-xs bg-white border-2 border-gray-200 rounded-xl px-4 py-3 text-lg focus:outline-none focus:border-green-500 focus:ring-2 focus:ring-green-200 transition-all"
                />
              </div>

              {loadingSlots ? (
                <div className="text-center py-8">
                  <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
                  <p className="mt-4 text-gray-600">Cargando horarios disponibles...</p>
                </div>
              ) : availableSlots.length > 0 ? (
                <div className="mb-8">
                  <label className="block text-gray-700 font-bold mb-4 flex items-center gap-2">
                    <span className="material-icons text-green-600">schedule</span>
                    Horarios disponibles
                  </label>
                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
                    {availableSlots.map((slot, index) => (
                      <button
                        key={index}
                        type="button"
                        onClick={() => handleReservation(slot)}
                        className="bg-white border-2 border-gray-200 hover:border-green-500 hover:bg-green-50 text-gray-900 font-bold py-4 rounded-xl transition-all duration-300 hover:scale-105 active:scale-95 shadow-md hover:shadow-lg flex items-center justify-center gap-2"
                      >
                        <span className="material-icons text-sm">watch_later</span>
                        {formatTimeSlot(slot)}
                      </button>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="text-center py-8 bg-white rounded-xl border-2 border-gray-100">
                  <div className="flex flex-col items-center gap-3">
                    <span className="material-icons text-gray-400 text-4xl">schedule</span>
                    <p className="text-gray-600">No hay horarios disponibles para esta fecha</p>
                  </div>
                </div>
              )}

              <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
                <button
                  type="button"
                  onClick={() => handleReservation()}
                  disabled={!court.isActive || availableSlots.length === 0}
                  className={`flex items-center gap-3 font-bold text-lg px-10 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 active:scale-95 hover:text-white ${!court.isActive || availableSlots.length === 0 ? 'bg-gray-300 cursor-not-allowed text-gray-500' : 'bg-gradient-sport hover:from-green-700 hover:to-emerald-700 text-white shadow-lg hover:shadow-xl'}`}
                >
                  <span className="material-icons">lock</span>
                  Reservar ahora
                </button>

                {!isAuthenticated && (
                  <p className="text-gray-600 flex items-center gap-2">
                    <span className="material-icons text-green-600">info</span>
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