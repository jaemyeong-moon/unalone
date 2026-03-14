'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import CategoryBadge from '@/components/common/CategoryBadge';
import apiClient from '@/lib/api';
import { isLoggedIn, getUser } from '@/lib/auth';
import { formatDateTimeFull } from '@/lib/utils';
import { ApiResponse, CommunityPostResponse } from '@/types';

export default function CommunityDetailPage() {
  const router = useRouter();
  const params = useParams();
  const postId = params.id;
  const [post, setPost] = useState<CommunityPostResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [userId, setUserId] = useState<number | null>(null);
  const [userRole, setUserRole] = useState<string | null>(null);

  useEffect(() => {
    if (isLoggedIn()) {
      const user = getUser();
      setUserId(user?.userId ?? null);
      setUserRole(user?.role ?? null);
    }
    fetchPost();
  }, [postId]);

  const fetchPost = async () => {
    try {
      const res = await apiClient.get<ApiResponse<CommunityPostResponse>>(
        `/api/community/posts/${postId}`
      );
      setPost(res.data.data);
    } catch {
      console.error('Failed to fetch post');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm('이 게시글을 삭제하시겠습니까?')) return;
    try {
      await apiClient.delete(`/api/community/posts/${postId}`);
      router.push('/community');
    } catch {
      alert('삭제에 실패했습니다');
    }
  };

  const canDelete = post && (userId === post.userId || userRole === 'ROLE_ADMIN');

  return (
    <PageLayout maxWidth="max-w-3xl">
      <Link
        href="/community"
        className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-6"
      >
        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        목록으로
      </Link>

      {loading ? (
        <LoadingSpinner />
      ) : !post ? (
        <div className="text-center py-12">
          <p className="text-gray-500">게시글을 찾을 수 없습니다</p>
          <Link href="/community" className="text-emerald-600 hover:underline text-sm mt-2 inline-block">
            목록으로 돌아가기
          </Link>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-100">
            <div className="flex items-center gap-2 mb-3">
              {post.category && <CategoryBadge category={post.category} />}
            </div>
            <h1 className="text-xl font-bold text-gray-900 mb-3">{post.title}</h1>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <span className="font-medium">{post.userName}</span>
                <span>|</span>
                <span>{formatDateTimeFull(post.createdAt)}</span>
              </div>
              {canDelete && (
                <button
                  onClick={handleDelete}
                  className="text-sm text-red-500 hover:text-red-700 transition-colors"
                >
                  삭제
                </button>
              )}
            </div>
          </div>
          <div className="p-6">
            <p className="text-gray-700 whitespace-pre-wrap leading-relaxed">{post.content}</p>
          </div>
        </div>
      )}
    </PageLayout>
  );
}
