import type { ClubResponse } from "../types/club.types.ts";
import api from "../axiosConfig.ts";

export const clubService = {
  async getClubById(id: bigint) {
    try {
      const response = await api.get(`/clubs/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener club con ID ${id}:`, error);
      throw error;
    }
  },

  async getAllClubs() {
    try {
      const response = await api.get('/clubs');
      return response.data;
    } catch (error) {
      console.error('Error al obtener todos los clubes:', error);
      throw error;
    }
  },

  /**
   * Obtiene el club con información del usuario administrador
   */
  async getClubWithUser(clubId: bigint): Promise<ClubResponse> {
    try {
      const response = await api.get(`/clubs/${clubId}/with-user`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener club con usuario para ID ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Crea un nuevo club
   */
  async createClub(clubData: {
    name: string;
    address: string;
    phone?: string;
    openingTime: string;
    closingTime: string;
    adminId: bigint;
  }): Promise<ClubResponse> {
    try {
      const response = await api.post('/clubs', clubData);
      return response.data;
    } catch (error) {
      console.error('Error al crear club:', error);
      throw error;
    }
  },

  /**
   * Actualiza un club existente
   */
  async updateClub(
    clubId: bigint,
    clubData: Partial<{
      name: string;
      address: string;
      phone?: string;
      openingTime: string;
      closingTime: string;
      adminId: bigint;
    }>
  ): Promise<ClubResponse> {
    try {
      const response = await api.put(`/clubs/${clubId}`, clubData);
      return response.data;
    } catch (error) {
      console.error(`Error al actualizar club con ID ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Elimina un club
   */
  async deleteClub(clubId: bigint): Promise<void> {
    try {
      await api.delete(`/clubs/${clubId}`);
    } catch (error) {
      console.error(`Error al eliminar club con ID ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Verifica si un club existe por su nombre
   */
  async existsByName(name: string): Promise<boolean> {
    try {
      const response = await api.get('/clubs/exists', {
        params: { name }
      });
      return response.data;
    } catch (error) {
      console.error(`Error al verificar si existe club con nombre ${name}:`, error);
      throw error;
    }
  },

  /**
   * Verifica si un club existe por su ID
   */
  async existsById(clubId: bigint): Promise<boolean> {
    try {
      const response = await api.get(`/clubs/${clubId}/exists`);
      return response.data;
    } catch (error) {
      console.error(`Error al verificar si existe club con ID ${clubId}:`, error);
      return false;
    }
  },

  /**
   * Verifica si un club está abierto en una fecha y hora específica
   */
  async isClubOpenAt(
    clubId: bigint,
    dateTime: Date
  ): Promise<boolean> {
    try {
      const response = await api.get(`/clubs/${clubId}/is-open`, {
        params: { dateTime: dateTime.toISOString() }
      });
      return response.data;
    } catch (error) {
      console.error(`Error al verificar horario del club ${clubId}:`, error);
      throw error;
    }
  },

  getClubStats: async (clubId: bigint): Promise<{
    totalCourts: number;
    activeCourts: number;
    maintenanceCourts: number;
    totalReservations: number;
    activeReservations: number;
    pendingReservations: number;
    revenue: number;
    monthlyRevenue: number;
    weeklyRevenue: number;
    averageRating?: number;
    memberCount?: number;
  }> => {
    try {
      const response = await api.get(`/clubs/${clubId}/stats`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener estadísticas del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Obtener club por administrador (para el dashboard)
   */
  getClubByAdmin: async (adminId: bigint): Promise<ClubResponse[]> => {
    try {
      const response = await api.get(`/clubs/admin/${adminId}`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener clubes del administrador ${adminId}:`, error);
      throw error;
    }
  },

  /**
   * Obtener club del usuario actual (si es administrador)
   */
  getMyClub: async (): Promise<ClubResponse | null> => {
    try {
      const response = await api.get('/clubs/my-club');
      return response.data;
    } catch (error: any) {
      // Verificar si el error tiene response
      if (error?.response?.status === 404) {
        return null;
      }

      // Para otros errores
      console.error('Error al obtener club del usuario:', error);

      // O relanza el error
      throw error;
    }
  },

  /**
   * Actualizar horario del club
   */
  updateClubHours: async (
    clubId: bigint,
    openingTime: string,
    closingTime: string,
    daysOff?: string[] // Array de días en formato 'YYYY-MM-DD'
  ): Promise<ClubResponse> => {
    try {
      const response = await api.patch(`/clubs/${clubId}/hours`, {
        openingTime,
        closingTime,
        daysOff
      });
      return response.data;
    } catch (error) {
      console.error(`Error al actualizar horario del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Obtener horarios especiales del club (festivos, mantenimiento, etc.)
   */
  getSpecialHours: async (clubId: bigint): Promise<Array<{
    date: string;
    openingTime: string;
    closingTime: string;
    reason?: string;
    isClosed: boolean;
  }>> => {
    try {
      const response = await api.get(`/clubs/${clubId}/special-hours`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener horarios especiales del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Agregar horario especial al club
   */
  addSpecialHour: async (
    clubId: bigint,
    specialHour: {
      date: string;
      openingTime: string;
      closingTime: string;
      reason?: string;
      isClosed: boolean;
    }
  ): Promise<void> => {
    try {
      await api.post(`/clubs/${clubId}/special-hours`, specialHour);
    } catch (error) {
      console.error(`Error al agregar horario especial al club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Verificar disponibilidad del club en una fecha y hora específica
   */
  checkAvailability: async (
    clubId: bigint,
    dateTime: string,
    durationHours: number = 1
  ): Promise<{
    isAvailable: boolean;
    reason?: string;
    availableCourts?: Array<{ id: bigint; name: string; type: string; }>;
  }> => {
    try {
      const response = await api.get(`/clubs/${clubId}/availability`, {
        params: { dateTime, durationHours }
      });
      return response.data;
    } catch (error) {
      console.error(`Error al verificar disponibilidad del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Obtener configuración del club
   */
  getClubSettings: async (clubId: bigint): Promise<{
    bookingAdvanceDays: number;
    maxBookingDuration: number;
    minBookingDuration: number;
    cancellationPolicyHours: number;
    requireDeposit: boolean;
    depositPercentage?: number;
    autoConfirmReservations: boolean;
    notificationEmail: string;
    notificationPhone?: string;
  }> => {
    try {
      const response = await api.get(`/clubs/${clubId}/settings`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener configuración del club ${clubId}:`, error);
      throw error;
    }
  },

  /**
   * Actualizar configuración del club
   */
  updateClubSettings: async (
    clubId: bigint,
    settings: {
      bookingAdvanceDays?: number;
      maxBookingDuration?: number;
      minBookingDuration?: number;
      cancellationPolicyHours?: number;
      requireDeposit?: boolean;
      depositPercentage?: number;
      autoConfirmReservations?: boolean;
      notificationEmail?: string;
      notificationPhone?: string;
    }
  ): Promise<void> => {
    try {
      await api.put(`/clubs/${clubId}/settings`, settings);
    } catch (error) {
      console.error(`Error al actualizar configuración del club ${clubId}:`, error);
      throw error;
    }
  },
};