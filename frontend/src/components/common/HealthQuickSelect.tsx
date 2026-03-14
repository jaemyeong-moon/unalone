'use client';

const HEALTH_OPTIONS = [
  { value: 'SLEPT_WELL', label: '잘 잤어요', icon: '\u{1F319}' },
  { value: 'ATE_MEAL', label: '식사 했어요', icon: '\u{1F35A}' },
  { value: 'EXERCISED', label: '운동 했어요', icon: '\u{1F3C3}' },
  { value: 'TOOK_MEDICINE', label: '약 먹었어요', icon: '\u{1F48A}' },
  { value: 'WENT_OUT', label: '외출 했어요', icon: '\u{1F6B6}' },
  { value: 'IN_PAIN', label: '통증 있어요', icon: '\u{1F915}' },
  { value: 'LOW_ENERGY', label: '기운 없어요', icon: '\u{1F62E}\u{200D}\u{1F4A8}' },
];

interface HealthQuickSelectProps {
  selected: string[];
  onChange: (selected: string[]) => void;
}

export default function HealthQuickSelect({ selected, onChange }: HealthQuickSelectProps) {
  const toggleOption = (value: string) => {
    if (selected.includes(value)) {
      onChange(selected.filter((v) => v !== value));
    } else {
      onChange([...selected, value]);
    }
  };

  const isSelected = (value: string) => selected.includes(value);

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-2">오늘의 건강 상태</h2>
      <p className="text-sm text-gray-500 mb-4">해당하는 항목을 모두 선택해주세요</p>
      <div className="flex flex-wrap gap-2">
        {HEALTH_OPTIONS.map((option) => (
          <button
            key={option.value}
            type="button"
            onClick={() => toggleOption(option.value)}
            className={`inline-flex items-center gap-1.5 px-4 py-2.5 rounded-full text-sm font-medium transition-all duration-200
              ${isSelected(option.value)
                ? 'bg-emerald-100 text-emerald-700 border border-emerald-300'
                : 'bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200'
              }`}
            aria-pressed={isSelected(option.value)}
          >
            <span aria-hidden="true">{option.icon}</span>
            {option.label}
          </button>
        ))}
      </div>
    </div>
  );
}

export { HEALTH_OPTIONS };
