'use client';
import { useCallback, useEffect, useState } from 'react';
import { getApplications } from '@/lib/applicationService';
import { matchJob } from '@/lib/jobMatchService';
import { t } from '@/lib/i18n';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { Application, JobMatchResponse } from '@/types';

export default function JobMatchCard() {
    const [applications, setApplications] = useState<Application[]>([]);
    const [selected, setSelected] = useState('');
    const [jdText, setJdText] = useState('');
    const [extra, setExtra] = useState('');
    const [result, setResult] = useState<JobMatchResponse | null>(null);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        getApplications(0, 50).then((r) => setApplications(r.content)).catch(() => {});
    }, []);

    const compute = useCallback(async () => {
        if (!selected || !jdText.trim()) return;
        setBusy(true); setError('');
        try {
            const extras = extra.split(/[,，]/).map((s) => s.trim()).filter(Boolean);
            setResult(await matchJob(selected, jdText, extras.length ? extras : undefined));
        } catch {
            setError(t('compute') + ' failed');
        } finally {
            setBusy(false);
        }
    }, [selected, jdText, extra]);

    return (
        <section aria-label={t('matchTitle')} className="rounded-xl border border-(--border) bg-(--card) p-5 shadow-sm space-y-3">
            <h2 className="font-semibold">{t('matchTitle')}</h2>
            <p className="text-xs text-(--muted-foreground)">{t('matchHint')}</p>
            <select aria-label={t('selectApplication')} value={selected}
                onChange={(e) => setSelected(e.target.value)}
                className="w-full rounded-md border border-(--border) bg-(--background) px-3 py-2 text-sm">
                <option value="">--</option>
                {applications.map((a) => <option key={a.id} value={a.id}>{a.companyName} · {a.jobRole}</option>)}
            </select>
            <textarea aria-label={t('jdPlaceholder')} value={jdText}
                onChange={(e) => setJdText(e.target.value)} rows={5}
                placeholder={t('jdPlaceholder')}
                className="w-full rounded-md border border-(--border) bg-(--background) px-3 py-2 text-sm" />
            <Input aria-label={t('extraKeywords')} value={extra}
                onChange={(e) => setExtra(e.target.value)} placeholder={t('extraKeywords')} />
            <Button onClick={compute} disabled={busy || !selected || !jdText.trim()}>{t('compute')}</Button>
            {error && <p className="text-sm text-red-500">{error}</p>}
            {result && (
                <div className="rounded-lg border border-(--border) p-3 text-sm">
                    <p className="text-2xl font-bold">{t('score')}: {result.score}<span className="text-sm font-normal text-(--muted-foreground)"> / 100</span></p>
                    <p className="mt-1 text-xs text-(--muted-foreground)">{t('weightExplain')} · {result.matchedWeight}/{result.totalWeight}</p>
                    <p className="mt-2">{t('matchedKeywords')}: {result.matchedKeywords.join('、') || '-'}</p>
                    <p className="text-(--muted-foreground)">{t('missingKeywords')}: {result.missingKeywords.join('、') || '-'}</p>
                </div>
            )}
        </section>
    );
}
