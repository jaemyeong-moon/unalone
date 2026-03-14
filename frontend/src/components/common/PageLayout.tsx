import Header from '@/components/common/Header';

interface PageLayoutProps {
  children: React.ReactNode;
  /** max-width 제한 클래스 (기본: max-w-4xl) */
  maxWidth?: string;
}

export default function PageLayout({ children, maxWidth = 'max-w-4xl' }: PageLayoutProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className={`${maxWidth} mx-auto px-4 sm:px-6 lg:px-8 py-8`}>
        {children}
      </main>
    </div>
  );
}
