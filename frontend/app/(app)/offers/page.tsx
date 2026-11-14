'use client';
import { useCallback, useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import useAuthGuard from '@/hooks/useAuthGuard';
import Spinner from '@/components/ui/Spinner';
import { compareOffers } from '@/lib/offerService';
import { OfferComparisonResponse } from '@/types';

interface Weights {
    salary: number;
    growth: number;
    pto: number;
}

export default function OffersPage() {
    const ready = useAuthGuard();
    const [data, setData] = useState<OfferComparisonResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [weights, setWeights] = useState<Weights>({ salary: 60, growth: 25, pto: 15 });

    const fetchOffers = useCallback(async () => {
        try {
            setData(await compareOffers());
        } catch {
            setError('Failed to load offers');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (ready) fetchOffers();
    }, [ready, fetchOffers]);

    const scored = useMemo(() => {
        if (!data || data.items.length === 0) return [];
        const maxPkg = Math.max(...data.items.map((i) => i.normalizedAnnualPackage), 1);
        const maxGrowth = Math.max(...data.items.map((i) => (i.offer.bonus || 0) + (i.offer.equity || 0)), 1);
        const maxPto = Math.max(...data.items.map((i) => i.offer.ptoDays || 0), 1);
        const total = weights.salary + weights.growth + weights.pto || 1;
        return data.items
            .map((item) => ({
                ...item,
                score:
                    (weights.salary / total) * (item.normalizedAnnualPackage / maxPkg) +
                    (weights.growth / total) * (((item.offer.bonus || 0) + (item.offer.equity || 0)) / maxGrowth) +
                    (weights.pto / total) * ((item.offer.ptoDays || 0) / maxPto),
            }))
            .sort((a, b) => b.score - a.score);
    }, [data, weights]);

    if (!ready || loading) {
        return <div className="flex items-center justify-center py-16"><Spinner size="lg" /></div>;
    }

    if (error) {
        return <div className="py-16 text-center text-(--muted-foreground)">{error}</div>;
    }

    const top = scored.slice(0, 2);
    const symbol = data?.baseCurrency === 'CNY' ? '¥' : data?.baseCurrency === 'USD' ? '$' : '€';

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold">Offer 薪酬比较</h1>
                <p className="text-sm text-(--muted-foreground)">归一化年包 = (月薪 × 12 + 年奖金 + 年度股权) × 汇率，基准 {data?.baseCurrency}，汇率表内置可配置。</p>
            </div>

            <div className="rounded-xl border border-(--border) bg-(--card) p-4">
                <p className="mb-2 text-sm font-medium">评分权重（可调）</p>
                <div className="grid gap-4 md:grid-cols-3">
                    <label className="text-sm">薪资 {weights.salary}%
                        <input aria-label="Salary weight" type="range" min="0" max="100" value={weights.salary}
                            onChange={(e) => setWeights((w) => ({ ...w, salary: Number(e.target.value) }))} className="w-full" />
                    </label>
                    <label className="text-sm">长期激励 {weights.growth}%
                        <input aria-label="Growth weight" type="range" min="0" max="100" value={weights.growth}
                            onChange={(e) => setWeights((w) => ({ ...w, growth: Number(e.target.value) }))} className="w-full" />
                    </label>
                    <label className="text-sm">假期 {weights.pto}%
                        <input aria-label="PTO weight" type="range" min="0" max="100" value={weights.pto}
                            onChange={(e) => setWeights((w) => ({ ...w, pto: Number(e.target.value) }))} className="w-full" />
                    </label>
                </div>
            </div>

            {scored.length === 0 ? (
                <div className="py-16 text-center text-(--muted-foreground)">
                    <p>暂无 Offer。请到申请详情页录入。</p>
                    <Link href="/applications" className="mt-2 inline-block underline">前往申请列表</Link>
                </div>
            ) : (
                <>
                    <div className="grid gap-4 md:grid-cols-2">
                        {top.map((item, idx) => (
                            <div key={item.offer.id}
                                className={`rounded-xl border border-(--border) bg-(--card) p-5 shadow-sm ${idx === 0 ? 'ring-2 ring-(--primary)' : ''}`}>
                                <div className="flex items-center justify-between">
                                    <h2 className="font-semibold">{idx === 0 ? '🥇 ' : '🥈 '}{item.offer.companyName} · {item.offer.jobRole}</h2>
                                    <span className="rounded-full bg-(--primary)/10 px-2 py-0.5 text-xs font-medium text-(--primary)">
                                        评分 {(item.score * 100).toFixed(1)}
                                    </span>
                                </div>
                                <p className="mt-2 text-lg font-semibold">
                                    {symbol}{item.normalizedAnnualPackage.toLocaleString()} <span className="text-xs font-normal text-(--muted-foreground)">/ 年（归一化）</span>
                                </p>
                                <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
                                    <dt className="text-(--muted-foreground)">月薪</dt><dd>{item.offer.baseSalary} {item.offer.currency}</dd>
                                    <dt className="text-(--muted-foreground)">奖金</dt><dd>{item.offer.bonus === null || item.offer.bonus === undefined ? '-' : String(item.offer.bonus)}</dd>
                                    <dt className="text-(--muted-foreground)">股权</dt><dd>{item.offer.equity === null || item.offer.equity === undefined ? '-' : String(item.offer.equity)}</dd>
                                    <dt className="text-(--muted-foreground)">远程</dt><dd>{item.offer.remotePolicy === null || item.offer.remotePolicy === undefined ? '-' : String(item.offer.remotePolicy)}</dd>
                                    <dt className="text-(--muted-foreground)">保险</dt><dd>{item.offer.insurance === null || item.offer.insurance === undefined ? '-' : String(item.offer.insurance)}</dd>
                                    <dt className="text-(--muted-foreground)">年假</dt><dd>{item.offer.ptoDays === null || item.offer.ptoDays === undefined ? '-' : String(item.offer.ptoDays)}</dd>
                                    <dt className="text-(--muted-foreground)">入职</dt><dd>{item.offer.startDate === null || item.offer.startDate === undefined ? '-' : String(item.offer.startDate)}</dd>
                                    <dt className="text-(--muted-foreground)">地点</dt><dd>{item.offer.location}</dd>
                                </dl>
                            </div>
                        ))}
                    </div>

                    <div className="rounded-xl border border-(--border) bg-(--card) p-4">
                        <p className="mb-2 text-sm font-medium">全部 Offer（按归一化年包排序）</p>
                        <ul className="space-y-2">
                            {scored.map((item) => (
                                <li key={item.offer.id} className="flex items-center justify-between text-sm">
                                    <span>{item.offer.companyName} · {item.offer.jobRole} · {item.offer.location}</span>
                                    <span className="font-medium">{symbol}{item.normalizedAnnualPackage.toLocaleString()}</span>
                                </li>
                            ))}
                        </ul>
                    </div>
                </>
            )}
        </div>
    );
}
