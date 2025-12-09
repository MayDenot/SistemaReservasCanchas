export interface CourtRequest {
    clubId: bigint;
    name: string;
    type: "OUTDOOR" | "INDOOR";
    pricePerHour: bigint;
    isActive: boolean;
}

export interface CourtResponse {
    id: bigint;
    clubId: bigint;
    name: string;
    type: "OUTDOOR" | "INDOOR";
    pricePerHour: bigint;
    isActive: boolean;
}