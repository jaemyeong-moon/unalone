'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import MoodSelector from '@/components/common/MoodSelector';
import { isLoggedIn } from '@/lib/auth';
import { createHealthJournal } from '@/lib/health';
import { getErrorMessage } from '@/lib/api';
import { MedicineEntry, MealRecord } from '@/types';

const SYMPTOMS = [
  { value: 'HEADACHE', label: '두통' },
  { value: 'DIZZINESS', label: '어지러움' },
  { value: 'FATIGUE', label: '피로감' },
  { value: 'INSOMNIA', label: '불면' },
  { value: 'CHEST_PAIN', label: '가슴 통증' },
  { value: 'JOINT_PAIN', label: '관절통' },
  { value: 'STOMACH', label: '소화불량' },
  { value: 'NAUSEA', label: '메스꺼움' },
  { value: 'COUGH', label: '기침' },
  { value: 'FEVER', label: '발열' },
  { value: 'SHORTNESS_OF_BREATH', label: '호흡곤란' },
  { value: 'NONE', label: '증상 없음' },
];

const MEAL_TYPES = [
  { value: 'breakfast' as const, label: '아침', icon: '\u{1F305}' },
  { value: 'lunch' as const, label: '점심', icon: '\u{2600}\u{FE0F}' },
  { value: 'dinner' as const, label: '저녁', icon: '\u{1F319}' },
];

interface FormState {
  mood: number | null;
  symptoms: string[];
  medicines: MedicineEntry[];
  meals: MealRecord;
  sleepHours: number | null;
  sleepQuality: number | null;
  exerciseMinutes: number | null;
  painLevel: number | null;
  note: string;
}

const initialForm: FormState = {
  mood: null,
  symptoms: [],
  medicines: [],
  meals: { breakfast: null, lunch: null, dinner: null },
  sleepHours: null,
  sleepQuality: null,
  exerciseMinutes: null,
  painLevel: null,
  note: '',
};

export default function NewHealthJournalPage() {
  const router = useRouter();
  const [form, setForm] = useState<FormState>(initialForm);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);

  const today = new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push('/login');
    }
  }, []);

  const toggleSymptom = (value: string) => {
    if (value === 'NONE') {
      setForm({ ...form, symptoms: form.symptoms.includes('NONE') ? [] : ['NONE'] });
      return;
    }
    const filtered = form.symptoms.filter((s) => s !== 'NONE');
    if (filtered.includes(value)) {
      setForm({ ...form, symptoms: filtered.filter((s) => s !== value) });
    } else {
      setForm({ ...form, symptoms: [...filtered, value] });
    }
  };

  const addMedicine = () => {
    setForm({ ...form, medicines: [...form.medicines, { name: '', taken: false }] });
  };

  const updateMedicine = (index: number, field: keyof MedicineEntry, value: string | boolean) => {
    const updated = [...form.medicines];
    updated[index] = { ...updated[index], [field]: value };
    setForm({ ...form, medicines: updated });
  };

  const removeMedicine = (index: number) => {
    setForm({ ...form, medicines: form.medicines.filter((_, i) => i !== index) });
  };

  const setMealStatus = (meal: keyof MealRecord, value: boolean) => {
    const current = form.meals[meal];
    setForm({
      ...form,
      meals: { ...form.meals, [meal]: current === value ? null : value },
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.mood) return;

    setSubmitting(true);
    setMessage('');
    try {
      await createHealthJournal({
        date: new Date().toISOString().slice(0, 10),
        mood: form.mood,
        symptoms: form.symptoms,
        medicines: form.medicines.filter((m) => m.name.trim()),
        meals: form.meals,
        sleepHours: form.sleepHours,
        sleepQuality: form.sleepQuality,
        exerciseMinutes: form.exerciseMinutes,
        painLevel: form.painLevel,
        note: form.note || undefined,
      });
      setMessage('일지가 저장되었습니다');
      setIsError(false);
      setTimeout(() => router.push('/health'), 1500);
    } catch (error) {
      setMessage(getErrorMessage(error, '저장에 실패했습니다'));
      setIsError(true);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <PageLayout maxWidth="max-w-2xl">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">건강 일지 작성</h1>
        <p className="text-sm text-gray-500 mt-1">{today} 기록</p>
      </div>

      {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Mood */}
        <MoodSelector value={form.mood} onChange={(v) => setForm({ ...form, mood: v })} />

        {/* Sleep */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">수면</h2>
          <p className="text-sm text-gray-500 mb-4">어젯밤 수면 상태를 기록해주세요</p>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                수면 시간: <span className="text-emerald-600 font-bold">{form.sleepHours ?? '-'}시간</span>
              </label>
              <input
                type="range"
                min={0}
                max={14}
                step={0.5}
                value={form.sleepHours ?? 7}
                onChange={(e) => setForm({ ...form, sleepHours: parseFloat(e.target.value) })}
                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-emerald-600"
              />
              <div className="flex justify-between text-xs text-gray-400 mt-1">
                <span>0시간</span>
                <span>7시간</span>
                <span>14시간</span>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">수면 질</label>
              <div className="flex gap-2">
                {[1, 2, 3, 4, 5].map((q) => (
                  <button
                    key={q}
                    type="button"
                    onClick={() => setForm({ ...form, sleepQuality: q })}
                    className={`flex-1 py-2 rounded-lg text-sm font-medium transition-colors
                      ${form.sleepQuality === q
                        ? 'bg-emerald-100 text-emerald-700 border border-emerald-300'
                        : 'bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200'
                      }`}
                    aria-pressed={form.sleepQuality === q}
                  >
                    {q === 1 ? '매우 나쁨' : q === 2 ? '나쁨' : q === 3 ? '보통' : q === 4 ? '좋음' : '매우 좋음'}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Meals */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">식사</h2>
          <p className="text-sm text-gray-500 mb-4">오늘의 식사를 기록해주세요</p>

          <div className="space-y-4">
            {MEAL_TYPES.map((meal) => (
              <div key={meal.value} className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="text-lg" aria-hidden="true">{meal.icon}</span>
                  <span className="text-sm font-medium text-gray-700">{meal.label}</span>
                </div>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setMealStatus(meal.value, true)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors
                      ${form.meals[meal.value] === true
                        ? 'bg-emerald-100 text-emerald-700 border border-emerald-300'
                        : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                      }`}
                    aria-pressed={form.meals[meal.value] === true}
                  >
                    먹었어요
                  </button>
                  <button
                    type="button"
                    onClick={() => setMealStatus(meal.value, false)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors
                      ${form.meals[meal.value] === false
                        ? 'bg-red-100 text-red-700 border border-red-300'
                        : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                      }`}
                    aria-pressed={form.meals[meal.value] === false}
                  >
                    건너뛰었어요
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Exercise */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">운동</h2>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              운동 시간: <span className="text-emerald-600 font-bold">{form.exerciseMinutes ?? 0}분</span>
            </label>
            <input
              type="range"
              min={0}
              max={180}
              step={5}
              value={form.exerciseMinutes ?? 0}
              onChange={(e) => setForm({ ...form, exerciseMinutes: parseInt(e.target.value) })}
              className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-emerald-600"
            />
            <div className="flex justify-between text-xs text-gray-400 mt-1">
              <span>0분</span>
              <span>90분</span>
              <span>180분</span>
            </div>
          </div>
        </div>

        {/* Symptoms */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">증상</h2>
          <p className="text-sm text-gray-500 mb-4">오늘 느끼는 증상을 선택해주세요</p>
          <div className="flex flex-wrap gap-2">
            {SYMPTOMS.map((symptom) => (
              <button
                key={symptom.value}
                type="button"
                onClick={() => toggleSymptom(symptom.value)}
                className={`inline-flex items-center gap-1.5 px-3 py-2 rounded-full text-sm font-medium transition-all duration-200
                  ${form.symptoms.includes(symptom.value)
                    ? symptom.value === 'NONE'
                      ? 'bg-emerald-100 text-emerald-700 border border-emerald-300'
                      : 'bg-red-100 text-red-700 border border-red-300'
                    : 'bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200'
                  }`}
                aria-pressed={form.symptoms.includes(symptom.value)}
              >
                {symptom.label}
              </button>
            ))}
          </div>
        </div>

        {/* Medications */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">복용 약물</h2>
              <p className="text-sm text-gray-500">오늘 복용한 약물을 기록해주세요</p>
            </div>
            <button
              type="button"
              onClick={addMedicine}
              className="text-sm text-emerald-600 hover:text-emerald-700 font-medium"
            >
              + 추가
            </button>
          </div>

          {form.medicines.length === 0 ? (
            <p className="text-sm text-gray-400 py-4 text-center">등록된 약물이 없습니다</p>
          ) : (
            <div className="space-y-3">
              {form.medicines.map((med, i) => (
                <div key={i} className="flex items-center gap-2">
                  <input
                    type="text"
                    value={med.name}
                    onChange={(e) => updateMedicine(i, 'name', e.target.value)}
                    placeholder="약물명"
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                  />
                  <label className="flex items-center gap-1.5 shrink-0">
                    <input
                      type="checkbox"
                      checked={med.taken}
                      onChange={(e) => updateMedicine(i, 'taken', e.target.checked)}
                      className="h-5 w-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500"
                    />
                    <span className="text-sm text-gray-600">복용</span>
                  </label>
                  <button
                    type="button"
                    onClick={() => removeMedicine(i)}
                    className="text-gray-400 hover:text-red-500 transition-colors"
                    aria-label="약물 삭제"
                  >
                    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Pain Level */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">통증 수준</h2>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              통증: <span className="text-emerald-600 font-bold">{form.painLevel ?? 0} / 10</span>
            </label>
            <input
              type="range"
              min={0}
              max={10}
              step={1}
              value={form.painLevel ?? 0}
              onChange={(e) => setForm({ ...form, painLevel: parseInt(e.target.value) })}
              className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-emerald-600"
            />
            <div className="flex justify-between text-xs text-gray-400 mt-1">
              <span>없음</span>
              <span>보통</span>
              <span>극심</span>
            </div>
          </div>
        </div>

        {/* Notes */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">메모</h2>
          <textarea
            value={form.note}
            onChange={(e) => setForm({ ...form, note: e.target.value })}
            rows={4}
            placeholder="오늘의 건강 상태에 대해 자유롭게 기록해주세요"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm resize-none"
          />
        </div>

        <button
          type="submit"
          disabled={submitting || !form.mood}
          className="w-full py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {submitting ? '저장 중...' : '일지 저장'}
        </button>
      </form>
    </PageLayout>
  );
}
