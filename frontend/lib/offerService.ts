import api from './api';
import { Offer, Currency, OfferComparisonResponse } from '@/types';

export interface OfferInput {
    baseSalary: number;
    bonus?: number;
    equity?: number;
    currency: Currency;
    location: string;
    remotePolicy?: string;
    insurance?: string;
    ptoDays?: number;
    startDate?: string;
    notes?: string;
}

export const getOffers = async (applicationId: string): Promise<Offer[]> => {
    const response = await api.get<Offer[]>(`/applications/${applicationId}/offers`);
    return response.data;
};

export const createOffer = async (applicationId: string, data: OfferInput): Promise<Offer> => {
    const response = await api.post<Offer>(`/applications/${applicationId}/offers`, data);
    return response.data;
};

export const updateOffer = async (applicationId: string, offerId: string, data: Partial<OfferInput>): Promise<Offer> => {
    const response = await api.patch<Offer>(`/applications/${applicationId}/offers/${offerId}`, data);
    return response.data;
};

export const deleteOffer = async (applicationId: string, offerId: string): Promise<void> => {
    await api.delete(`/applications/${applicationId}/offers/${offerId}`);
};

export const compareOffers = async (): Promise<OfferComparisonResponse> => {
    const response = await api.get<OfferComparisonResponse>('/offers/compare');
    return response.data;
};
