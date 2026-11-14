'use client';
import { useEffect, useState } from 'react';
import { getUpcomingInterviews } from '@/lib/interviewService';
import { Interview } from '@/types';

const POLL_MS = 5 * 60 * 1000;

export default function UpcomingInterviewBanner() {
    const [upcoming, setUpcoming] = useState<Interview[]>([]);

    useEffect(() => {
        let cancelled = false;
        const poll = async () => {
            try {
                const list = await getUpcomingInterviews(24);
                if (!cancelled) setUpcoming(list);
            } catch {
                // best-effort browser-side polling reminder
            }
        };
        poll();
        const timer = setInterval(poll, POLL_MS);
        return () => {
            cancelled = true;
            clearInterval(timer);
        };
    }, []);

    if (upcoming.length === 0) return null;
    return (
        <div role="alert" className="mb-4 rounded-lg border border-(--primary) bg-(--primary)/5 p-3 text-sm">
            <span className="font-semibold text-(--primary)">⏰ 24 小时内有 {upcoming.length} 场面试：</span>
            {upcoming.map((it) => (
                <span key={it.id} className="ms-2">{it.stage} · {new Date(it.scheduledAt).toLocaleString()}</span>
            ))}
        </div>
    );
}
