'use client';

import Link from 'next/link';

interface FloatingActionButtonProps {
  href: string;
  label: string;
}

export default function FloatingActionButton({ href, label }: FloatingActionButtonProps) {
  return (
    <Link
      href={href}
      className="fixed bottom-6 right-6 z-40 w-14 h-14 bg-emerald-600 text-white rounded-full shadow-lg
        flex items-center justify-center hover:bg-emerald-700 transition-colors
        sm:hidden"
      aria-label={label}
    >
      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
      </svg>
    </Link>
  );
}
