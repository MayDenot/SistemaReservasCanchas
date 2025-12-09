export interface ReservationRequest {
    userId: bigint;
    courtId: bigint;
    clubId: bigint;
    startTime: Date;
    endTime: Date;
    status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
    paymentStatus: 'PENDING' | 'CONFIRMED' | 'FAILED' | 'CANCELLED' | "REFUNDED";
    createdAt: Date;
}

export interface ReservationResponse {
    id: bigint;
    userId: bigint;
    courtId: bigint;
    clubId: bigint;
    startTime: Date;
    endTime: Date;
    status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
    paymentStatus: 'PENDING' | 'CONFIRMED' | 'FAILED' | 'CANCELLED' | "REFUNDED";
    createdAt: Date;
}