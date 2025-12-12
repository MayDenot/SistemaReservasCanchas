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
  }
};