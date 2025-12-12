import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useReservations } from '../../hooks/useReservations';

const ReservationDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { getReservationById } = useReservations();
  const [reservation, setReservation] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchReservation = async () => {
      try {
        if (id) {
          const data = await getReservationById(BigInt(id));
          setReservation(data);
        }
      } catch (err) {
        setError('Error al cargar la reserva');
      } finally {
        setLoading(false);
      }
    };
    fetchReservation();
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-sport">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-white"></div>
          <p className="mt-4 text-white text-lg">Cargando detalles de la reserva...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-sport px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-20 h-20 bg-gradient-to-br from-red-500 to-teal-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="text-3xl">⚠️</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error</h3>
          <p className="text-gray-600 mb-8">{error}</p>
        </div>
      </div>
    );
  }

  if (!reservation) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-sport px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <h3 className="text-2xl font-bold text-gray-900 mb-4">Reserva no encontrada</h3>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-sport py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-3xl shadow-2xl p-8">
          <div className="text-center mb-10">
            <h1 className="text-4xl md:text-5xl font-black text-gradient-sport mb-4">
              Detalle de Reserva
            </h1>
            <p className="text-gray-600 text-xl">
              Información completa de tu reserva
            </p>
          </div>

          <div className="bg-gradient-to-br from-gray-50 to-white rounded-2xl p-8 mb-8">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
              <div>
                <h3 className="text-2xl font-bold text-gray-900 mb-6">Información General</h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center py-3 border-b border-gray-100">
                    <span className="text-gray-600">ID Reserva:</span>
                    <span className="font-bold text-lg">#{reservation.id?.toString().slice(-6)}</span>
                  </div>
                  <div className="flex justify-between items-center py-3 border-b border-gray-100">
                    <span className="text-gray-600">Estado:</span>
                    <span className="px-4 py-2 success rounded-full font-bold">
                      {reservation.status || 'Confirmada'}
                    </span>
                  </div>
                  <div className="flex justify-between items-center py-3">
                    <span className="text-gray-600">Pago:</span>
                    <span className="px-4 py-2 bg-blue-100 text-blue-800 rounded-full font-bold">
                      {reservation.paymentStatus || 'Pagado'}
                    </span>
                  </div>
                </div>
              </div>

              <div>
                <h3 className="text-2xl font-bold text-gray-900 mb-6">Horarios</h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center py-3 border-b border-gray-100">
                    <span className="text-gray-600">Fecha:</span>
                    <span className="font-bold">
                      {reservation.startTime ? new Date(reservation.startTime).toLocaleDateString('es-ES') : 'N/A'}
                    </span>
                  </div>
                  <div className="flex justify-between items-center py-3 border-b border-gray-100">
                    <span className="text-gray-600">Hora inicio:</span>
                    <span className="font-bold">
                      {reservation.startTime ? new Date(reservation.startTime).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }) : 'N/A'}
                    </span>
                  </div>
                  <div className="flex justify-between items-center py-3">
                    <span className="text-gray-600">Hora fin:</span>
                    <span className="font-bold">
                      {reservation.endTime ? new Date(reservation.endTime).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }) : 'N/A'}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white p-6 rounded-xl border border-gray-100">
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-12 h-12 bg-gradient-to-br from-green-100 to-emerald-100 rounded-xl flex items-center justify-center">
                    <span className="text-2xl">🏸</span>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Cancha ID</div>
                    <div className="text-xl font-bold">{reservation.courtId?.toString() || 'N/A'}</div>
                  </div>
                </div>
              </div>

              <div className="bg-white p-6 rounded-xl border border-gray-100">
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-12 h-12 bg-gradient-to-br from-green-100 to-emerald-100 rounded-xl flex items-center justify-center">
                    <span className="text-2xl">🏢</span>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Club ID</div>
                    <div className="text-xl font-bold">{reservation.clubId?.toString() || 'N/A'}</div>
                  </div>
                </div>
              </div>

              <div className="bg-white p-6 rounded-xl border border-gray-100">
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-12 h-12 bg-gradient-to-br from-blue-100 to-cyan-100 rounded-xl flex items-center justify-center">
                    <span className="text-2xl">👤</span>
                  </div>
                  <div>
                    <div className="text-sm text-gray-500">Usuario ID</div>
                    <div className="text-xl font-bold">{reservation.userId?.toString() || 'N/A'}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="flex gap-6">
            <button
              onClick={() => window.history.back()}
              className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-4 px-8 rounded-2xl transition-all duration-300 transform hover:scale-105"
            >
              Volver
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReservationDetailPage;