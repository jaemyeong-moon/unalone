import { ReceiverCondition } from '@/types';

interface ConditionBadgeProps {
  condition: ReceiverCondition;
}

const conditionStyles: Record<ReceiverCondition, string> = {
  GOOD: 'bg-emerald-100 text-emerald-700',
  FAIR: 'bg-yellow-100 text-yellow-700',
  POOR: 'bg-orange-100 text-orange-700',
  CRITICAL: 'bg-red-100 text-red-700',
};

const conditionLabels: Record<ReceiverCondition, string> = {
  GOOD: '양호',
  FAIR: '보통',
  POOR: '주의',
  CRITICAL: '위험',
};

export default function ConditionBadge({ condition }: ConditionBadgeProps) {
  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${conditionStyles[condition]}`}
    >
      {conditionLabels[condition]}
    </span>
  );
}
