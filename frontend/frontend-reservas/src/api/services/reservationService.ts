import type {ReservationRequest, ReservationResponse} from '../types/reservation.types';
import api from "../axiosConfig.ts";

// Helper para convertir bigint a string para JSON
const serializeBigInt = (data: any): any => {
  return JSON.parse(JSON.stringify(data, (_key, value) =>
    typeof value === 'bigint' ? value.toString() : value
  ));
};

// Helper para convertir string a bigint donde corresponda
const deserializeBigInt = (data: any): any => {
  const bigIntKeys = ['id', 'userId', 'courtId', 'clubId', 'totalAmount'];
  return JSON.parse(JSON.stringify(data), (key, value) =>
    bigIntKeys.includes(key) && typeof value === 'string' ? BigInt(value) : value
  );
};

export const reservationService = {
  getReservations: async (filters?: {
    userId?: bigint;
    courtId?: bigint;
    clubId?: bigint;
    status?: string;
    startDate?: string;
    endDate?: string;
  }): Promise<ReservationResponse[]> => {
    const serializedFilters = serializeBigInt(filters);
    const response = await api.get('/reservations', {params: serializedFilters});
    return deserializeBigInt(response.data);
  },

  // Obtener reservas del usuario - FALTA EN BACK
  getUserReservations: async (): Promise<ReservationResponse[]> => {
    const response = await api.get('/reservations/my-reservations');
    return response.data;
  },

  // Crear reserva
  createReservation: async (data: ReservationRequest): Promise<ReservationResponse> => {
    const response = await api.post('/reservations', data);
    return response.data;
  },

  // Cancelar reserva
  cancelReservation: async (id: bigint): Promise<ReservationResponse> => {
    const response = await api.delete(`/reservations/${id}/cancel`);
    return response.data;
  },

  // Obtener reserva por ID
  getReservationById: async (id: bigint): Promise<ReservationResponse> => {
    const response = await api.get(`/reservations/${id}`);
    return response.data;
  },

  // Actualizar reserva por ID
  updateReservation: async (id: bigint, data: ReservationRequest): Promise<ReservationResponse> => {
    const response = await api.put(`/reservations/${id}`, data);
    return response.data;
  },

  /**
   * Obtener reservas por club (para administradores de club)
   */
  getReservationsByClub: async (
    clubId: bigint,
    filters?: {
      status?: string;
      startDate?: string;
      endDate?: string;
      courtId?: bigint;
      limit?: number;
    }
  ): Promise<ReservationResponse[]> => {
    try {
      const response = await api.get(`/reservations/club/${clubId}`, {
        params: serializeBigInt(filters)
      });
      return deserializeBigInt(response.data);
    } catch (error) {
      console.error(`Error al obtener reservas del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Obtener estadísticas de reservas por club
   */
  getReservationStatsByClub: async (
    clubId: bigint,
    timeRange: 'today' | 'week' | 'month' | 'year'
  ): Promise<{
    totalReservations: number;
    activeReservations: number;
    pendingReservations: number;
    cancelledReservations: number;
    revenue: number;
    averageBookingValue: number;
    mostPopularCourt: { courtId: bigint; courtName: string; count: number; };
    peakHours: Array<{ hour: number; count: number; }>;
  }> => {
    try {
      const response = await api.get(`/reservations/club/${clubId}/stats`, {
        params: { timeRange }
      });
      return deserializeBigInt(response.data);
    } catch (error) {
      console.error(`Error al obtener estadísticas del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Obtener reservas recientes por club
   */
  getRecentReservationsByClub: async (
    clubId: bigint,
    limit: number = 10
  ): Promise<ReservationResponse[]> => {
    try {
      const response = await api.get(`/reservations/club/${clubId}/recent`, {
        params: { limit }
      });
      return deserializeBigInt(response.data);
    } catch (error) {
      console.error(`Error al obtener reservas recientes del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Confirmar reserva (para administradores de club)
   */
  confirmReservation: async (
    reservationId: bigint,
    adminNotes?: string
  ): Promise<ReservationResponse> => {
    try {
      const response = await api.patch(`/reservations/${reservationId}/confirm`, {
        adminNotes
      });
      return deserializeBigInt(response.data);
    } catch (error) {
      console.error(`Error al confirmar reserva ${reservationId}:`, error);
      throw error;
    }
  },

  /**
   * Rechazar reserva (para administradores de club)
   */
  rejectReservation: async (
    reservationId: bigint,
    reason?: string
  ): Promise<ReservationResponse> => {
    try {
      const response = await api.patch(`/reservations/${reservationId}/reject`, {
        reason
      });
      return deserializeBigInt(response.data);
    } catch (error) {
      console.error(`Error al rechazar reserva ${reservationId}:`, error);
      throw error;
    }
  },

  /**
   * Actualizar estado de pago de reserva
   */
  updatePaymentStatus: async (
    reservationId: bigint,
    paymentStatus: 'PENDING' | 'CONFIRMED' | 'REFUNDED' | 'FAILED',
    paymentMethod?: string,
    transactionId?: string
  ): Promise<ReservationResponse> => {
    try {
      const response = await api.patch(`/reservations/${reservationId}/payment-status`, {
        paymentStatus,
        paymentMethod,
        transactionId
      });
      return deserializeBigInt(response.data);
    } catch (error) {
      console.error(`Error al actualizar estado de pago ${reservationId}:`, error);
      throw error;
    }
  },
};