'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import Pagination from '@/components/common/Pagination';
import CategoryBadge from '@/components/common/CategoryBadge';
import QualityBadge from '@/components/common/QualityBadge';
import FormField from '@/components/common/FormField';
import apiClient from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { formatRelativeDate } from '@/lib/utils';
import { CATEGORY_FILTER_MAP, CATEGORY_FILTER_LABELS } from '@/lib/community';
import { ApiResponse, PageResponse, CommunityPostResponse, CommunityPostRequest } from '@/types';

export default function CommunityPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<CommunityPostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [category, setCategory] = useState('전체');
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState<CommunityPostRequest>({ title: '', content: '', category: 'DAILY' });
  const [sortBy, setSortBy] = useState<'latest' | 'quality'>('latest');

  useEffect(() => {
    if (isLoggedIn()) {
      getUser();
    }
    fetchPosts();
  }, [page, category, sortBy]);

  const fetchPosts = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: String(page), size: '10' });
      const catValue = CATEGORY_FILTER_MAP[category];
      if (catValue) params.append('category', catValue);
      if (sortBy === 'quality') params.append('sort', 'quality');
      const res = await apiClient.get<ApiResponse<PageResponse<CommunityPostResponse>>>(
        `/api/community/posts?${params}`
      );
      setPosts(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
    } catch {
      console.error('Failed to fetch posts');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    setSaving(true);
    try {
      await apiClient.post<ApiResponse<CommunityPostResponse>>('/api/community/posts', form);
      setForm({ title: '', content: '', category: 'DAILY' });
      setShowForm(false);
      setPage(0);
      fetchPosts();
    } catch {
      alert('게시글 작성에 실패했습니다');
    } finally {
      setSaving(false);
    }
  };

  const handleCategoryChange = (cat: string) => {
    setCategory(cat);
    setPage(0);
  };

  return (
    <PageLayout maxWidth="max-w-3xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">커뮤니티</h1>
          <p className="text-sm text-gray-500 mt-1">이웃과 소통하며 함께하는 일상</p>
        </div>
        {isLoggedIn() && (
          <button
            onClick={() => setShowForm(!showForm)}
            className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors text-sm font-medium"
          >
            {showForm ? '취소' : '글쓰기'}
          </button>
        )}
      </div>

      {/* 글쓰기 폼 */}
      {showForm && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
              <select
                value={form.category ?? 'DAILY'}
                onChange={(e) => setForm({ ...form, category: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              >
                {CATEGORY_FILTER_LABELS.filter((c) => c !== '전체').map((c) => (
                  <option key={c} value={CATEGORY_FILTER_MAP[c]}>{c}</option>
                ))}
              </select>
            </div>
            <FormField
              label="제목"
              type="text"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              placeholder="제목을 입력하세요"
              maxLength={100}
              required
            />
            <FormField
              as="textarea"
              label="내용"
              value={form.content}
              onChange={(e) => setForm({ ...form, content: e.target.value })}
              rows={5}
              placeholder="내용을 입력하세요"
              required
            />
            <button
              type="submit"
              disabled={saving}
              className="w-full py-2.5 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors font-medium text-sm"
            >
              {saving ? '등록 중...' : '게시글 등록'}
            </button>
          </form>
        </div>
      )}

      {/* 카테고리 필터 + 정렬 */}
      <div className="flex items-center justify-between gap-2 mb-4">
        <div className="flex gap-2 overflow-x-auto pb-1">
          {CATEGORY_FILTER_LABELS.map((cat) => (
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
        <div className="flex-shrink-0">
          <select
            value={sortBy}
            onChange={(e) => { setSortBy(e.target.value as 'latest' | 'quality'); setPage(0); }}
            className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 bg-white"
          >
            <option value="latest">최신순</option>
            <option value="quality">품질순</option>
          </select>
        </div>
      </div>

      {/* 게시글 목록 */}
      {loading ? (
        <LoadingSpinner />
      ) : posts.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <p className="text-gray-500">게시글이 없습니다</p>
          <p className="text-sm text-gray-400 mt-1">첫 번째 글을 작성해보세요</p>
        </div>
      ) : (
        <div className="space-y-3">
          {posts.map((post) => (
            <Link
              key={post.id}
              href={`/community/${post.id}`}
              className="block bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    {post.category && <CategoryBadge category={post.category} />}
                    {post.qualityGrade && <QualityBadge grade={post.qualityGrade} score={post.qualityScore} />}
                    {post.translationStatus === 'TRANSLATED' && (
                      <span className="text-xs text-emerald-500" title="번역 가능">
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5 inline" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
                        </svg>
                      </span>
                    )}
                  </div>
                  <h3 className="text-base font-medium text-gray-900 truncate">{post.title}</h3>
                  <p className="text-sm text-gray-500 mt-1 line-clamp-2">{post.content}</p>
                </div>
              </div>
              <div className="flex items-center gap-2 mt-3 text-xs text-gray-400">
                <span>{post.userName}</span>
                <span>|</span>
                <span>{formatRelativeDate(post.createdAt)}</span>
              </div>
            </Link>
          ))}
        </div>
      )}

      <Pagination
        page={page}
        totalPages={totalPages}
        onPrev={() => setPage(Math.max(0, page - 1))}
        onNext={() => setPage(Math.min(totalPages - 1, page + 1))}
      />
    </PageLayout>
  );
}
