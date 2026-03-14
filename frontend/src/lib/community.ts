import apiClient from '@/lib/api';
import {
  CommunityCategory,
  ApiResponse,
  CommentRequest,
  CommentResponse,
} from '@/types';

/** 카테고리 표시명 매핑 */
export const CATEGORY_LABELS: Record<CommunityCategory, string> = {
  DAILY: '일상',
  HEALTH: '건강',
  HOBBY: '취미',
  HELP: '도움요청',
  NOTICE: '공지',
};

/** 필터용 카테고리 목록 (전체 포함) */
export const CATEGORY_FILTER_MAP: Record<string, string> = {
  '전체': '',
  '일상': 'DAILY',
  '건강': 'HEALTH',
  '취미': 'HOBBY',
  '도움요청': 'HELP',
  '공지': 'NOTICE',
};

export const CATEGORY_FILTER_LABELS = Object.keys(CATEGORY_FILTER_MAP);

// === 댓글 API ===

/** 게시글의 댓글 목록 조회 */
export async function getComments(postId: number): Promise<CommentResponse[]> {
  const res = await apiClient.get<ApiResponse<CommentResponse[]>>(
    `/api/community/posts/${postId}/comments`
  );
  return res.data.data;
}

/** 댓글 작성 */
export async function createComment(
  postId: number,
  request: CommentRequest
): Promise<CommentResponse> {
  const res = await apiClient.post<ApiResponse<CommentResponse>>(
    `/api/community/posts/${postId}/comments`,
    request
  );
  return res.data.data;
}

/** 댓글 수정 */
export async function updateComment(
  postId: number,
  commentId: number,
  content: string
): Promise<CommentResponse> {
  const res = await apiClient.put<ApiResponse<CommentResponse>>(
    `/api/community/posts/${postId}/comments/${commentId}`,
    { content }
  );
  return res.data.data;
}

/** 댓글 삭제 */
export async function deleteComment(
  postId: number,
  commentId: number
): Promise<void> {
  await apiClient.delete(`/api/community/posts/${postId}/comments/${commentId}`);
}
