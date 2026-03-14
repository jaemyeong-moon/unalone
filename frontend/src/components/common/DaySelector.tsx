'use client';

import { DayOfWeek } from '@/types';

const DAYS: { value: DayOfWeek; short: string; label: string }[] = [
  { value: 'MON', short: '월', label: '월요일' },
  { value: 'TUE', short: '화', label: '화요일' },
  { value: 'WED', short: '수', label: '수요일' },
  { value: 'THU', short: '목', label: '목요일' },
  { value: 'FRI', short: '금', label: '금요일' },
  { value: 'SAT', short: '토', label: '토요일' },
  { value: 'SUN', short: '일', label: '일요일' },
];

interface DaySelectorProps {
  selected: DayOfWeek[];
  onChange: (days: DayOfWeek[]) => void;
}

export default function DaySelector({ selected, onChange }: DaySelectorProps) {
  const toggleDay = (day: DayOfWeek) => {
    if (selected.includes(day)) {
      onChange(selected.filter((d) => d !== day));
    } else {
      onChange([...selected, day]);
    }
  };

  return (
    <div className="flex gap-2">
      {DAYS.map((day) => (
        <button
          key={day.value}
          type="button"
          onClick={() => toggleDay(day.value)}
          className={`w-10 h-10 rounded-full text-sm font-medium transition-colors
            ${selected.includes(day.value)
              ? 'bg-emerald-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          aria-pressed={selected.includes(day.value)}
          aria-label={day.label}
        >
          {day.short}
        </button>
      ))}
    </div>
  );
}
