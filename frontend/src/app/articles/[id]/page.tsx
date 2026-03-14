'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import TranslationToggle from '@/components/common/TranslationToggle';
import ArticleCard from '@/components/common/ArticleCard';
import { getArticle, getArticles, ARTICLE_CATEGORY_LABELS, ARTICLE_CATEGORY_COLORS } from '@/lib/article';
import { formatDateTimeFull } from '@/lib/utils';
import { ArticleDetailResponse, CrawledArticleResponse } from '@/types';

export default function ArticleDetailPage() {
  const params = useParams();
  const articleId = Number(params.id);
  const [article, setArticle] = useState<ArticleDetailResponse | null>(null);
  const [relatedArticles, setRelatedArticles] = useState<CrawledArticleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showTranslation, setShowTranslation] = useState(false);
  const [expanded, setExpanded] = useState(false);

  useEffect(() => {
    fetchArticle();
  }, [articleId]);

  const fetchArticle = async () => {
    setLoading(true);
    try {
      const data = await getArticle(articleId);
      setArticle(data);
      // Fetch related articles (same category)
      try {
        const related = await getArticles(0, 5, data.category);
        setRelatedArticles(related.content.filter((a) => a.id !== articleId).slice(0, 3));
      } catch {
        // ignore
      }
    } catch {
      console.error('Failed to fetch article');
    } finally {
      setLoading(false);
    }
  };

  const displayTitle = showTranslation && article?.translatedTitle
    ? article.translatedTitle
    : article?.originalTitle;

  const displayContent = showTranslation && article?.translatedContent
    ? article.translatedContent
    : article?.originalContent;

  const hasTranslation = article?.translatedTitle || article?.translatedContent;
  const translationStatus = hasTranslation ? 'TRANSLATED' as const : undefined;

  return (
    <PageLayout maxWidth="max-w-3xl">
      <Link
        href="/articles"
        className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6"
      >
        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        뉴스 목록
      </Link>

      {loading ? (
        <LoadingSpinner />
      ) : !article ? (
        <div className="text-center py-12">
          <p className="text-gray-500">기사를 찾을 수 없습니다</p>
          <Link href="/articles" className="text-emerald-600 hover:underline text-sm mt-2 inline-block">
            목록으로 돌아가기
          </Link>
        </div>
      ) : (
        <>
          <div className="bg-white rounded-xl shadow-sm border border-gray-200">
            {/* Header */}
            <div className="p-6 border-b border-gray-100">
              <div className="flex items-center gap-2 mb-3">
                <span className={`text-xs font-medium px-2 py-0.5 rounded ${ARTICLE_CATEGORY_COLORS[article.category].bg} ${ARTICLE_CATEGORY_COLORS[article.category].text}`}>
                  {ARTICLE_CATEGORY_LABELS[article.category]}
                </span>
                {translationStatus && (
                  <TranslationToggle
                    translationStatus={translationStatus}
                    showTranslation={showTranslation}
                    onToggle={() => setShowTranslation(!showTranslation)}
                  />
                )}
              </div>
              <h1 className="text-xl font-bold text-gray-900 mb-3">{displayTitle}</h1>
              <div className="flex items-center gap-3 text-sm text-gray-500">
                {article.author && <span className="font-medium">{article.author}</span>}
                <span>{formatDateTimeFull(article.publishedAt)}</span>
                <span className="flex items-center gap-1">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  {article.viewCount}
                </span>
              </div>
            </div>

            {/* Summary */}
            <div className="p-6 bg-emerald-50 border-b border-gray-100">
              <h2 className="text-sm font-semibold text-emerald-700 mb-2">요약</h2>
              <p className="text-sm text-gray-700 leading-relaxed">{article.summary}</p>
            </div>

            {/* Full Content */}
            <div className="p-6">
              <div className={`text-gray-700 whitespace-pre-wrap leading-relaxed ${!expanded ? 'max-h-96 overflow-hidden relative' : ''}`}>
                {displayContent}
                {!expanded && displayContent && displayContent.length > 500 && (
                  <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-white to-transparent" />
                )}
              </div>
              {displayContent && displayContent.length > 500 && (
                <button
                  onClick={() => setExpanded(!expanded)}
                  className="mt-4 text-sm text-emerald-600 hover:text-emerald-700 font-medium"
                >
                  {expanded ? '접기' : '전체 보기'}
                </button>
              )}
            </div>

            {/* Original Link */}
            <div className="px-6 pb-6">
              <a
                href={article.originalUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm font-medium"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                </svg>
                원문 보기
              </a>
            </div>
          </div>

          {/* Related Articles */}
          {relatedArticles.length > 0 && (
            <div className="mt-8">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">관련 기사</h2>
              <div className="space-y-3">
                {relatedArticles.map((related) => (
                  <ArticleCard key={related.id} article={related} />
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </PageLayout>
  );
}
