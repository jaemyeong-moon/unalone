interface AlertBannerProps {
  message: string;
  variant?: 'error' | 'success';
}

export default function AlertBanner({ message, variant = 'error' }: AlertBannerProps) {
  const styles =
    variant === 'error'
      ? 'bg-red-50 text-red-600'
      : 'bg-emerald-50 text-emerald-600';

  return (
    <div className={`mb-4 p-3 rounded-lg text-sm ${styles}`}>
      {message}
    </div>
  );
}
