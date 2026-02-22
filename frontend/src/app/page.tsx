'use client';

import { useEffect, useState } from 'react';
import Header from '@/components/common/Header';

interface ServiceStatus {
  name: string;
  port: number;
  healthUrl: string;
  status: 'loading' | 'healthy' | 'unhealthy';
}

export default function Home() {
  const [services, setServices] = useState<ServiceStatus[]>([
    { name: 'API Service', port: 8080, healthUrl: '/api/health', status: 'loading' },
    { name: 'Admin Service', port: 8081, healthUrl: '/admin/api/health', status: 'loading' },
    { name: 'Event Service', port: 8082, healthUrl: '/api/events/health', status: 'loading' },
  ]);

  useEffect(() => {
    const checkHealth = async (index: number, url: string) => {
      try {
        const response = await fetch(url);
        if (response.ok) {
          setServices((prev) =>
            prev.map((s, i) => (i === index ? { ...s, status: 'healthy' as const } : s))
          );
        } else {
          setServices((prev) =>
            prev.map((s, i) => (i === index ? { ...s, status: 'unhealthy' as const } : s))
          );
        }
      } catch {
        setServices((prev) =>
          prev.map((s, i) => (i === index ? { ...s, status: 'unhealthy' as const } : s))
        );
      }
    };

    services.forEach((service, index) => {
      checkHealth(index, service.healthUrl);
    });

    const interval = setInterval(() => {
      services.forEach((service, index) => {
        checkHealth(index, service.healthUrl);
      });
    }, 30000);

    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'healthy':
        return 'bg-green-500';
      case 'unhealthy':
        return 'bg-red-500';
      default:
        return 'bg-yellow-500';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'healthy':
        return 'Running';
      case 'unhealthy':
        return 'Offline';
      default:
        return 'Checking...';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Service Dashboard</h1>
          <p className="mt-2 text-gray-600">Monitor the status of all running services.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {services.map((service) => (
            <div
              key={service.name}
              className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-gray-900">{service.name}</h2>
                <span
                  className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium text-white ${getStatusColor(service.status)}`}
                >
                  <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" />
                  {getStatusText(service.status)}
                </span>
              </div>
              <div className="space-y-2 text-sm text-gray-600">
                <div className="flex justify-between">
                  <span>Port</span>
                  <span className="font-mono font-medium text-gray-900">{service.port}</span>
                </div>
                <div className="flex justify-between">
                  <span>Health Endpoint</span>
                  <span className="font-mono text-xs text-gray-500">{service.healthUrl}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
