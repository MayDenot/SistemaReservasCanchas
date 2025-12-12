// api/types/club.types.ts

export interface ClubResponse {
  id: bigint;
  name: string;
  address: string;
  phone?: string;
  openingTime: string; // Formato HH:mm:ss
  closingTime: string; // Formato HH:mm:ss
  adminId: bigint;
  // Campos calculados o adicionales que podrían venir del backend
  status?: "ACTIVE" | "INACTIVE" | "MAINTENANCE";
  isActive?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  // Para el endpoint /with-user
  admin?: {
    id: bigint;
    email: string;
    firstName?: string;
    lastName?: string;
  };
}

export interface ClubRequest {
  name: string;
  address: string;
  phone?: string;
  openingTime: string; // Formato HH:mm:ss
  closingTime: string; // Formato HH:mm:ss
  adminId: bigint;
}

export interface ClubWithUserResponse extends ClubResponse {
  admin: {
    id: bigint;
    email: string;
    firstName?: string;
    lastName?: string;
    phoneNumber?: string;
  };
}

export interface ClubOpeningHours {
  openingTime: string;
  closingTime: string;
  isOpen: boolean;
}

// Para el formulario de creación/edición
export interface ClubFormData {
  name: string;
  address: string;
  phone?: string;
  openingTime: string; // Formato HH:mm (para input type="time")
  closingTime: string; // Formato HH:mm (para input type="time")
  adminId: bigint;
}

// Para listado de clubes
export interface ClubListItem {
  id: bigint;
  name: string;
  address: string;
  phone?: string;
  openingHours: string; // Ejemplo: "08:00 - 22:00"
  isActive: boolean;
  courtCount?: number;
}