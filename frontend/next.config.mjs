/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  async rewrites() {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    const adminUrl = process.env.NEXT_PUBLIC_ADMIN_API_URL || 'http://localhost:8081';
    return [
      {
        source: '/api/:path*',
        destination: `${apiUrl}/api/:path*`,
      },
      {
        source: '/admin/api/:path*',
        destination: `${adminUrl}/admin/:path*`,
      },
    ];
  },
};

export default nextConfig;
