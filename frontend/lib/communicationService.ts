import api from './api';
import { Communication, CommunicationChannel, CommunicationDirection } from '@/types';

export interface CommunicationInput {
    channel: CommunicationChannel;
    direction: CommunicationDirection;
    occurredAt: string;
    summary: string;
    nextAction?: string;
    nextActionAt?: string;
}

export const getCommunications = async (applicationId: string): Promise<Communication[]> => {
    const response = await api.get<Communication[]>(`/applications/${applicationId}/communications`);
    return response.data;
};

export const createCommunication = async (applicationId: string, data: CommunicationInput): Promise<Communication> => {
    const response = await api.post<Communication>(`/applications/${applicationId}/communications`, data);
    return response.data;
};

export const updateCommunication = async (applicationId: string, communicationId: string, data: Partial<CommunicationInput>): Promise<Communication> => {
    const response = await api.patch<Communication>(`/applications/${applicationId}/communications/${communicationId}`, data);
    return response.data;
};

export const deleteCommunication = async (applicationId: string, communicationId: string): Promise<void> => {
    await api.delete(`/applications/${applicationId}/communications/${communicationId}`);
};
