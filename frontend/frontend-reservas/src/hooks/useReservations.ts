import { useState, useEffect } from 'react';
import { reservationService } from '../api/services/reservationService';
import {
  type ReservationRequest,
  type ReservationResponse
} from '../api/types/reservation.types';

interface UseReservationsOptions {
  userId?: bigint;
  courtId?: bigint;
  clubId?: bigint;
  status?: ReservationResponse['status'];
  startDate?: Date;
  endDate?: Date;
}

export const useReservations = (options?: UseReservationsOptions) => {
  const [reservations, setReservations] = useState<ReservationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchReservations = async () => {
    setLoading(true);
    try {
      // Adaptar según tu API
      const params: any = {};
      if (options?.userId) params.userId = options.userId;
      if (options?.courtId) params.courtId = options.courtId;
      if (options?.clubId) params.clubId = options.clubId;
      if (options?.status) params.status = options.status;
      if (options?.startDate) params.startDate = options.startDate.toISOString();
      if (options?.endDate) params.endDate = options.endDate.toISOString();

      const data = await reservationService.getReservations(params);
      setReservations(data);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Error al cargar reservas');
    } finally {
      setLoading(false);
    }
  };

  const addReservation = async (reservationData: ReservationRequest) => {
    setLoading(true);
    try {
      // Convertir bigints si es necesario
      const dataToSend = {
        ...reservationData,
        userId: BigInt(reservationData.userId),
        courtId: BigInt(reservationData.courtId),
        clubId: BigInt(reservationData.clubId),
        startTime: reservationData.startTime,
        endTime: reservationData.endTime,
        status: reservationData.status || 'PENDING',
        paymentStatus: reservationData.paymentStatus || 'PENDING',
        createdAt: reservationData.createdAt || new Date()
      };

      const newReservation = await reservationService.createReservation(dataToSend);
      setReservations(prev => [...prev, newReservation]);
      setError(null);
      return newReservation;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Error al crear reserva';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateReservationStatus = async (
    id: bigint,
    data: ReservationRequest
  ) => {
    setLoading(true);
    try {
      const updated = await reservationService.updateReservation(id, data);
      setReservations(prev => prev.map(r =>
        r.id === id ? { ...r, ...updated } : r
      ));
      setError(null);
      return updated;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Error al actualizar reserva';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const cancelReservationById = async (id: bigint) => {
    setLoading(true);
    try {
      await reservationService.cancelReservation(id);
      // Actualizar localmente
      setReservations(prev => prev.map(r =>
        r.id === id ? { ...r, status: 'CANCELLED' } : r
      ));
      setError(null);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Error al cancelar reserva';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deleteReservation = async (id: bigint) => {
    setLoading(true);
    try {
      await reservationService.cancelReservation(id);
      setReservations(prev => prev.filter(r => r.id !== id));
      setError(null);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Error al eliminar reserva';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Filtrar reservas por diferentes criterios
  const getUpcomingReservations = () => {
    const now = new Date();
    return reservations.filter(r =>
      new Date(r.startTime) > now &&
      r.status !== 'CANCELLED'
    );
  };

  const getPastReservations = () => {
    const now = new Date();
    return reservations.filter(r => new Date(r.startTime) <= now);
  };

  const getReservationsByStatus = (status: ReservationResponse['status']) => {
    return reservations.filter(r => r.status === status);
  };

  const getReservationById = (id: bigint) => {
    return reservations.find(r => r.id === id);
  };

  useEffect(() => {
    fetchReservations();
  }, [
    options?.userId,
    options?.courtId,
    options?.clubId,
    options?.status,
    options?.startDate?.toISOString(),
    options?.endDate?.toISOString()
  ]);

  return {
    // Estado
    reservations,
    loading,
    error,

    // Métodos CRUD
    fetchReservations,
    addReservation,
    updateReservation: updateReservationStatus,
    cancelReservation: cancelReservationById,
    deleteReservation,

    // Métodos de filtrado
    getUpcomingReservations,
    getPastReservations,
    getReservationsByStatus,
    getReservationById,

    // Filtros directos (computados)
    upcomingReservations: getUpcomingReservations(),
    pastReservations: getPastReservations(),
    pendingReservations: getReservationsByStatus('PENDING'),
    confirmedReservations: getReservationsByStatus('CONFIRMED'),
    cancelledReservations: getReservationsByStatus('CANCELLED'),

    // Utilidades
    clearError: () => setError(null),
    reset: () => {
      setReservations([]);
      setError(null);
    }
  };
};