'use client';

import { useState } from 'react';
import { CommentResponse } from '@/types';
import { updateComment, deleteComment } from '@/lib/community';
import { formatTimeAgo } from '@/lib/utils';

interface CommentItemProps {
  comment: CommentResponse;
  postId: number;
  currentUserId: number | null;
  /** 답글 작성 폼 표시 콜백 (최상위 댓글만) */
  onReply?: (parentId: number) => void;
  /** 댓글 목록 새로고침 */
  onRefresh: () => void;
  /** 답글 depth (0 = 최상위, 1 = 답글) */
  depth?: number;
}

export default function CommentItem({
  comment,
  postId,
  currentUserId,
  onReply,
  onRefresh,
  depth = 0,
}: CommentItemProps) {
  const [editing, setEditing] = useState(false);
  const [editContent, setEditContent] = useState(comment.content);
  const [saving, setSaving] = useState(false);

  const isOwner = currentUserId !== null && currentUserId === comment.userId;

  const handleEdit = async () => {
    if (!editContent.trim()) return;
    setSaving(true);
    try {
      await updateComment(postId, comment.id, editContent.trim());
      setEditing(false);
      onRefresh();
    } catch {
      alert('댓글 수정에 실패했습니다');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm('이 댓글을 삭제하시겠습니까?')) return;
    try {
      await deleteComment(postId, comment.id);
      onRefresh();
    } catch {
      alert('댓글 삭제에 실패했습니다');
    }
  };

  return (
    <div className={depth > 0 ? 'ml-8 pl-4 border-l-2 border-gray-100' : ''}>
      <div className="py-3">
        {/* Author & Time */}
        <div className="flex items-center gap-2 mb-1">
          <span className="text-sm font-medium text-gray-900">{comment.authorName}</span>
          <span className="text-xs text-gray-400">{formatTimeAgo(comment.createdAt)}</span>
          {comment.updatedAt !== comment.createdAt && (
            <span className="text-xs text-gray-400">(수정됨)</span>
          )}
        </div>

        {/* Content or Edit Form */}
        {editing ? (
          <div className="mt-2">
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-none"
              rows={3}
            />
            <div className="flex gap-2 mt-2">
              <button
                onClick={handleEdit}
                disabled={saving || !editContent.trim()}
                className="px-3 py-1.5 text-xs font-medium text-white bg-emerald-600 rounded-lg hover:bg-emerald-700 disabled:opacity-50 transition-colors"
              >
                {saving ? '저장 중...' : '저장'}
              </button>
              <button
                onClick={() => { setEditing(false); setEditContent(comment.content); }}
                className="px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                취소
              </button>
            </div>
          </div>
        ) : (
          <p className="text-sm text-gray-700 whitespace-pre-wrap">{comment.content}</p>
        )}

        {/* Actions */}
        {!editing && (
          <div className="flex items-center gap-3 mt-2">
            {depth === 0 && onReply && (
              <button
                onClick={() => onReply(comment.id)}
                className="text-xs text-gray-500 hover:text-emerald-600 font-medium transition-colors"
              >
                답글
              </button>
            )}
            {isOwner && (
              <>
                <button
                  onClick={() => setEditing(true)}
                  className="text-xs text-gray-500 hover:text-emerald-600 font-medium transition-colors"
                >
                  수정
                </button>
                <button
                  onClick={handleDelete}
                  className="text-xs text-gray-500 hover:text-red-500 font-medium transition-colors"
                >
                  삭제
                </button>
              </>
            )}
          </div>
        )}
      </div>

      {/* Nested Replies (max 1 level) */}
      {comment.replies && comment.replies.length > 0 && (
        <div>
          {comment.replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              postId={postId}
              currentUserId={currentUserId}
              onRefresh={onRefresh}
              depth={1}
            />
          ))}
        </div>
      )}
    </div>
  );
}
