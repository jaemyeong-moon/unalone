'use client';

import Link from 'next/link';
import { CrawledArticleResponse, ArticleCategory } from '@/types';
import { ARTICLE_CATEGORY_LABELS, ARTICLE_CATEGORY_COLORS } from '@/lib/article';
import { formatRelativeDate } from '@/lib/utils';

interface ArticleCardProps {
  article: CrawledArticleResponse;
}

export default function ArticleCard({ article }: ArticleCardProps) {
  const catColor = ARTICLE_CATEGORY_COLORS[article.category];
  const catLabel = ARTICLE_CATEGORY_LABELS[article.category];

  return (
    <Link
      href={`/articles/${article.id}`}
      className="block bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow"
    >
      <div className="flex">
        {/* Thumbnail */}
        <div className="flex-shrink-0 w-28 h-28 sm:w-36 sm:h-32">
          {article.thumbnailUrl ? (
            <img
              src={article.thumbnailUrl}
              alt={article.originalTitle}
              className="w-full h-full object-cover"
            />
          ) : (
            <CategoryPlaceholder category={article.category} />
          )}
        </div>

        {/* Content */}
        <div className="flex-1 p-4 min-w-0">
          <div className="flex items-center gap-2 mb-1.5">
            <span className={`text-xs font-medium px-2 py-0.5 rounded ${catColor.bg} ${catColor.text}`}>
              {catLabel}
            </span>
          </div>
          <h3 className="text-sm sm:text-base font-medium text-gray-900 line-clamp-1 mb-1">
            {article.originalTitle}
          </h3>
          <p className="text-xs sm:text-sm text-gray-500 line-clamp-2 mb-2">
            {article.summary}
          </p>
          <div className="flex items-center gap-2 text-xs text-gray-400">
            {article.author && (
              <>
                <span>{article.author}</span>
                <span>|</span>
              </>
            )}
            <span>{formatRelativeDate(article.publishedAt)}</span>
            <span>|</span>
            <span className="flex items-center gap-0.5">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              {article.viewCount}
            </span>
          </div>
        </div>
      </div>
    </Link>
  );
}

function CategoryPlaceholder({ category }: { category: ArticleCategory }) {
  const colorMap: Record<ArticleCategory, string> = {
    HEALTH: 'from-emerald-400 to-emerald-600',
    WELFARE: 'from-blue-400 to-blue-600',
    ELDERLY_CARE: 'from-purple-400 to-purple-600',
    SAFETY: 'from-orange-400 to-orange-600',
    POLICY: 'from-indigo-400 to-indigo-600',
    LIFESTYLE: 'from-pink-400 to-pink-600',
  };

  const iconMap: Record<ArticleCategory, string> = {
    HEALTH: '🏥',
    WELFARE: '🤝',
    ELDERLY_CARE: '👴',
    SAFETY: '🛡️',
    POLICY: '📋',
    LIFESTYLE: '🌿',
  };

  return (
    <div className={`w-full h-full bg-gradient-to-br ${colorMap[category]} flex items-center justify-center`}>
      <span className="text-3xl">{iconMap[category]}</span>
    </div>
  );
}
