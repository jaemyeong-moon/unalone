'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import FormField from '@/components/common/FormField';
import ToggleSwitch from '@/components/common/ToggleSwitch';
import { isLoggedIn } from '@/lib/auth';
import { getCheckInSchedule, updateCheckInSchedule } from '@/lib/checkin';
import { getErrorMessage } from '@/lib/api';
import { CheckInScheduleRequest } from '@/types';

const DEFAULT_SETTINGS: CheckInScheduleRequest = {
  intervalHours: 24,
  activeStart: '08:00',
  activeEnd: '22:00',
  pushEnabled: true,
  emailEnabled: false,
  reminderBeforeMinutes: 15,
  warningAfterHours: 2,
  dangerAfterHours: 6,
  criticalAfterHours: 24,
  pauseUntil: null,
};

export default function CheckInSettingsPage() {
  const router = useRouter();
  const [settings, setSettings] = useState<CheckInScheduleRequest>(DEFAULT_SETTINGS);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [banner, setBanner] = useState<{ text: string; variant: 'success' | 'error' } | null>(null);
  const [pauseEnabled, setPauseEnabled] = useState(false);
  const [pauseDate, setPauseDate] = useState('');

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
      return;
    }
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      const data = await getCheckInSchedule();
      setSettings({
        intervalHours: data.intervalHours,
        activeStart: data.activeStart,
        activeEnd: data.activeEnd,
        pushEnabled: data.pushEnabled,
        emailEnabled: data.emailEnabled,
        reminderBeforeMinutes: data.reminderBeforeMinutes,
        warningAfterHours: data.warningAfterHours,
        dangerAfterHours: data.dangerAfterHours,
        criticalAfterHours: data.criticalAfterHours,
        pauseUntil: data.pauseUntil,
      });
      if (data.pauseUntil) {
        setPauseEnabled(true);
        setPauseDate(data.pauseUntil.slice(0, 10));
      }
    } catch {
      // Use defaults if settings not found
    } finally {
      setLoading(false);
    }
  };

  const showBanner = (text: string, variant: 'success' | 'error') => {
    setBanner({ text, variant });
    setTimeout(() => setBanner(null), 3000);
  };

  const handleSave = async () => {
    setSaving(true);
    setBanner(null);
    try {
      const request: CheckInScheduleRequest = {
        ...settings,
        pauseUntil: pauseEnabled && pauseDate ? pauseDate : null,
      };
      await updateCheckInSchedule(request);
      showBanner('설정이 저장되었습니다', 'success');
    } catch (error) {
      showBanner(getErrorMessage(error, '저장에 실패했습니다'), 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">체크인 설정</h1>
        <p className="text-sm text-gray-500 mt-1">체크인 알림과 에스컬레이션을 설정합니다</p>
      </div>

      {banner && <AlertBanner message={banner.text} variant={banner.variant} />}

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-6">
          {/* Schedule Settings */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-1">체크인 스케줄</h2>
            <p className="text-sm text-gray-500 mb-4">체크인 알림을 받을 시간과 주기를 설정합니다</p>

            <div className="space-y-4">
              <FormField
                label="체크인 주기 (시간)"
                type="number"
                min={1}
                max={72}
                value={settings.intervalHours}
                onChange={(e) => setSettings({ ...settings, intervalHours: Number(e.target.value) })}
                hint="1~72시간 사이로 설정"
              />
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  label="활동 시작 시간"
                  type="time"
                  value={settings.activeStart}
                  onChange={(e) => setSettings({ ...settings, activeStart: e.target.value })}
                />
                <FormField
                  label="활동 종료 시간"
                  type="time"
                  value={settings.activeEnd}
                  onChange={(e) => setSettings({ ...settings, activeEnd: e.target.value })}
                />
              </div>
              <p className="text-xs text-gray-400">
                활동 시간 외에는 알림이 발송되지 않습니다
              </p>
            </div>
          </div>

          {/* Reminder Settings */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-1">리마인더 알림</h2>
            <p className="text-sm text-gray-500 mb-4">체크인을 잊지 않도록 알림을 보내드립니다</p>

            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-900">푸시 알림</p>
                  <p className="text-xs text-gray-500">브라우저 푸시 알림 수신</p>
                </div>
                <ToggleSwitch
                  checked={settings.pushEnabled}
                  onChange={(v) => setSettings({ ...settings, pushEnabled: v })}
                  label="푸시 알림 토글"
                />
              </div>

              <div className="h-px bg-gray-100" />

              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-900">이메일 알림</p>
                  <p className="text-xs text-gray-500">이메일로 리마인더 수신</p>
                </div>
                <ToggleSwitch
                  checked={settings.emailEnabled}
                  onChange={(v) => setSettings({ ...settings, emailEnabled: v })}
                  label="이메일 알림 토글"
                />
              </div>

              <div className="h-px bg-gray-100" />

              <FormField
                label="미리 알림 (분)"
                type="number"
                min={5}
                max={60}
                value={settings.reminderBeforeMinutes}
                onChange={(e) => setSettings({ ...settings, reminderBeforeMinutes: Number(e.target.value) })}
                hint="체크인 예정 시간 N분 전에 알림 (5~60분)"
              />
            </div>
          </div>

          {/* Escalation Settings */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-1">에스컬레이션 설정</h2>
            <p className="text-sm text-gray-500 mb-4">미응답 시 보호자에게 단계적으로 알림을 보냅니다</p>

            <div className="space-y-3">
              {/* Step 1 - Warning */}
              <div className="flex items-start gap-3 p-3 rounded-lg bg-yellow-50 border border-yellow-200">
                <div className="w-8 h-8 rounded-full bg-yellow-400 text-white flex items-center justify-center text-sm font-bold shrink-0">1</div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">주의 (WARNING)</p>
                  <p className="text-xs text-gray-600 mt-0.5">체크인 미응답 시 본인에게 반복 알림</p>
                  <div className="mt-2">
                    <FormField
                      label=""
                      type="number"
                      min={1}
                      max={24}
                      value={settings.warningAfterHours}
                      onChange={(e) => setSettings({ ...settings, warningAfterHours: Number(e.target.value) })}
                      hint="미응답 후 N시간 경과 시"
                    />
                  </div>
                </div>
              </div>

              {/* Step 2 - Danger */}
              <div className="flex items-start gap-3 p-3 rounded-lg bg-orange-50 border border-orange-200">
                <div className="w-8 h-8 rounded-full bg-orange-500 text-white flex items-center justify-center text-sm font-bold shrink-0">2</div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">위험 (DANGER)</p>
                  <p className="text-xs text-gray-600 mt-0.5">보호자에게 알림 발송</p>
                  <div className="mt-2">
                    <FormField
                      label=""
                      type="number"
                      min={2}
                      max={48}
                      value={settings.dangerAfterHours}
                      onChange={(e) => setSettings({ ...settings, dangerAfterHours: Number(e.target.value) })}
                      hint="미응답 후 N시간 경과 시"
                    />
                  </div>
                </div>
              </div>

              {/* Step 3 - Critical */}
              <div className="flex items-start gap-3 p-3 rounded-lg bg-red-50 border border-red-200">
                <div className="w-8 h-8 rounded-full bg-red-600 text-white flex items-center justify-center text-sm font-bold shrink-0">3</div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">긴급 (CRITICAL)</p>
                  <p className="text-xs text-gray-600 mt-0.5">관리자 및 긴급 연락처에 알림</p>
                  <div className="mt-2">
                    <FormField
                      label=""
                      type="number"
                      min={4}
                      max={72}
                      value={settings.criticalAfterHours}
                      onChange={(e) => setSettings({ ...settings, criticalAfterHours: Number(e.target.value) })}
                      hint="미응답 후 N시간 경과 시"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Pause Check-in */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-1">체크인 일시정지</h2>
            <p className="text-sm text-gray-500 mb-4">여행 등의 이유로 체크인을 일시정지합니다</p>

            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-900">일시정지</p>
                  <p className="text-xs text-gray-500">정지 기간 동안 알림이 발송되지 않습니다</p>
                </div>
                <ToggleSwitch
                  checked={pauseEnabled}
                  onChange={setPauseEnabled}
                  label="체크인 일시정지 토글"
                />
              </div>

              {pauseEnabled && (
                <FormField
                  label="정지 종료일"
                  type="date"
                  value={pauseDate}
                  onChange={(e) => setPauseDate(e.target.value)}
                  hint="이 날짜까지 체크인이 일시정지됩니다"
                />
              )}
            </div>
          </div>

          {/* Save Button */}
          <button
            onClick={handleSave}
            disabled={saving}
            className="w-full py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {saving ? '저장 중...' : '설정 저장'}
          </button>
        </div>
      )}
    </PageLayout>
  );
}
