'use client';

import { useEffect, useState, useCallback } from 'react';
import { CommentResponse } from '@/types';
import { getComments, createComment } from '@/lib/community';
import { isLoggedIn } from '@/lib/auth';
import CommentItem from '@/components/common/CommentItem';

interface CommentSectionProps {
  postId: number;
  currentUserId: number | null;
}

export default function CommentSection({ postId, currentUserId }: CommentSectionProps) {
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [replyTo, setReplyTo] = useState<number | null>(null);
  const [replyContent, setReplyContent] = useState('');
  const [replySubmitting, setReplySubmitting] = useState(false);

  const fetchComments = useCallback(async () => {
    try {
      const data = await getComments(postId);
      setComments(data);
    } catch {
      console.error('Failed to fetch comments');
    } finally {
      setLoading(false);
    }
  }, [postId]);

  useEffect(() => {
    fetchComments();
  }, [fetchComments]);

  // 전체 댓글 수 계산 (답글 포함)
  const totalCount = comments.reduce(
    (acc, c) => acc + 1 + (c.replies ? c.replies.length : 0),
    0
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;
    setSubmitting(true);
    try {
      await createComment(postId, { content: content.trim() });
      setContent('');
      fetchComments();
    } catch {
      alert('댓글 작성에 실패했습니다');
    } finally {
      setSubmitting(false);
    }
  };

  const handleReplySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!replyContent.trim() || replyTo === null) return;
    setReplySubmitting(true);
    try {
      await createComment(postId, { content: replyContent.trim(), parentId: replyTo });
      setReplyContent('');
      setReplyTo(null);
      fetchComments();
    } catch {
      alert('답글 작성에 실패했습니다');
    } finally {
      setReplySubmitting(false);
    }
  };

  const handleReply = (parentId: number) => {
    setReplyTo((prev) => (prev === parentId ? null : parentId));
    setReplyContent('');
  };

  return (
    <div className="mt-6">
      {/* Comment Count Header */}
      <h2 className="text-lg font-semibold text-gray-900 mb-4">
        댓글 {totalCount > 0 && <span className="text-emerald-600">{totalCount}</span>}
      </h2>

      {/* Comment Input Form */}
      {isLoggedIn() ? (
        <form onSubmit={handleSubmit} className="mb-6">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="댓글을 입력하세요"
            rows={3}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-none placeholder-gray-400"
          />
          <div className="flex justify-end mt-2">
            <button
              type="submit"
              disabled={submitting || !content.trim()}
              className="px-4 py-2 text-sm font-medium text-white bg-emerald-600 rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors"
            >
              {submitting ? '등록 중...' : '댓글 등록'}
            </button>
          </div>
        </form>
      ) : (
        <div className="mb-6 p-4 bg-gray-50 rounded-lg text-center">
          <p className="text-sm text-gray-500">댓글을 작성하려면 로그인이 필요합니다</p>
        </div>
      )}

      {/* Comment List */}
      {loading ? (
        <div className="py-8 text-center">
          <div className="inline-block h-6 w-6 animate-spin rounded-full border-2 border-emerald-500 border-t-transparent" />
        </div>
      ) : comments.length === 0 ? (
        <div className="py-8 text-center">
          <p className="text-sm text-gray-400">아직 댓글이 없습니다</p>
          <p className="text-xs text-gray-300 mt-1">첫 번째 댓글을 남겨보세요</p>
        </div>
      ) : (
        <div className="divide-y divide-gray-100">
          {comments.map((comment) => (
            <div key={comment.id}>
              <CommentItem
                comment={comment}
                postId={postId}
                currentUserId={currentUserId}
                onReply={handleReply}
                onRefresh={fetchComments}
                depth={0}
              />

              {/* Inline Reply Form */}
              {replyTo === comment.id && isLoggedIn() && (
                <div className="ml-8 pl-4 border-l-2 border-emerald-200 pb-3">
                  <form onSubmit={handleReplySubmit}>
                    <textarea
                      value={replyContent}
                      onChange={(e) => setReplyContent(e.target.value)}
                      placeholder={`${comment.authorName}님에게 답글 작성`}
                      rows={2}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-none placeholder-gray-400"
                      autoFocus
                    />
                    <div className="flex gap-2 mt-2">
                      <button
                        type="submit"
                        disabled={replySubmitting || !replyContent.trim()}
                        className="px-3 py-1.5 text-xs font-medium text-white bg-emerald-600 rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors"
                      >
                        {replySubmitting ? '등록 중...' : '답글 등록'}
                      </button>
                      <button
                        type="button"
                        onClick={() => { setReplyTo(null); setReplyContent(''); }}
                        className="px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                      >
                        취소
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
