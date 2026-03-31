import type {CourtResponse, CourtRequest} from '../types/court.types';
import api from "../axiosConfig.ts";

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
        // Formatear como YYYY-MM-DD
        const dateStr = date.toISOString().split('T')[0];

        const response = await api.get(`/courts/${courtId}/available`, {
            params: { date: dateStr }
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

    getCourtsByClub: async (clubId: bigint): Promise<CourtResponse[]> => {
        try {
            const response = await api.get(`/courts/club/${clubId}`);
            return response.data;
        } catch (error) {
            console.error(`Error al obtener canchas del club ${clubId}:`, error);
            throw error;
        }
    },

    /**
     * Obtener estadísticas de canchas por club
     */
    getCourtStatsByClub: async (clubId: bigint): Promise<{
        totalCourts: number;
        activeCourts: number;
        maintenanceCourts: number;
        popularCourts: Array<{ id: bigint; name: string; bookingCount: number; }>;
        courtTypes: Array<{ type: string; count: number; }>;
    }> => {
        try {
            const response = await api.get(`/courts/club/${clubId}/stats`);
            return response.data;
        } catch (error) {
            console.error(`Error al obtener estadísticas de canchas del club ${clubId}:`, error);
            throw error;
        }
    },

    /**
     * Actualizar estado de cancha
     */
    updateCourtStatus: async (
      courtId: bigint,
      status: 'ACTIVE' | 'MAINTENANCE' | 'INACTIVE',
      maintenanceNotes?: string,
      estimatedRepairDate?: string
    ): Promise<CourtResponse> => {
        try {
            const response = await api.patch(`/courts/${courtId}/status`, {
                status,
                maintenanceNotes,
                estimatedRepairDate
            });
            return response.data;
        } catch (error) {
            console.error(`Error al actualizar estado de cancha ${courtId}:`, error);
            throw error;
        }
    },
};