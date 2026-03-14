'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { getUser, isLoggedIn, logout } from '@/lib/auth';
import NotificationBell from '@/components/common/NotificationBell';

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const [user, setUser] = useState<{ name: string; role: string } | null>(null);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    if (isLoggedIn()) {
      setUser(getUser());
    }
  }, [pathname]);

  const handleLogout = () => {
    logout();
    setUser(null);
    router.push('/login');
  };

  const userLinks = [
    { href: '/', label: '홈' },
    ...(user ? [
      { href: '/checkin', label: '체크인' },
      { href: '/health', label: '건강일지' },
      { href: '/care', label: '이웃 돌봄' },
      { href: '/community', label: '커뮤니티' },
      { href: '/guardians', label: '보호자' },
      { href: '/profile', label: '프로필' },
    ] : [
      { href: '/community', label: '커뮤니티' },
    ]),
    ...(user?.role === 'ROLE_ADMIN' ? [{ href: '/admin', label: '관리자' }] : []),
  ];

  return (
    <header className="bg-emerald-700 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link href="/" className="text-xl font-bold tracking-tight flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            Unalone
          </Link>

          {/* Desktop Nav */}
          <div className="hidden md:flex items-center gap-4">
            <nav className="flex items-center gap-1">
              {userLinks.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    pathname === link.href
                      ? 'bg-emerald-800 text-white'
                      : 'text-emerald-100 hover:text-white hover:bg-emerald-600'
                  }`}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
            {user ? (
              <div className="flex items-center gap-2 ml-2">
                <NotificationBell />
                <span className="text-sm text-emerald-200">{user.name}</span>
                <button
                  onClick={handleLogout}
                  className="px-3 py-1.5 rounded-lg text-sm bg-emerald-900 hover:bg-emerald-950 transition-colors"
                >
                  로그아웃
                </button>
              </div>
            ) : (
              <Link
                href="/login"
                className="px-4 py-2 rounded-lg text-sm font-medium bg-white text-emerald-700 hover:bg-emerald-50 transition-colors"
              >
                로그인
              </Link>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="md:hidden p-2 rounded-lg hover:bg-emerald-600"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              {menuOpen ? (
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              ) : (
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              )}
            </svg>
          </button>
        </div>

        {/* Mobile Nav */}
        {menuOpen && (
          <div className="md:hidden pb-4 space-y-1">
            {userLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                onClick={() => setMenuOpen(false)}
                className={`block px-4 py-2 rounded-lg text-sm font-medium ${
                  pathname === link.href
                    ? 'bg-emerald-800 text-white'
                    : 'text-emerald-100 hover:bg-emerald-600'
                }`}
              >
                {link.label}
              </Link>
            ))}
            {user && (
              <Link
                href="/notifications"
                onClick={() => setMenuOpen(false)}
                className={`block px-4 py-2 rounded-lg text-sm font-medium ${
                  pathname === '/notifications'
                    ? 'bg-emerald-800 text-white'
                    : 'text-emerald-100 hover:bg-emerald-600'
                }`}
              >
                알림
              </Link>
            )}
            {user ? (
              <button
                onClick={handleLogout}
                className="w-full text-left px-4 py-2 rounded-lg text-sm text-emerald-100 hover:bg-emerald-600"
              >
                로그아웃
              </button>
            ) : (
              <Link
                href="/login"
                onClick={() => setMenuOpen(false)}
                className="block px-4 py-2 rounded-lg text-sm font-medium text-emerald-100 hover:bg-emerald-600"
              >
                로그인
              </Link>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
