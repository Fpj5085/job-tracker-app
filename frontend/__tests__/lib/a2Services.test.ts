import api from '@/lib/api';
import { getInterviews, createInterview, getUpcomingInterviews } from '@/lib/interviewService';
import { getCommunications, createCommunication } from '@/lib/communicationService';
import { getOffers, createOffer, compareOffers } from '@/lib/offerService';

jest.mock('@/lib/api');

const mockedApi = api as jest.Mocked<typeof api>;

describe('A2 services', () => {
    beforeEach(() => jest.resetAllMocks());

    it('getInterviews calls the application interviews endpoint', async () => {
        mockedApi.get.mockResolvedValueOnce({ data: [] });
        await getInterviews('app-1');
        expect(mockedApi.get).toHaveBeenCalledWith('/applications/app-1/interviews');
    });

    it('createInterview posts the payload', async () => {
        const payload = { stage: 'TECHNICAL' as const, scheduledAt: '2026-11-15T10:00', mode: 'ONLINE' as const };
        mockedApi.post.mockResolvedValueOnce({ data: { id: 'iv-1' } });
        await createInterview('app-1', payload);
        expect(mockedApi.post).toHaveBeenCalledWith('/applications/app-1/interviews', payload);
    });

    it('getUpcomingInterviews passes the hours param', async () => {
        mockedApi.get.mockResolvedValueOnce({ data: [] });
        await getUpcomingInterviews(24);
        expect(mockedApi.get).toHaveBeenCalledWith('/interviews/upcoming', { params: { hours: 24 } });
    });

    it('getCommunications calls the communications endpoint', async () => {
        mockedApi.get.mockResolvedValueOnce({ data: [] });
        await getCommunications('app-1');
        expect(mockedApi.get).toHaveBeenCalledWith('/applications/app-1/communications');
    });

    it('createCommunication posts the payload', async () => {
        const payload = { channel: 'EMAIL' as const, direction: 'OUT' as const, occurredAt: '2026-11-14T09:00', summary: 'hello' };
        mockedApi.post.mockResolvedValueOnce({ data: { id: 'cm-1' } });
        await createCommunication('app-1', payload);
        expect(mockedApi.post).toHaveBeenCalledWith('/applications/app-1/communications', payload);
    });

    it('getOffers calls the offers endpoint', async () => {
        mockedApi.get.mockResolvedValueOnce({ data: [] });
        await getOffers('app-1');
        expect(mockedApi.get).toHaveBeenCalledWith('/applications/app-1/offers');
    });

    it('createOffer posts the payload', async () => {
        const payload = { baseSalary: 30000, currency: 'CNY' as const, location: 'Shanghai' };
        mockedApi.post.mockResolvedValueOnce({ data: { id: 'of-1' } });
        await createOffer('app-1', payload);
        expect(mockedApi.post).toHaveBeenCalledWith('/applications/app-1/offers', payload);
    });

    it('compareOffers calls the compare endpoint', async () => {
        mockedApi.get.mockResolvedValueOnce({ data: { baseCurrency: 'CNY', rates: {}, items: [] } });
        const result = await compareOffers();
        expect(mockedApi.get).toHaveBeenCalledWith('/offers/compare');
        expect(result.baseCurrency).toBe('CNY');
    });
});
