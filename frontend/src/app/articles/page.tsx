'use client';

import { useEffect, useState } from 'react';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import Pagination from '@/components/common/Pagination';
import ArticleCard from '@/components/common/ArticleCard';
import {
  getArticles,
  getPopularArticles,
  ARTICLE_CATEGORY_FILTER_MAP,
  ARTICLE_CATEGORY_FILTER_LABELS,
  ARTICLE_CATEGORY_COLORS,
  ARTICLE_CATEGORY_LABELS,
} from '@/lib/article';
import { formatRelativeDate } from '@/lib/utils';
import { CrawledArticleResponse, ArticleCategory } from '@/types';
import Link from 'next/link';

export default function ArticlesPage() {
  const [articles, setArticles] = useState<CrawledArticleResponse[]>([]);
  const [popularArticles, setPopularArticles] = useState<CrawledArticleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [category, setCategory] = useState('전체');

  useEffect(() => {
    fetchArticles();
  }, [page, category]);

  useEffect(() => {
    fetchPopular();
  }, []);

  const fetchArticles = async () => {
    setLoading(true);
    try {
      const catValue = ARTICLE_CATEGORY_FILTER_MAP[category];
      const data = await getArticles(page, 10, catValue ? (catValue as ArticleCategory) : undefined);
      setArticles(data.content);
      setTotalPages(data.totalPages);
    } catch {
      console.error('Failed to fetch articles');
    } finally {
      setLoading(false);
    }
  };

  const fetchPopular = async () => {
    try {
      const data = await getPopularArticles();
      setPopularArticles(data);
    } catch {
      console.error('Failed to fetch popular articles');
    }
  };

  const handleCategoryChange = (cat: string) => {
    setCategory(cat);
    setPage(0);
  };

  return (
    <PageLayout maxWidth="max-w-5xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">뉴스</h1>
        <p className="text-sm text-gray-500 mt-1">건강, 복지, 안전 관련 최신 소식</p>
      </div>

      {/* 인기 기사 (모바일: 상단, 데스크탑: 사이드바) */}
      {popularArticles.length > 0 && (
        <div className="lg:hidden mb-6">
          <PopularArticlesSection articles={popularArticles} />
        </div>
      )}

      <div className="flex gap-6">
        {/* 메인 컨텐츠 */}
        <div className="flex-1 min-w-0">
          {/* 카테고리 필터 */}
          <div className="flex gap-2 mb-4 overflow-x-auto pb-1">
            {ARTICLE_CATEGORY_FILTER_LABELS.map((cat) => (
              <button
                key={cat}
                onClick={() => handleCategoryChange(cat)}
                className={`px-3 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                  category === cat
                    ? 'bg-emerald-600 text-white'
                    : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* 기사 목록 */}
          {loading ? (
            <LoadingSpinner />
          ) : articles.length === 0 ? (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
              <p className="text-gray-500">기사가 없습니다</p>
            </div>
          ) : (
            <div className="space-y-3">
              {articles.map((article) => (
                <ArticleCard key={article.id} article={article} />
              ))}
            </div>
          )}

          <Pagination
            page={page}
            totalPages={totalPages}
            onPrev={() => setPage(Math.max(0, page - 1))}
            onNext={() => setPage(Math.min(totalPages - 1, page + 1))}
          />
        </div>

        {/* 사이드바 - 인기 기사 (데스크탑만) */}
        {popularArticles.length > 0 && (
          <div className="hidden lg:block w-72 flex-shrink-0">
            <PopularArticlesSection articles={popularArticles} />
          </div>
        )}
      </div>
    </PageLayout>
  );
}

function PopularArticlesSection({ articles }: { articles: CrawledArticleResponse[] }) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
      <h2 className="text-base font-semibold text-gray-900 mb-4">인기 기사</h2>
      <div className="space-y-3">
        {articles.slice(0, 5).map((article, index) => {
          const catColor = ARTICLE_CATEGORY_COLORS[article.category];
          return (
            <Link
              key={article.id}
              href={`/articles/${article.id}`}
              className="flex items-start gap-3 group"
            >
              <span className="flex-shrink-0 w-6 h-6 rounded-full bg-emerald-100 text-emerald-700 text-xs font-bold flex items-center justify-center">
                {index + 1}
              </span>
              <div className="min-w-0 flex-1">
                <p className="text-sm font-medium text-gray-900 line-clamp-2 group-hover:text-emerald-600 transition-colors">
                  {article.originalTitle}
                </p>
                <div className="flex items-center gap-1.5 mt-1">
                  <span className={`text-xs ${catColor.text}`}>
                    {ARTICLE_CATEGORY_LABELS[article.category]}
                  </span>
                  <span className="text-xs text-gray-400">
                    {formatRelativeDate(article.publishedAt)}
                  </span>
                </div>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
