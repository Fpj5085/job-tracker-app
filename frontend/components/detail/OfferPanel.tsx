'use client';
import { useState } from 'react';
import { Currency, Offer } from '@/types';
import { createOffer, deleteOffer, OfferInput } from '@/lib/offerService';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';

const CURRENCIES: Currency[] = ['CNY', 'USD', 'EUR'];
const CURRENCY_OPTIONS = CURRENCIES.map((v) => ({ value: v, label: v }));

interface Props {
    applicationId: string;
    offers: Offer[];
    onChanged: () => void;
}

export default function OfferPanel({ applicationId, offers, onChanged }: Props) {
    const [baseSalary, setBaseSalary] = useState('');
    const [bonus, setBonus] = useState('');
    const [equity, setEquity] = useState('');
    const [currency, setCurrency] = useState<Currency>('CNY');
    const [location, setLocation] = useState('');
    const [remotePolicy, setRemotePolicy] = useState('');
    const [ptoDays, setPtoDays] = useState('');
    const [busy, setBusy] = useState(false);

    const add = async () => {
        if (!baseSalary || !location) return;
        setBusy(true);
        try {
            const payload: OfferInput = { baseSalary: Number(baseSalary), currency, location };
            if (bonus) payload.bonus = Number(bonus);
            if (equity) payload.equity = Number(equity);
            if (remotePolicy) payload.remotePolicy = remotePolicy;
            if (ptoDays) payload.ptoDays = Number(ptoDays);
            await createOffer(applicationId, payload);
            setBaseSalary(''); setBonus(''); setEquity(''); setLocation(''); setRemotePolicy(''); setPtoDays('');
            onChanged();
        } finally {
            setBusy(false);
        }
    };

    const remove = async (id: string) => {
        await deleteOffer(applicationId, id);
        onChanged();
    };

    return (
        <div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2 mb-2">
                <Input aria-label="Base salary" type="number" min="0" value={baseSalary} onChange={(e) => setBaseSalary(e.target.value)} placeholder="月薪" />
                <Input aria-label="Bonus" type="number" min="0" value={bonus} onChange={(e) => setBonus(e.target.value)} placeholder="年奖金" />
                <Input aria-label="Equity" type="number" min="0" value={equity} onChange={(e) => setEquity(e.target.value)} placeholder="股权（年度）" />
                <Select aria-label="Currency" options={CURRENCY_OPTIONS} value={currency} onChange={(e) => setCurrency(e.target.value as Currency)} />
            </div>
            <div className="flex gap-2 mb-6">
                <Input aria-label="Location" value={location} onChange={(e) => setLocation(e.target.value)} placeholder="工作地点" />
                <Input aria-label="Remote policy" value={remotePolicy} onChange={(e) => setRemotePolicy(e.target.value)} placeholder="远程政策" />
                <Input aria-label="PTO days" type="number" min="0" value={ptoDays} onChange={(e) => setPtoDays(e.target.value)} placeholder="年假天数" />
                <Button onClick={add} disabled={busy || !baseSalary || !location}>录入 Offer</Button>
            </div>
            <ul className="space-y-2">
                {offers.map((o) => (
                    <li key={o.id} className="flex items-center justify-between rounded-lg border border-(--border) bg-(--card) p-3 text-sm">
                        <span>{o.baseSalary} {o.currency}/月 · 奖金 {o.bonus ?? 0} · 股权 {o.equity ?? 0} · {o.location}</span>
                        <button aria-label="Delete offer" className="text-(--muted-foreground) hover:text-(--foreground)" onClick={() => remove(o.id)}>✕</button>
                    </li>
                ))}
                {offers.length === 0 && <li className="text-sm text-(--muted-foreground)">暂无 Offer</li>}
            </ul>
        </div>
    );
}
