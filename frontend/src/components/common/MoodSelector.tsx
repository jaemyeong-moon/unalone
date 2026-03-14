'use client';

const MOOD_LEVELS = [
  { value: 1, label: '매우 나쁨', emoji: '\u{1F622}', selectedBg: 'bg-red-100', selectedBorder: 'border-red-400' },
  { value: 2, label: '나쁨', emoji: '\u{1F61F}', selectedBg: 'bg-orange-100', selectedBorder: 'border-orange-400' },
  { value: 3, label: '보통', emoji: '\u{1F610}', selectedBg: 'bg-yellow-100', selectedBorder: 'border-yellow-400' },
  { value: 4, label: '좋음', emoji: '\u{1F642}', selectedBg: 'bg-emerald-100', selectedBorder: 'border-emerald-400' },
  { value: 5, label: '매우 좋음', emoji: '\u{1F604}', selectedBg: 'bg-blue-100', selectedBorder: 'border-blue-400' },
];

interface MoodSelectorProps {
  value: number | null;
  onChange: (mood: number) => void;
  title?: string;
  subtitle?: string;
}

export default function MoodSelector({
  value,
  onChange,
  title = '오늘 기분은 어떠세요?',
  subtitle = '가장 가까운 표정을 선택해주세요',
}: MoodSelectorProps) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-2">{title}</h2>
      <p className="text-sm text-gray-500 mb-4">{subtitle}</p>
      <div className="flex justify-between gap-2 sm:gap-4">
        {MOOD_LEVELS.map((mood) => (
          <button
            key={mood.value}
            type="button"
            onClick={() => onChange(mood.value)}
            className={`flex-1 flex flex-col items-center gap-1.5 py-3 px-2 rounded-xl border-2 transition-all duration-200
              ${value === mood.value
                ? `${mood.selectedBg} ${mood.selectedBorder}`
                : 'border-transparent hover:bg-gray-50'
              }`}
            aria-label={mood.label}
            aria-pressed={value === mood.value}
          >
            <span className="text-3xl sm:text-4xl" role="img" aria-hidden="true">{mood.emoji}</span>
            <span className="text-xs text-gray-600 font-medium">{mood.label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}

export { MOOD_LEVELS };
