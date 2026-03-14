'use client';

import { useState } from 'react';

interface RatingStarsProps {
  initialRating?: number;
  onSubmit: (rating: number, review: string) => Promise<void>;
  disabled?: boolean;
}

export default function RatingStars({ initialRating = 0, onSubmit, disabled = false }: RatingStarsProps) {
  const [rating, setRating] = useState(initialRating);
  const [hoverRating, setHoverRating] = useState(0);
  const [review, setReview] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (rating === 0 || submitting) return;
    setSubmitting(true);
    try {
      await onSubmit(rating, review);
    } finally {
      setSubmitting(false);
    }
  };

  const displayRating = hoverRating || rating;

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Star rating */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">평점</label>
        <div className="flex gap-1">
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              type="button"
              disabled={disabled}
              onMouseEnter={() => setHoverRating(star)}
              onMouseLeave={() => setHoverRating(0)}
              onClick={() => setRating(star)}
              className="p-0.5 transition-transform hover:scale-110 disabled:cursor-not-allowed"
              aria-label={`${star}점`}
            >
              <svg
                className={`h-8 w-8 transition-colors ${
                  star <= displayRating ? 'text-yellow-400' : 'text-gray-300'
                }`}
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </button>
          ))}
          {rating > 0 && (
            <span className="ml-2 text-sm text-gray-500 self-center">{rating}점</span>
          )}
        </div>
      </div>

      {/* Review textarea */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">후기</label>
        <textarea
          value={review}
          onChange={(e) => setReview(e.target.value)}
          disabled={disabled}
          rows={3}
          placeholder="방문에 대한 후기를 남겨주세요"
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent disabled:bg-gray-50 disabled:cursor-not-allowed"
        />
      </div>

      {/* Submit button */}
      <button
        type="submit"
        disabled={rating === 0 || submitting || disabled}
        className="w-full py-2.5 bg-emerald-600 text-white rounded-lg font-medium hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        {submitting ? '제출 중...' : '평가 제출'}
      </button>
    </form>
  );
}
