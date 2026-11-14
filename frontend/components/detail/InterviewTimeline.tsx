'use client';
import { useState } from 'react';
import { Interview, InterviewStage, InterviewMode, InterviewResult } from '@/types';
import { createInterview, deleteInterview, updateInterview } from '@/lib/interviewService';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';

const STAGES: InterviewStage[] = ['HR_SCREEN', 'TECHNICAL', 'SYSTEM_DESIGN', 'FINAL', 'CROSS'];
const MODES: InterviewMode[] = ['ONLINE', 'ONSITE', 'PHONE'];
const RESULTS: InterviewResult[] = ['PENDING', 'PASS', 'FAIL', 'NO_SHOW'];
const STAGE_OPTIONS = STAGES.map((v) => ({ value: v, label: v }));
const MODE_OPTIONS = MODES.map((v) => ({ value: v, label: v }));
const RESULT_OPTIONS = RESULTS.map((v) => ({ value: v, label: v }));

interface Props {
    applicationId: string;
    interviews: Interview[];
    onChanged: () => void;
}

export default function InterviewTimeline({ applicationId, interviews, onChanged }: Props) {
    const [stage, setStage] = useState<InterviewStage>('HR_SCREEN');
    const [mode, setMode] = useState<InterviewMode>('ONLINE');
    const [scheduledAt, setScheduledAt] = useState('');
    const [timezone, setTimezone] = useState('Asia/Shanghai');
    const [notes, setNotes] = useState('');
    const [busy, setBusy] = useState(false);

    const sorted = [...interviews].sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt));

    const add = async () => {
        if (!scheduledAt) return;
        setBusy(true);
        try {
            await createInterview(applicationId, { stage, mode, scheduledAt, timezone, interviewerNotes: notes || undefined });
            setScheduledAt('');
            setNotes('');
            onChanged();
        } finally {
            setBusy(false);
        }
    };

    const setResult = async (id: string, result: InterviewResult) => {
        await updateInterview(applicationId, id, { result });
        onChanged();
    };

    const remove = async (id: string) => {
        await deleteInterview(applicationId, id);
        onChanged();
    };

    return (
        <div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2 mb-4">
                <Select aria-label="Stage" options={STAGE_OPTIONS} value={stage} onChange={(e) => setStage(e.target.value as InterviewStage)} />
                <Select aria-label="Mode" options={MODE_OPTIONS} value={mode} onChange={(e) => setMode(e.target.value as InterviewMode)} />
                <Input aria-label="Scheduled at" type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} />
                <Input aria-label="Timezone" value={timezone} onChange={(e) => setTimezone(e.target.value)} placeholder="Asia/Shanghai" />
            </div>
            <div className="flex gap-2 mb-6">
                <Input aria-label="Notes" value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="面试备注（可选）" />
                <Button onClick={add} disabled={busy || !scheduledAt}>添加面试</Button>
            </div>
            <ol className="relative border-s border-(--border) ms-2">
                {sorted.map((it) => (
                    <li key={it.id} className="ms-6 mb-6">
                        <span className="absolute -start-1.5 flex h-3 w-3 rounded-full bg-(--primary)"></span>
                        <div className="rounded-lg border border-(--border) bg-(--card) p-3">
                            <div className="flex items-center justify-between gap-2">
                                <span className="font-medium">{it.stage} · {it.mode}</span>
                                <button aria-label="Delete interview" className="text-(--muted-foreground) hover:text-(--foreground)" onClick={() => remove(it.id)}>✕</button>
                            </div>
                            <p className="text-sm text-(--muted-foreground)">{new Date(it.scheduledAt).toLocaleString()} {it.timezone ? `(${it.timezone})` : ''}</p>
                            {it.interviewerNotes && <p className="mt-1 text-sm">{it.interviewerNotes}</p>}
                            {it.feedback && <p className="mt-1 text-sm text-(--muted-foreground)">{it.feedback}</p>}
                            <div className="mt-2 flex gap-1">
                                {RESULTS.map((r) => (
                                    <button key={r} onClick={() => setResult(it.id, r)}
                                        className={`px-2 py-0.5 text-xs rounded-full border ${it.result === r ? 'bg-(--primary) text-(--primary-foreground)' : 'text-(--muted-foreground)'}`}>
                                        {r}
                                    </button>
                                ))}
                            </div>
                        </div>
                    </li>
                ))}
                {sorted.length === 0 && <li className="ms-6 text-sm text-(--muted-foreground)">暂无面试记录</li>}
            </ol>
        </div>
    );
}
