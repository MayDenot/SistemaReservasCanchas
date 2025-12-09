import api from '../axiosConfig';
import type {CourtResponse, CourtRequest} from '../types/court.types';

export const courtService = {
    // Obtener todas las canchas
    getAllCourts: async (filters?: {
        limit?: number,
        date?: Date;
    }): Promise<CourtResponse[]> => {
        const response = await api.get('/courts', { params: filters });
        return response.data;
    },

    // Obtener cancha por ID
    getCourtById: async (id: bigint): Promise<CourtResponse> => {
        const response = await api.get(`/courts/${id}`);
        return response.data;
    },

    // Obtener disponibilidad
    getCourtAvailability: async (courtId: bigint, date: Date): Promise<string[]> => {
        const response = await api.get(`/courts/${courtId}/available`, {
            params: { date },
        });
        return response.data;
    },

    // Crear cancha (admin/club owner)
    createCourt: async (courtData: CourtRequest): Promise<CourtResponse> => {
        const response = await api.post('/courts', courtData);
        return response.data;
    },

    // Actualizar cancha
    updateCourt: async (id: bigint, courtData: CourtRequest): Promise<CourtResponse> => {
        const response = await api.put(`/courts/${id}`, courtData);
        return response.data;
    },

    // Eliminar cancha
    deleteCourt: async (id: bigint): Promise<void> => {
        await api.delete(`/courts/${id}`);
    },
};