'use client';
import { useState } from 'react';
import { Communication, CommunicationChannel, CommunicationDirection } from '@/types';
import { createCommunication, deleteCommunication } from '@/lib/communicationService';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';

const CHANNELS: CommunicationChannel[] = ['EMAIL', 'PHONE', 'PORTAL', 'WECHAT', 'LINKEDIN'];
const DIRECTIONS: CommunicationDirection[] = ['IN', 'OUT'];
const CHANNEL_OPTIONS = CHANNELS.map((v) => ({ value: v, label: v }));
const DIRECTION_OPTIONS = DIRECTIONS.map((v) => ({ value: v, label: v === 'IN' ? '收到 (IN)' : '发出 (OUT)' }));

interface Props {
    applicationId: string;
    communications: Communication[];
    onChanged: () => void;
}

export default function CommunicationList({ applicationId, communications, onChanged }: Props) {
    const [channel, setChannel] = useState<CommunicationChannel>('EMAIL');
    const [direction, setDirection] = useState<CommunicationDirection>('OUT');
    const [occurredAt, setOccurredAt] = useState('');
    const [summary, setSummary] = useState('');
    const [nextAction, setNextAction] = useState('');
    const [nextActionAt, setNextActionAt] = useState('');
    const [busy, setBusy] = useState(false);

    // 到期置顶：next_action 已到期的排最前，其次按 nextActionAt 升序，其余按 occurredAt 降序
    const now = Date.now();
    const sorted = [...communications].sort((a, b) => {
        const dueA = a.nextActionAt ? new Date(a.nextActionAt).getTime() <= now : false;
        const dueB = b.nextActionAt ? new Date(b.nextActionAt).getTime() <= now : false;
        if (dueA !== dueB) return dueA ? -1 : 1;
        if (a.nextActionAt && b.nextActionAt) return a.nextActionAt.localeCompare(b.nextActionAt);
        return b.occurredAt.localeCompare(a.occurredAt);
    });

    const add = async () => {
        if (!occurredAt || !summary) return;
        setBusy(true);
        try {
            await createCommunication(applicationId, {
                channel, direction, occurredAt, summary,
                nextAction: nextAction || undefined,
                nextActionAt: nextActionAt || undefined,
            });
            setSummary('');
            setNextAction('');
            setNextActionAt('');
            onChanged();
        } finally {
            setBusy(false);
        }
    };

    const remove = async (id: string) => {
        await deleteCommunication(applicationId, id);
        onChanged();
    };

    return (
        <div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2 mb-2">
                <Select aria-label="Channel" options={CHANNEL_OPTIONS} value={channel} onChange={(e) => setChannel(e.target.value as CommunicationChannel)} />
                <Select aria-label="Direction" options={DIRECTION_OPTIONS} value={direction} onChange={(e) => setDirection(e.target.value as CommunicationDirection)} />
                <Input aria-label="Occurred at" type="datetime-local" value={occurredAt} onChange={(e) => setOccurredAt(e.target.value)} />
                <Input aria-label="Next action at" type="datetime-local" value={nextActionAt} onChange={(e) => setNextActionAt(e.target.value)} />
            </div>
            <div className="flex gap-2 mb-6">
                <Input aria-label="Summary" value={summary} onChange={(e) => setSummary(e.target.value)} placeholder="沟通摘要" />
                <Input aria-label="Next action" value={nextAction} onChange={(e) => setNextAction(e.target.value)} placeholder="下一步动作（可选）" />
                <Button onClick={add} disabled={busy || !occurredAt || !summary}>添加记录</Button>
            </div>
            <ul className="space-y-3">
                {sorted.map((c) => {
                    const due = c.nextActionAt && new Date(c.nextActionAt).getTime() <= now;
                    return (
                        <li key={c.id} className={`rounded-lg border p-3 ${due ? 'border-(--primary) bg-(--primary)/5' : 'border-(--border) bg-(--card)'}`}>
                            <div className="flex items-center justify-between gap-2">
                                <span className="font-medium">{c.channel} · {c.direction}</span>
                                <button aria-label="Delete communication" className="text-(--muted-foreground) hover:text-(--foreground)" onClick={() => remove(c.id)}>✕</button>
                            </div>
                            <p className="text-sm">{c.summary}</p>
                            <p className="text-xs text-(--muted-foreground)">{new Date(c.occurredAt).toLocaleString()}</p>
                            {c.nextAction && (
                                <p className={`mt-1 text-xs ${due ? 'font-semibold text-(--primary)' : 'text-(--muted-foreground)'}`}>
                                    下一步：{c.nextAction} {c.nextActionAt ? `· ${new Date(c.nextActionAt).toLocaleString()}` : ''}{due ? '（已到期）' : ''}
                                </p>
                            )}
                        </li>
                    );
                })}
                {sorted.length === 0 && <li className="text-sm text-(--muted-foreground)">暂无沟通记录</li>}
            </ul>
        </div>
    );
}
