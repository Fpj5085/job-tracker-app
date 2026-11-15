// A4: bilingual dictionary for new UI copy. Default locale zh-CN.
export type Locale = 'zh-CN' | 'en';

const DICT = {
    'zh-CN': {
        matchTitle: '岗位匹配评分',
        matchHint: '粘贴 JD 文本，评估与所选申请的匹配度（纯本地算法：职责/地点/公司/类型关键词加权）',
        selectApplication: '选择申请',
        jdPlaceholder: '粘贴岗位描述（中文/英文均可）',
        extraKeywords: '附加关键词（可选，逗号分隔）',
        compute: '计算匹配分',
        score: '匹配分',
        matchedKeywords: '命中关键词',
        missingKeywords: '未命中',
        weightExplain: '权重：职位 40 / 地点 25 / 公司 15 / 类型 10 / 附加关键词 每个 10（上限 40）',
    },
    en: {
        matchTitle: 'Job Match Score',
        matchHint: 'Paste a JD text to score it against an application (local keyword weighting)',
        selectApplication: 'Select application',
        jdPlaceholder: 'Paste job description (Chinese or English)',
        extraKeywords: 'Extra keywords (optional, comma separated)',
        compute: 'Compute match score',
        score: 'Score',
        matchedKeywords: 'Matched keywords',
        missingKeywords: 'Missing',
        weightExplain: 'Weights: role 40 / location 25 / company 15 / type 10 / extra keyword 10 each (cap 40)',
    },
} as const;

export type Dict = typeof DICT['zh-CN'];

let locale: Locale = 'zh-CN';

export function setLocale(next: Locale) {
    locale = next;
}

export function t(key: keyof Dict): string {
    return DICT[locale][key];
}
