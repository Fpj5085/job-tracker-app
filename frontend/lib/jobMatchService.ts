import api from './api';
import { JobMatchResponse } from '@/types';

export const matchJob = async (applicationId: string, jdText: string, extraKeywords?: string[]): Promise<JobMatchResponse> => {
    const response = await api.post<JobMatchResponse>(`/applications/${applicationId}/match`, { jdText, extraKeywords });
    return response.data;
};
