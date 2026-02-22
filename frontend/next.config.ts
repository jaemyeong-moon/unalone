import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
      {
        source: '/admin/api/:path*',
        destination: 'http://localhost:8081/admin/:path*',
      },
    ];
  },
};

export default nextConfig;
