import type {
    LoginRequest,
    LoginResponse,
    UserRequest,
    UserResponse
} from "../types/user.types";
import api from "../axiosConfig.ts";

export const authService = {
    login: async (req: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post("/auth/login", req);
        return response.data;
    },

    register: async (req: UserRequest): Promise<UserResponse> => {
        const response = await api.post("/auth/register", req);
        return response.data;
    },

    validate: async (token: string): Promise<boolean> => {
        try {
            const response = await api.post("/auth/validate", { token });
            return response.data.isValid || response.data;
        } catch (error) {
            console.warn('Validate endpoint might not exist');
            throw error;
        }
    }
};