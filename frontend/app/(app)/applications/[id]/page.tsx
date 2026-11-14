'use client';
import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import useAuthGuard from '@/hooks/useAuthGuard';
import Spinner from '@/components/ui/Spinner';
import { StatusBadge } from '@/components/ui/Badge';
import { getApplication } from '@/lib/applicationService';
import { getInterviews } from '@/lib/interviewService';
import { getCommunications } from '@/lib/communicationService';
import { getOffers } from '@/lib/offerService';
import { Application, Communication, Interview, Offer } from '@/types';
import InterviewTimeline from '@/components/detail/InterviewTimeline';
import CommunicationList from '@/components/detail/CommunicationList';
import OfferPanel from '@/components/detail/OfferPanel';

type Tab = 'interviews' | 'communications' | 'offers';

export default function ApplicationDetailPage() {
    const ready = useAuthGuard();
    const params = useParams<{ id: string }>();
    const id = params?.id;
    const [application, setApplication] = useState<Application | null>(null);
    const [interviews, setInterviews] = useState<Interview[]>([]);
    const [communications, setCommunications] = useState<Communication[]>([]);
    const [offers, setOffers] = useState<Offer[]>([]);
    const [tab, setTab] = useState<Tab>('interviews');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const fetchAll = useCallback(async () => {
        if (!id) return;
        try {
            const [app, iv, cm, of] = await Promise.all([
                getApplication(id),
                getInterviews(id).catch(() => []),
                getCommunications(id).catch(() => []),
                getOffers(id).catch(() => []),
            ]);
            setApplication(app);
            setInterviews(iv);
            setCommunications(cm);
            setOffers(of);
            setError('');
        } catch {
            setError('Failed to load application detail');
        }
    }, [id]);

    useEffect(() => {
        if (ready && id) fetchAll().finally(() => setLoading(false));
    }, [ready, id, fetchAll]);

    if (!ready || loading) {
        return <div className="flex items-center justify-center py-16"><Spinner size="lg" /></div>;
    }

    if (error || !application) {
        return (
            <div className="py-16 text-center text-(--muted-foreground)">
                <p>{error || 'Application not found'}</p>
                <Link href="/applications" className="mt-2 inline-block underline">返回列表</Link>
            </div>
        );
    }

    const tabs: { key: Tab; label: string; count: number }[] = [
        { key: 'interviews', label: '面试时间线', count: interviews.length },
        { key: 'communications', label: '沟通记录', count: communications.length },
        { key: 'offers', label: 'Offer', count: offers.length },
    ];

    return (
        <div className="space-y-6">
            <div>
                <Link href="/applications" className="text-sm text-(--muted-foreground) hover:underline">← 返回列表</Link>
                <div className="mt-2 flex items-center gap-3">
                    <h1 className="text-2xl font-semibold">{application.companyName}</h1>
                    <StatusBadge status={application.status} />
                </div>
                <p className="text-(--muted-foreground)">{application.jobRole} · {application.location}</p>
            </div>

            <div className="flex gap-2 border-b border-(--border)">
                {tabs.map((t) => (
                    <button key={t.key} onClick={() => setTab(t.key)}
                        className={`px-4 py-2 text-sm font-medium ${tab === t.key ? 'border-b-2 border-(--primary) text-(--foreground)' : 'text-(--muted-foreground)'}`}>
                        {t.label} ({t.count})
                    </button>
                ))}
            </div>

            {tab === 'interviews' && <InterviewTimeline applicationId={id} interviews={interviews} onChanged={fetchAll} />}
            {tab === 'communications' && <CommunicationList applicationId={id} communications={communications} onChanged={fetchAll} />}
            {tab === 'offers' && <OfferPanel applicationId={id} offers={offers} onChanged={fetchAll} />}
        </div>
    );
}
