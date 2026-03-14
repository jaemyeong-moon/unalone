/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  async rewrites() {
    // In Docker mode with Traefik, NEXT_PUBLIC_API_URL is empty so no rewrites needed.
    // Traefik handles routing /api/* to the correct backend services.
    // Rewrites are only used for local development without Traefik.
    const apiUrl = process.env.NEXT_PUBLIC_API_URL;
    const adminUrl = process.env.NEXT_PUBLIC_ADMIN_API_URL;

    if (!apiUrl && !adminUrl) {
      return [];
    }

    const apiBase = apiUrl || 'http://localhost:8080';
    const adminBase = adminUrl || 'http://localhost:8081';

    return [
      // Check-in schedule & escalations
      {
        source: '/api/checkin/schedule',
        destination: `${apiBase}/api/checkin/schedule`,
      },
      {
        source: '/api/checkin/escalations',
        destination: `${apiBase}/api/checkin/escalations`,
      },
      // Health journals & trends
      {
        source: '/api/health/:path*',
        destination: `${apiBase}/api/health/:path*`,
      },
      // Neighbor care
      {
        source: '/api/care/:path*',
        destination: `${apiBase}/api/care/:path*`,
      },
      // Admin API (must be before general API catch-all)
      {
        source: '/api/admin/:path*',
        destination: `${adminBase}/api/admin/:path*`,
      },
      // General API catch-all (must be after specific routes)
      {
        source: '/api/:path*',
        destination: `${apiBase}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
