export interface User {
    id: bigint;
    email: string;
    name: string;
    phone?: string;
    role: 'USER' | 'ADMIN' | 'CLUB_OWNER';
    createdAt: string;
    updatedAt: string;
}

export interface UserRequest {
    email: string;
    password: string;
    userRole: 'USER' | 'ADMIN' | 'CLUB_OWNER';
    name: string;
    phone: string;
    createdAt: string;
}

export interface UserResponse {
    id: bigint;
    email: string;
    userRole: 'USER' | 'ADMIN' | 'CLUB_OWNER';
    name: string;
    phone: string;
    createdAt: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    user: User;
}

export interface ProfileRequest {
    name: string;
    phone: string;
    currentPassword: string;
    newPassword: string;
}

export interface ProfileResponse {
    id: bigint;
    email: string;
    name: string;
    phone: string;
    createdAt: Date;
    isActive: boolean;
}