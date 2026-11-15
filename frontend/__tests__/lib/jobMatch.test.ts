import api from '@/lib/api';
import { matchJob } from '@/lib/jobMatchService';
import { t, setLocale } from '@/lib/i18n';

jest.mock('@/lib/api');

const mockedApi = api as jest.Mocked<typeof api>;

describe('A4 job match', () => {
    beforeEach(() => jest.resetAllMocks());

    it('matchJob posts jd text and extra keywords', async () => {
        mockedApi.post.mockResolvedValueOnce({ data: { score: 100, matchedWeight: 130, totalWeight: 130, matchedKeywords: [], missingKeywords: [] } });
        const result = await matchJob('app-1', '全栈工程师 杭州', ['React']);
        expect(mockedApi.post).toHaveBeenCalledWith('/applications/app-1/match', { jdText: '全栈工程师 杭州', extraKeywords: ['React'] });
        expect(result.score).toBe(100);
    });

    it('i18n defaults to zh-CN and can switch', () => {
        expect(t('matchTitle')).toBe('岗位匹配评分');
        setLocale('en');
        expect(t('matchTitle')).toBe('Job Match Score');
        setLocale('zh-CN');
    });
});
