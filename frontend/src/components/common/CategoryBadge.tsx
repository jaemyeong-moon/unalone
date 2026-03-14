import { CommunityCategory } from '@/types';
import { CATEGORY_LABELS } from '@/lib/community';

interface CategoryBadgeProps {
  category: CommunityCategory | string;
}

export default function CategoryBadge({ category }: CategoryBadgeProps) {
  const label = CATEGORY_LABELS[category as CommunityCategory] ?? category;

  return (
    <span className="text-xs font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">
      {label}
    </span>
  );
}
