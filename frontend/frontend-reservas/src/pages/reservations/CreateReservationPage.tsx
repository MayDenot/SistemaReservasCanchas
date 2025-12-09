import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { courtService } from '../../api/services/courtService';
import { reservationService } from '../../api/services/reservationService';
import type { CourtResponse } from '../../api/types/court.types';
import type { ReservationRequest } from '../../api/types/reservation.types';

const CreateReservationPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user, isAuthenticated } = useAuth();

  const [court, setCourt] = useState<CourtResponse | null>(null);
  const [club, setClub] = useState<{ id: bigint } | null>(null);
  const [date, setDate] = useState<string>('');
  const [startTime, setStartTime] = useState<string>('08:00');
  const [endTime, setEndTime] = useState<string>('09:00');
  const [notes, setNotes] = useState<string>('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const courtId = searchParams.get('courtId');
  const dateParam = searchParams.get('date');
  const startTimeParam = searchParams.get('startTime');

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/reservations/new' } });
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
    if (startTimeParam) {
      setStartTime(startTimeParam);
      const [hours, minutes] = startTimeParam.split(':');
      const endHour = (parseInt(hours) + 1).toString().padStart(2, '0');
      setEndTime(`${endHour}:${minutes}`);
    }

    if (dateParam) {
      setDate(dateParam);
    }
  }, [startTimeParam, dateParam]);

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

      if (!dateParam) {
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
      const dateObj = new Date(date);
      const slots = await courtService.getCourtAvailability(court.id, dateObj);
      setAvailableSlots(slots);

      if (startTimeParam && slots.includes(startTimeParam)) {
        setStartTime(startTimeParam);
      }
    } catch (err) {
      console.error('Error loading available slots:', err);
      setAvailableSlots([]);
    }
  };

  const isTimeSlotAvailable = (time: string): boolean => {
    return availableSlots.includes(time);
  };

  const calculateDuration = (): number => {
    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [endHours, endMinutes] = endTime.split(':').map(Number);

    const startTotalMinutes = startHours * 60 + startMinutes;
    const endTotalMinutes = endHours * 60 + endMinutes;

    return (endTotalMinutes - startTotalMinutes) / 60;
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
            <span className="text-3xl">⚠️</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar la cancha</h3>
          <p className="text-gray-600 mb-8">{error}</p>
          <button
            onClick={() => navigate('/courts')}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors"
          >
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
          <h3 className="text-2xl font-bold text-gray-900 mb-4">Cancha no encontrada o sin club asociado</h3>
          <button
            onClick={() => navigate('/courts')}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors"
          >
            Volver a canchas
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500 py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-3 text-white mb-8">
          <button
            onClick={() => navigate('/courts')}
            className="bg-white/15 backdrop-blur-sm hover:bg-white/25 text-white border-2 border-white/30 px-5 py-2 rounded-xl transition-all duration-300 hover:scale-105"
          >
            Canchas
          </button>
          <span className="text-white/50">→</span>
          <button
            onClick={() => navigate(`/courts/${court.id}`)}
            className="bg-white/15 backdrop-blur-sm hover:bg-white/25 text-white border-2 border-white/30 px-5 py-2 rounded-xl transition-all duration-300 hover:scale-105"
          >
            {court.name}
          </button>
          <span className="text-white/50">→</span>
          <span className="font-bold">Nueva Reserva</span>
        </nav>

        <div className="bg-white rounded-3xl shadow-2xl p-8 animate-scale-in">
          <div className="text-center mb-10">
            <h1 className="text-4xl md:text-5xl font-black bg-gradient-to-r from-gray-900 via-green-600 to-emerald-600 bg-clip-text text-transparent mb-4">
              Nueva Reserva
            </h1>
            <p className="text-gray-600 text-xl">
              Completa los detalles para reservar la cancha
            </p>
          </div>

          {/* Court Summary */}
          <div className="bg-gradient-to-br from-gray-50 to-white rounded-2xl p-8 mb-10 border-l-4 border-green-500 shadow-lg hover:shadow-xl transition-shadow duration-500">
            <h3 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-3">
              <span className="text-3xl">🎾</span>
              Cancha seleccionada
            </h3>
            <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-8">
              <div>
                <h4 className="text-2xl font-bold text-gray-900 mb-4">{court.name}</h4>
                <div className="flex flex-wrap gap-4">
                  <span className="bg-white px-4 py-2 rounded-xl shadow-sm flex items-center gap-2">
                    <span className="text-lg">💰</span>
                    ${Number(court.pricePerHour).toFixed(2)}/hora
                  </span>
                  <span className="bg-white px-4 py-2 rounded-xl shadow-sm flex items-center gap-2">
                    <span className="text-lg">🏢</span>
                    Tipo: {court.type}
                  </span>
                  <span className="bg-white px-4 py-2 rounded-xl shadow-sm flex items-center gap-2">
                    <span className="text-lg">🏛️</span>
                    Club ID: {club.id.toString()}
                  </span>
                </div>
              </div>
              <button
                onClick={() => navigate(`/courts/${court.id}`)}
                className="bg-white border-2 border-green-500 text-green-600 hover:bg-green-50 font-bold px-8 py-3 rounded-xl transition-all duration-300 transform hover:scale-105"
              >
                Ver detalles
              </button>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-8">
            {error && (
              <div className="bg-gradient-to-r from-red-50 to-red-100 border-l-4 border-red-500 p-6 rounded-2xl">
                <div className="flex items-center gap-4">
                  <span className="text-red-500 text-3xl">⚠️</span>
                  <p className="text-red-700 font-bold">{error}</p>
                </div>
              </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Fecha */}
              <div>
                <label className="block text-gray-700 font-bold mb-4 flex items-center gap-3">
                  <span className="text-2xl">📅</span>
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
                  <span className="text-2xl">⏰</span>
                  Hora de inicio
                </label>
                <select
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  required
                  className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all appearance-none bg-white"
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
                {availableSlots.length > 0 && (
                  <div className="mt-3 bg-green-50 text-green-700 px-4 py-2 rounded-xl text-sm font-bold flex items-center gap-2">
                    <span>✅</span>
                    Horarios disponibles: {availableSlots.join(', ')}
                  </div>
                )}
              </div>

              {/* Hora de fin */}
              <div>
                <label className="block text-gray-700 font-bold mb-4 flex items-center gap-3">
                  <span className="text-2xl">🕒</span>
                  Hora de fin
                </label>
                <select
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  required
                  className="w-full px-6 py-4 border-3 border-gray-200 rounded-2xl text-lg focus:outline-none focus:ring-4 focus:ring-green-200 focus:border-green-500 transition-all appearance-none bg-white"
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
                <span className="text-2xl">📝</span>
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
            <div className="bg-gradient-to-r from-green-600 to-emerald-600 rounded-3xl p-8 text-white shadow-xl">
              <h3 className="text-2xl font-bold mb-8">Resumen de la reserva</h3>
              <div className="space-y-6">
                <div className="flex justify-between items-center py-4 border-b border-white/20">
                  <span>Duración:</span>
                  <span className="font-bold text-xl">{calculateDuration()} horas</span>
                </div>
                <div className="flex justify-between items-center py-4 border-b border-white/20">
                  <span>Precio por hora:</span>
                  <span className="font-bold text-xl">${Number(court.pricePerHour).toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center py-6 border-t border-white/30 mt-4">
                  <span className="text-2xl">Total estimado:</span>
                  <span className="text-4xl font-black">${calculateTotalPrice().toFixed(2)}</span>
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
                  <span>←</span>
                  Cancelar
                </div>
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white font-bold text-lg py-5 px-8 rounded-2xl transition-all duration-300 transform hover:scale-105 disabled:opacity-50"
              >
                {submitting ? (
                  <div className="flex items-center justify-center gap-3">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white"></div>
                    Procesando...
                  </div>
                ) : (
                  <div className="flex items-center justify-center gap-3">
                    <span>✅</span>
                    Confirmar Reserva
                  </div>
                )}
              </button>
            </div>

            {/* Términos */}
            <div className="bg-gradient-to-r from-emerald-50 to-teal-50 border-l-4 border-emerald-500 p-6 rounded-2xl">
              <div className="flex items-start gap-4">
                <span className="text-emerald-500 text-2xl">ℹ️</span>
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