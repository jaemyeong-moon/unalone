'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import Pagination from '@/components/common/Pagination';
import { isLoggedIn, getUser } from '@/lib/auth';
import { getErrorMessage } from '@/lib/api';
import {
  getNewsSources,
  createNewsSource,
  deleteNewsSource,
  triggerCrawl,
  getAdminArticles,
  publishArticle,
  rejectArticle,
  getCrawlStats,
  ARTICLE_CATEGORY_LABELS,
  ARTICLE_STATUS_LABELS,
  ARTICLE_CATEGORY_COLORS,
} from '@/lib/article';
import { formatShortDateTime } from '@/lib/utils';
import {
  NewsSourceResponse,
  NewsSourceRequest,
  ArticleDetailResponse,
  ArticleStatus,
  ArticleCategory,
  CrawlStatsResponse,
} from '@/types';

type Tab = 'sources' | 'articles' | 'stats';

export default function AdminArticlesPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<Tab>('sources');

  useEffect(() => {
    const user = getUser();
    if (!isLoggedIn() || user?.role !== 'ROLE_ADMIN') {
      router.push('/');
    }
  }, []);

  const tabs: { key: Tab; label: string }[] = [
    { key: 'sources', label: '뉴스 소스' },
    { key: 'articles', label: '기사 관리' },
    { key: 'stats', label: '크롤링 통계' },
  ];

  return (
    <PageLayout maxWidth="max-w-7xl">
      <div className="flex items-center gap-4 mb-6">
        <Link href="/admin" className="text-sm text-gray-500 hover:text-gray-700">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 inline mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          대시보드
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">기사 관리</h1>
          <p className="text-sm text-gray-500 mt-1">뉴스 소스 관리 및 기사 게시 승인</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 border-b border-gray-200">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 transition-colors ${
              activeTab === tab.key
                ? 'border-emerald-600 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'sources' && <NewsSourcesTab />}
      {activeTab === 'articles' && <ArticlesTab />}
      {activeTab === 'stats' && <StatsTab />}
    </PageLayout>
  );
}

// === News Sources Tab ===
function NewsSourcesTab() {
  const [sources, setSources] = useState<NewsSourceResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [crawling, setCrawling] = useState(false);
  const [form, setForm] = useState<NewsSourceRequest>({
    name: '',
    baseUrl: '',
    crawlPattern: '',
    articlePattern: '',
    category: 'HEALTH',
    enabled: true,
  });

  useEffect(() => {
    fetchSources();
  }, []);

  const fetchSources = async () => {
    setLoading(true);
    try {
      const data = await getNewsSources();
      setSources(data);
    } catch {
      console.error('Failed to fetch sources');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createNewsSource(form);
      setForm({ name: '', baseUrl: '', crawlPattern: '', articlePattern: '', category: 'HEALTH', enabled: true });
      setShowForm(false);
      fetchSources();
    } catch (err) {
      alert(getErrorMessage(err, '소스 생성에 실패했습니다'));
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('이 뉴스 소스를 삭제하시겠습니까?')) return;
    try {
      await deleteNewsSource(id);
      fetchSources();
    } catch (err) {
      alert(getErrorMessage(err, '삭제에 실패했습니다'));
    }
  };

  const handleTriggerCrawl = async () => {
    setCrawling(true);
    try {
      await triggerCrawl();
      alert('크롤링이 시작되었습니다');
    } catch (err) {
      alert(getErrorMessage(err, '크롤링 실행에 실패했습니다'));
    } finally {
      setCrawling(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <p className="text-sm text-gray-500">총 {sources.length}개 소스</p>
        <div className="flex gap-2">
          <button
            onClick={handleTriggerCrawl}
            disabled={crawling}
            className="px-4 py-2 bg-amber-500 text-white rounded-lg hover:bg-amber-600 disabled:opacity-50 transition-colors text-sm font-medium"
          >
            {crawling ? '크롤링 중...' : '수동 크롤링 실행'}
          </button>
          <button
            onClick={() => setShowForm(!showForm)}
            className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors text-sm font-medium"
          >
            {showForm ? '취소' : '소스 추가'}
          </button>
        </div>
      </div>

      {showForm && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">소스명</label>
                <input
                  type="text"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">기본 URL</label>
                <input
                  type="url"
                  value={form.baseUrl}
                  onChange={(e) => setForm({ ...form, baseUrl: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">크롤링 패턴</label>
                <input
                  type="text"
                  value={form.crawlPattern}
                  onChange={(e) => setForm({ ...form, crawlPattern: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  placeholder="/news/list/*"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">기사 패턴</label>
                <input
                  type="text"
                  value={form.articlePattern}
                  onChange={(e) => setForm({ ...form, articlePattern: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  placeholder="/news/article/{id}"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
                <select
                  value={form.category}
                  onChange={(e) => setForm({ ...form, category: e.target.value as ArticleCategory })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                >
                  {(Object.entries(ARTICLE_CATEGORY_LABELS) as [ArticleCategory, string][]).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
              <div className="flex items-end">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={form.enabled}
                    onChange={(e) => setForm({ ...form, enabled: e.target.checked })}
                    className="w-4 h-4 text-emerald-600 rounded"
                  />
                  <span className="text-sm font-medium text-gray-700">활성화</span>
                </label>
              </div>
            </div>
            <button
              type="submit"
              className="px-6 py-2.5 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors text-sm font-medium"
            >
              소스 등록
            </button>
          </form>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-4 py-3 text-left font-medium text-gray-600">소스명</th>
              <th className="px-4 py-3 text-left font-medium text-gray-600 hidden md:table-cell">URL</th>
              <th className="px-4 py-3 text-left font-medium text-gray-600">카테고리</th>
              <th className="px-4 py-3 text-center font-medium text-gray-600">상태</th>
              <th className="px-4 py-3 text-left font-medium text-gray-600 hidden md:table-cell">마지막 수집</th>
              <th className="px-4 py-3 text-center font-medium text-gray-600">작업</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {sources.map((source) => {
              const catColor = ARTICLE_CATEGORY_COLORS[source.category];
              return (
                <tr key={source.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-900">{source.name}</td>
                  <td className="px-4 py-3 text-gray-500 hidden md:table-cell truncate max-w-[200px]">{source.baseUrl}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded ${catColor.bg} ${catColor.text}`}>
                      {ARTICLE_CATEGORY_LABELS[source.category]}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded ${source.enabled ? 'bg-emerald-50 text-emerald-600' : 'bg-gray-100 text-gray-500'}`}>
                      {source.enabled ? '활성' : '비활성'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500 hidden md:table-cell">
                    {formatShortDateTime(source.lastCrawledAt ?? null)}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <button
                      onClick={() => handleDelete(source.id)}
                      className="text-red-500 hover:text-red-700 text-xs font-medium"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              );
            })}
            {sources.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-500">
                  등록된 뉴스 소스가 없습니다
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// === Articles Tab ===
function ArticlesTab() {
  const [articles, setArticles] = useState<ArticleDetailResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState<ArticleStatus | ''>('');

  useEffect(() => {
    fetchArticles();
  }, [page, statusFilter]);

  const fetchArticles = async () => {
    setLoading(true);
    try {
      const data = await getAdminArticles(page, statusFilter || undefined);
      setArticles(data.content);
      setTotalPages(data.totalPages);
    } catch {
      console.error('Failed to fetch articles');
    } finally {
      setLoading(false);
    }
  };

  const handlePublish = async (id: number) => {
    try {
      await publishArticle(id);
      fetchArticles();
    } catch (err) {
      alert(getErrorMessage(err, '게시에 실패했습니다'));
    }
  };

  const handleReject = async (id: number) => {
    try {
      await rejectArticle(id);
      fetchArticles();
    } catch (err) {
      alert(getErrorMessage(err, '반려에 실패했습니다'));
    }
  };

  const statusOptions: { value: string; label: string }[] = [
    { value: '', label: '전체 상태' },
    { value: 'CRAWLED', label: '수집됨' },
    { value: 'SUMMARIZED', label: '요약완료' },
    { value: 'PUBLISHED', label: '게시됨' },
    { value: 'REJECTED', label: '반려됨' },
    { value: 'FAILED', label: '실패' },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value as ArticleStatus | ''); setPage(0); }}
          className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500"
        >
          {statusOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-4 py-3 text-left font-medium text-gray-600">제목</th>
                <th className="px-4 py-3 text-left font-medium text-gray-600 hidden md:table-cell">카테고리</th>
                <th className="px-4 py-3 text-center font-medium text-gray-600">상태</th>
                <th className="px-4 py-3 text-center font-medium text-gray-600">품질</th>
                <th className="px-4 py-3 text-center font-medium text-gray-600">작업</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {articles.map((article) => {
                const catColor = ARTICLE_CATEGORY_COLORS[article.category];
                const statusColor = article.status === 'PUBLISHED'
                  ? 'bg-emerald-50 text-emerald-600'
                  : article.status === 'REJECTED'
                  ? 'bg-red-50 text-red-600'
                  : article.status === 'FAILED'
                  ? 'bg-red-50 text-red-500'
                  : 'bg-gray-100 text-gray-600';
                return (
                  <tr key={article.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <p className="font-medium text-gray-900 line-clamp-1">{article.originalTitle}</p>
                      <p className="text-xs text-gray-400 mt-0.5">{formatShortDateTime(article.publishedAt)}</p>
                    </td>
                    <td className="px-4 py-3 hidden md:table-cell">
                      <span className={`text-xs font-medium px-2 py-0.5 rounded ${catColor.bg} ${catColor.text}`}>
                        {ARTICLE_CATEGORY_LABELS[article.category]}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <span className={`text-xs font-medium px-2 py-0.5 rounded ${statusColor}`}>
                        {ARTICLE_STATUS_LABELS[article.status]}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <span className="text-sm font-medium text-gray-700">{article.qualityScore}</span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      {(article.status === 'CRAWLED' || article.status === 'SUMMARIZED') && (
                        <div className="flex gap-2 justify-center">
                          <button
                            onClick={() => handlePublish(article.id)}
                            className="px-3 py-1 bg-emerald-600 text-white rounded text-xs hover:bg-emerald-700"
                          >
                            게시
                          </button>
                          <button
                            onClick={() => handleReject(article.id)}
                            className="px-3 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600"
                          >
                            반려
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })}
              {articles.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                    기사가 없습니다
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      <Pagination
        page={page}
        totalPages={totalPages}
        onPrev={() => setPage(Math.max(0, page - 1))}
        onNext={() => setPage(Math.min(totalPages - 1, page + 1))}
      />
    </div>
  );
}

// === Stats Tab ===
function StatsTab() {
  const [stats, setStats] = useState<CrawlStatsResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const data = await getCrawlStats();
      setStats(data);
    } catch {
      console.error('Failed to fetch stats');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner />;
  if (!stats) return <p className="text-gray-500 text-center py-8">통계를 불러올 수 없습니다</p>;

  const statCards = [
    { label: '전체 소스', value: stats.totalSources, color: 'bg-blue-500' },
    { label: '전체 기사', value: stats.totalArticles, color: 'bg-emerald-500' },
    { label: '게시된 기사', value: stats.publishedArticles, color: 'bg-green-500' },
    { label: '반려된 기사', value: stats.rejectedArticles, color: 'bg-red-500' },
    { label: '대기 중 기사', value: stats.pendingArticles, color: 'bg-amber-500' },
  ];

  return (
    <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
      {statCards.map((stat) => (
        <div key={stat.label} className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <p className="text-sm text-gray-500">{stat.label}</p>
          <p className="text-3xl font-bold text-gray-900 mt-1">{stat.value}</p>
          <div className={`h-1 w-12 ${stat.color} rounded-full mt-3`} />
        </div>
      ))}
    </div>
  );
}
