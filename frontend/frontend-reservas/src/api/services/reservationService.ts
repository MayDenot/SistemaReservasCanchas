import api from '../axiosConfig';
import type {ReservationRequest, ReservationResponse} from '../types/reservation.types';

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
};