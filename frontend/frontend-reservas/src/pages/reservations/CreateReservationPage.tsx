import React, {useState, useEffect} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {useAuth} from '../../context/AuthContext';
import {courtService} from '../../api/services/courtService';
import {reservationService} from '../../api/services/reservationService';
import type {CourtResponse} from '../../api/types/court.types';
import type {ReservationRequest} from '../../api/types/reservation.types';

const CreateReservationPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const {user, isAuthenticated} = useAuth();

  const [court, setCourt] = useState<CourtResponse | null>(null);
  const [club, setClub] = useState<{ id: bigint } | null>(null);
  const [date, setDate] = useState<string>('');
  const [startTime, setStartTime] = useState<string>('');
  const [endTime, setEndTime] = useState<string>('');
  const [notes, setNotes] = useState<string>('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [loadingSlots, setLoadingSlots] = useState<boolean>(false);

  const courtId = searchParams.get('courtId');
  const dateParam = searchParams.get('date');
  const startTimeParam = searchParams.get('startTime');

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', {state: {from: '/reservations/new'}});
      return;
    }

    if (!courtId) {
      navigate('/courts');
      return;
    }

    loadCourtDetails();
  }, [courtId, isAuthenticated, navigate]);

  useEffect(() => {
    if (court && date) {
      loadAvailableSlots();
    }
  }, [court, date]);

  useEffect(() => {
    if (availableSlots.length > 0) {
      // Si hay un horario en los parámetros y está disponible, usarlo
      if (startTimeParam && availableSlots.includes(startTimeParam)) {
        setStartTime(startTimeParam);
        // Calcular hora de fin automática (1 hora después)
        const nextHour = calculateNextHour(startTimeParam);
        setEndTime(nextHour);
      }
      // Si no hay startTime seleccionado, usar el primer horario disponible
      else if (!startTime && availableSlots.length > 0) {
        setStartTime(availableSlots[0]);
        const nextHour = calculateNextHour(availableSlots[0]);
        setEndTime(nextHour);
      }
    }
  }, [availableSlots, startTimeParam]);

  // Calcular la siguiente hora
  const calculateNextHour = (time: string): string => {
    const [hours, minutes] = time.split(':').map(Number);
    const nextHour = hours + 1;
    if (nextHour > 22) return '23:00';
    return `${nextHour.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  };

  const getClubDisplayName = (court: CourtResponse): string => {
    // Prioridad 1: clubName del backend
    if (court.clubName && court.clubName.trim() !== '' && !court.clubName.startsWith('Club #')) {
      return court.clubName;
    }

    // Prioridad 2: Si existe clubId, mostrar formato genérico
    if (court.clubId) {
      return `Club #${court.clubId}`;
    }

    // Fallback
    return "Sin club";
  };

  const loadCourtDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const courtData = await courtService.getCourtById(BigInt(courtId!));
      setCourt(courtData);

      if (courtData.clubId) {
        setClub({
          id: courtData.clubId
        });
      } else {
        throw new Error('La cancha no está asociada a un club válido');
      }

      // Establecer fecha: usar parámetro o mañana por defecto
      if (dateParam) {
        setDate(dateParam);
      } else {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        setDate(tomorrow.toISOString().split('T')[0]);
      }
    } catch (err: any) {
      setError(err.message || 'Error al cargar los detalles de la cancha');
      console.error('Error loading court details:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadAvailableSlots = async () => {
    if (!court || !date) return;

    try {
      setLoadingSlots(true);
      setError(null);

      const dateObj = new Date(date + 'T00:00:00');
      const slots = await courtService.getCourtAvailability(court.id, dateObj);

      console.log('Available slots for', date, ':', slots);
      setAvailableSlots(slots);

      if (slots.length === 0) {
        setError('No hay horarios disponibles para la fecha seleccionada');
        setStartTime('');
        setEndTime('');
      }

    } catch (err: any) {
      console.error('Error loading available slots:', err);

      if (err.response?.status === 403) {
        setError('No tienes permiso para ver la disponibilidad de esta cancha');
      } else if (err.response?.status === 404) {
        setError('El endpoint de disponibilidad no fue encontrado');
      } else {
        setError('Error al cargar los horarios disponibles. Por favor, intenta de nuevo.');
      }

      setAvailableSlots([]);
      setStartTime('');
      setEndTime('');
    } finally {
      setLoadingSlots(false);
    }
  };

  const isTimeSlotAvailable = (time: string): boolean => {
    return availableSlots.includes(time);
  };

  const calculateDuration = (): number => {
    if (!startTime || !endTime) return 0;

    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [endHours, endMinutes] = endTime.split(':').map(Number);

    const startTotalMinutes = startHours * 60 + startMinutes;
    const endTotalMinutes = endHours * 60 + endMinutes;

    const durationMinutes = endTotalMinutes - startTotalMinutes;

    return durationMinutes > 0 ? durationMinutes / 60 : 0;
  };

  const calculateTotalPrice = (): number => {
    if (!court) return 0;
    const duration = calculateDuration();
    return Number(court.pricePerHour) * duration;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!court || !user || !club) {
      setError('Faltan datos necesarios para crear la reserva');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      if (!date) {
        throw new Error('Selecciona una fecha');
      }

      if (!startTime) {
        throw new Error('Selecciona una hora de inicio');
      }

      if (!endTime) {
        throw new Error('Selecciona una hora de fin');
      }

      if (!isTimeSlotAvailable(startTime)) {
        throw new Error('El horario seleccionado no está disponible');
      }

      const startDateTime = new Date(`${date}T${startTime}`);
      const endDateTime = new Date(`${date}T${endTime}`);

      if (endDateTime <= startDateTime) {
        throw new Error('La hora de fin debe ser posterior a la hora de inicio');
      }

      const reservationData: ReservationRequest = {
        userId: user.id,
        courtId: court.id,
        clubId: club.id,
        startTime: startDateTime,
        endTime: endDateTime,
        status: "PENDING",
        paymentStatus: "PENDING",
        createdAt: new Date(),
      };

      await reservationService.createReservation(reservationData);

      navigate('/reservations', {
        state: {
          message: 'Reserva creada exitosamente',
          reservationCreated: true
        }
      });

    } catch (err: any) {
      setError(err.message || 'Error al crear la reserva');
      console.error('Error creating reservation:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const getMinDate = (): string => {
    return new Date().toISOString().split('T')[0];
  };

  const generateTimeOptions = () => {
    const options = [];
    for (let hour = 8; hour <= 22; hour++) {
      for (let minute of ['00', '30']) {
        const time = `${hour.toString().padStart(2, '0')}:${minute}`;
        options.push(time);
      }
    }
    return options;
  };

  if (!isAuthenticated) {
    return null;
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500">
        <div className="bg-white rounded-2xl p-12 text-center shadow-2xl">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-green-600 mb-6"></div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Cargando información</h3>
          <p className="text-gray-600">Cargando detalles de la cancha...</p>
        </div>
      </div>
    );
  }

  if (error && !court) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500 px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-20 h-20 bg-gradient-to-br from-red-500 to-teal-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="material-icons text-3xl text-white">error</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar la cancha</h3>
          <p className="text-gray-600 mb-8">{error}</p>
          <button
            onClick={() => navigate('/courts')}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors flex items-center justify-center gap-2"
          >
            <span className="material-icons">arrow_back</span>
            Volver a canchas
          </button>
        </div>
      </div>
    );
  }

  if (!court || !club) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500 px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-16 h-16 bg-gradient-to-br from-gray-400 to-gray-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="material-icons text-2xl text-white">sports</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-4">Cancha no encontrada o sin club asociado</h3>
          <button
            onClick={() => navigate('/courts')}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors flex items-center justify-center gap-2"
          >
            <span className="material-icons">arrow_back</span>
            Volver a canchas
          </button>
        </div>
      </div>
    );
  }

  const duration = calculateDuration();
  const totalPrice = calculateTotalPrice();

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500 py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-3 text-white mb-8">
          <button
            onClick={() => navigate('/courts')}
            className="bg-white/15 backdrop-blur-sm hover:bg-white/25 text-white border-2 border-white/30 px-5 py-2 rounded-xl transition-all duration-300 hover:scale-105 flex items-center gap-2"
          >
            <span className="material-icons text-sm">sports</span>
            Canchas
          </button>
          <span className="material-icons text-white/50">chevron_right</span>
          <button
            onClick={() => navigate(`/courts/${court.id}`)}
            className="bg-white/15 backdrop-blur-sm hover:bg-white/25 text-white border-2 border-white/30 px-5 py-2 rounded-xl transition-all duration-300 hover:scale-105 flex items-center gap-2"
          >
            <span className="material-icons text-sm">stadium</span>
            {court.name}
          </button>
          <span className="material-icons text-white/50">chevron_right</span>
          <span className="font-bold flex items-center gap-2">
            <span className="material-icons">add_circle</span>
            Nueva Reserva
          </span>
        </nav>

        <div className="bg-white rounded-3xl shadow-2xl p-8 animate-scale-in">
          <div className="text-center mb-10">
            <h1 className="text-4xl md:text-5xl font-black bg-gradient-to-r from-gray-900 via-green-600 to-emerald-600 bg-clip-text text-transparent mb-4 flex items-center justify-center gap-3">
              <span className="material-icons text-4xl">add_circle</span>
              Nueva Reserva
            </h1>
            <p className="text-gray-600 text-xl flex items-center justify-center gap-2">
              <span className="material-icons">edit_calendar</span>
              Completa los detalles para reservar la cancha
            </p>
          </div>

          {/* Court Summary */}
          <div className="bg-gradient-to-br from-gray-50 to-white rounded-2xl p-8 mb-10 border-l-4 border-green-500 shadow-lg hover:shadow-xl transition-shadow duration-500">
            <h3 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-3">
              <span className="material-icons text-3xl text-green-600">sports</span>
              Cancha seleccionada
            </h3>
            <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-8">
              <div>
                <h4 className="text-2xl font-bold text-gray-900 mb-4">{court.name}</h4>
                <div className="text-black flex flex-wrap gap-4">
                  <span className="bg-gray-200 px-4 py-2 rounded-xl shadow-sm flex items-center gap-2">
                    <span className="material-icons text-green-600">attach_money</span>
                    ${Number(court.pricePerHour).toFixed(2)}/hora
                  </span>
                  <span className="bg-gray-200 px-4 py-2 rounded-xl shadow-sm flex items-center gap-2">
                    <span className="material-icons text-blue-600">category</span>
                    Tipo: {court.type}
                  </span>
                  <span className="bg-gray-200 px-4 py-2 rounded-xl shadow-sm flex items-center gap-2">
                    <span className="material-icons text-purple-600">business</span>
                    Club: {getClubDisplayName(court)}
                  </span>
                </div>
              </div>
              <button
                onClick={() => navigate(`/courts/${court.id}`)}
                className="bg-white border-2 border-green-500 text-green-600 hover:bg-green-500 hover:border-green-500 hover:text-white font-bold px-8 py-3 rounded-xl transition-all duration-300 transform hover:scale-105 flex items-center gap-2"
              >
                <span className="material-icons">visibility</span>
                Ver detalles
              </button>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-8">
            {error && (
              <div className="bg-red-50 border-l-4 border-red-500 p-6 rounded-2xl">
                <div className="flex items-center gap-4">
                  <span className="material-icons text-red-500 text-3xl">warning</span>
                  <p className="text-red-700 font-bold">{error}</p>
                </div>
              </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Fecha */}
              <div>
                <label className="block text-gray-700 font-bold mb-4 flex items-center gap-3">
                  <span className="material-icons text-green-600 text-2xl">calendar_today</span>
                  Fecha
                </label>
                <input
                  type="date"
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  min={getMinDate()}
                  required
                  className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all"
                />
              </div>

              {/* Hora de inicio */}
              <div>
                <label className="block text-gray-700 font-bold mb-4 flex items-center gap-3">
                  <span className="material-icons text-blue-600 text-2xl">schedule</span>
                  Hora de inicio
                  {loadingSlots && (
                    <span className="text-sm text-gray-500 ml-2">(Cargando...)</span>
                  )}
                </label>
                <select
                  value={startTime}
                  onChange={(e) => {
                    setStartTime(e.target.value);
                    // Auto-calcular hora de fin
                    if (e.target.value) {
                      const nextHour = calculateNextHour(e.target.value);
                      setEndTime(nextHour);
                    }
                  }}
                  required
                  disabled={loadingSlots || availableSlots.length === 0}
                  className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all appearance-none disabled:bg-gray-100 disabled:cursor-not-allowed"
                >
                  <option value="">Seleccionar hora</option>
                  {generateTimeOptions().map(time => (
                    <option
                      key={time}
                      value={time}
                      disabled={!isTimeSlotAvailable(time)}
                      className={!isTimeSlotAvailable(time) ? 'text-gray-400' : ''}
                    >
                      {time} {!isTimeSlotAvailable(time) ? '(No disponible)' : ''}
                    </option>
                  ))}
                </select>
                {availableSlots.length === 0 && date && !loadingSlots && (
                  <div className="bg-yellow-50 border-l-4 border-yellow-500 p-4 rounded-xl mt-4">
                    <div className="flex items-center gap-3">
                      <span className="material-icons text-yellow-500 text-xl">info</span>
                      <p className="text-yellow-700 text-sm font-semibold">
                        No hay horarios disponibles para esta fecha. Selecciona otra fecha.
                      </p>
                    </div>
                  </div>
                )}
              </div>

              {/* Hora de fin */}
              <div>
                <label className="block text-gray-700 font-bold mb-4 flex items-center gap-3">
                  <span className="material-icons text-blue-600 text-2xl">access_time</span>
                  Hora de fin
                </label>
                <select
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  required
                  disabled={!startTime}
                  className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all appearance-none disabled:bg-gray-100 disabled:cursor-not-allowed"
                >
                  <option value="">Seleccionar hora</option>
                  {generateTimeOptions().map(time => (
                    <option
                      key={time}
                      value={time}
                      disabled={time <= startTime}
                      className={time <= startTime ? 'text-gray-400' : ''}
                    >
                      {time} {time <= startTime ? '(Debe ser posterior)' : ''}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Notas */}
            <div>
              <label className="block text-gray-700 font-bold mb-4 flex items-center gap-3">
                <span className="material-icons text-gray-600 text-2xl">notes</span>
                Notas (opcional)
              </label>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Comentarios adicionales..."
                className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all h-40"
                rows={4}
              />
            </div>

            {/* Price Summary */}
            <div className="bg-gradient-to-r from-green-700 to-emerald-700 rounded-3xl p-8 text-white shadow-xl">
              <h3 className="text-2xl font-bold mb-8 flex items-center gap-3">
                <span className="material-icons">receipt</span>
                Resumen de la reserva
              </h3>
              <div className="space-y-6">
                <div className="flex justify-between items-center py-4 border-b border-white/20">
                  <span className="flex items-center gap-2">
                    <span className="material-icons text-sm">hourglass_empty</span>
                    Duración:
                  </span>
                  <span className="font-bold text-xl">
                    {duration > 0
                      ? `${duration} ${duration === 1 ? 'hora' : 'horas'}`
                      : 'Selecciona horarios'
                    }
                  </span>
                </div>
                <div className="flex justify-between items-center py-4 border-b border-white/20">
                  <span className="flex items-center gap-2">
                    <span className="material-icons text-sm">attach_money</span>
                    Precio por hora:
                  </span>
                  <span className="font-bold text-xl">${Number(court.pricePerHour).toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center py-6 border-t border-white/30 mt-4">
                  <span className="text-2xl flex items-center gap-3">
                    <span className="material-icons">payments</span>
                    Total estimado:
                  </span>
                  <span className="text-4xl font-black">
                    {totalPrice > 0 ? `$${totalPrice.toFixed(2)}` : '$0.00'}
                  </span>
                </div>
              </div>
            </div>

            {/* Botones */}
            <div className="flex flex-col sm:flex-row gap-6">
              <button
                type="button"
                onClick={() => navigate(-1)}
                disabled={submitting}
                className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold text-lg py-5 px-8 rounded-2xl transition-all duration-300 transform hover:scale-105 disabled:opacity-50"
              >
                <div className="flex items-center justify-center gap-3">
                  <span className="material-icons">arrow_back</span>
                  Cancelar
                </div>
              </button>
              <button
                type="submit"
                disabled={submitting || !startTime || !endTime || duration <= 0}
                className="flex-1 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white font-bold text-lg py-5 px-8 rounded-2xl transition-all duration-300 transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {submitting ? (
                  <div className="flex items-center justify-center gap-3">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white"></div>
                    Procesando...
                  </div>
                ) : (
                  <div className="flex items-center justify-center gap-3">
                    <span className="material-icons">check_circle</span>
                    Confirmar Reserva
                  </div>
                )}
              </button>
            </div>

            {/* Términos */}
            <div className="bg-gray-100 border-l-4 border-emerald-500 p-6 rounded-2xl">
              <div className="flex items-start gap-4">
                <span className="material-icons text-emerald-500 text-2xl">info</span>
                <p className="text-emerald-800">
                  Al confirmar la reserva, aceptas las políticas de cancelación del club.
                  Las reservas pueden cancelarse hasta 24 horas antes sin cargo.
                </p>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateReservationPage;