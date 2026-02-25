'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Header from '@/components/common/Header';
import apiClient from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { ApiResponse, PageResponse, CommunityPostResponse, CommunityPostRequest } from '@/types';

const CATEGORY_MAP: Record<string, string> = {
  '전체': '',
  '일상': 'DAILY',
  '건강': 'HEALTH',
  '취미': 'HOBBY',
  '도움요청': 'HELP',
  '공지': 'NOTICE',
};
const CATEGORY_LABELS: Record<string, string> = {
  'DAILY': '일상',
  'HEALTH': '건강',
  'HOBBY': '취미',
  'HELP': '도움요청',
  'NOTICE': '공지',
};
const CATEGORIES = Object.keys(CATEGORY_MAP);

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
  const [user, setUser] = useState<{ userId: number } | null>(null);

  useEffect(() => {
    if (isLoggedIn()) {
      setUser(getUser());
    }
    fetchPosts();
  }, [page, category]);

  const fetchPosts = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: String(page), size: '10' });
      const catValue = CATEGORY_MAP[category];
      if (catValue) params.append('category', catValue);
      const res = await apiClient.get<ApiResponse<PageResponse<CommunityPostResponse>>>(`/api/community/posts?${params}`);
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

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const hours = Math.floor(diff / (1000 * 60 * 60));

    if (hours < 1) return `${Math.floor(diff / (1000 * 60))}분 전`;
    if (hours < 24) return `${hours}시간 전`;
    if (hours < 48) return '어제';
    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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
                  value={form.category || 'DAILY'}
                  onChange={(e) => setForm({ ...form, category: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                >
                  {CATEGORIES.filter(c => c !== '전체').map(c => (
                    <option key={c} value={CATEGORY_MAP[c]}>{c}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">제목</label>
                <input
                  type="text"
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  placeholder="제목을 입력하세요"
                  maxLength={100}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">내용</label>
                <textarea
                  value={form.content}
                  onChange={(e) => setForm({ ...form, content: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  rows={5}
                  placeholder="내용을 입력하세요"
                  required
                />
              </div>
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

        {/* 카테고리 필터 */}
        <div className="flex gap-2 mb-4 overflow-x-auto pb-1">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => { setCategory(cat); setPage(0); }}
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

        {/* 게시글 목록 */}
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600" />
          </div>
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
                      {post.category && (
                        <span className="text-xs font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">
                          {CATEGORY_LABELS[post.category] || post.category}
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
                  <span>{formatDate(post.createdAt)}</span>
                </div>
              </Link>
            ))}
          </div>
        )}

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div className="flex justify-center gap-2 mt-8">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 hover:bg-gray-50 text-sm"
            >
              이전
            </button>
            <span className="px-4 py-2 text-sm text-gray-600">
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 hover:bg-gray-50 text-sm"
            >
              다음
            </button>
          </div>
        )}
      </main>
    </div>
  );
}
