import api from './api';
import { Interview, InterviewResult, InterviewStage, InterviewMode } from '@/types';

export interface InterviewInput {
    stage: InterviewStage;
    scheduledAt: string;
    timezone?: string;
    mode: InterviewMode;
    interviewerNotes?: string;
    feedback?: string;
    result?: InterviewResult;
}

export const getInterviews = async (applicationId: string): Promise<Interview[]> => {
    const response = await api.get<Interview[]>(`/applications/${applicationId}/interviews`);
    return response.data;
};

export const createInterview = async (applicationId: string, data: InterviewInput): Promise<Interview> => {
    const response = await api.post<Interview>(`/applications/${applicationId}/interviews`, data);
    return response.data;
};

export const updateInterview = async (applicationId: string, interviewId: string, data: Partial<InterviewInput>): Promise<Interview> => {
    const response = await api.patch<Interview>(`/applications/${applicationId}/interviews/${interviewId}`, data);
    return response.data;
};

export const deleteInterview = async (applicationId: string, interviewId: string): Promise<void> => {
    await api.delete(`/applications/${applicationId}/interviews/${interviewId}`);
};

export const getUpcomingInterviews = async (hours = 24): Promise<Interview[]> => {
    const response = await api.get<Interview[]>(`/interviews/upcoming`, { params: { hours } });
    return response.data;
};
