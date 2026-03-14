'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import PageLayout from '@/components/common/PageLayout';
import AlertBanner from '@/components/common/AlertBanner';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import MoodSelector from '@/components/common/MoodSelector';
import { isLoggedIn } from '@/lib/auth';
import { getHealthJournal, updateHealthJournal, deleteHealthJournal } from '@/lib/health';
import { getErrorMessage } from '@/lib/api';
import { HealthJournalResponse, MedicineEntry, MealRecord } from '@/types';

const MOOD_EMOJI: Record<number, string> = {
  1: '\u{1F622}', 2: '\u{1F61F}', 3: '\u{1F610}', 4: '\u{1F642}', 5: '\u{1F604}',
};

const MOOD_LABEL: Record<number, string> = {
  1: '매우 나쁨', 2: '나쁨', 3: '보통', 4: '좋음', 5: '매우 좋음',
};

const SYMPTOM_LABELS: Record<string, string> = {
  HEADACHE: '두통', DIZZINESS: '어지러움', FATIGUE: '피로감', INSOMNIA: '불면',
  CHEST_PAIN: '가슴 통증', JOINT_PAIN: '관절통', STOMACH: '소화불량', NAUSEA: '메스꺼움',
  COUGH: '기침', FEVER: '발열', SHORTNESS_OF_BREATH: '호흡곤란', NONE: '증상 없음',
};

const MEAL_LABELS: Record<string, string> = {
  breakfast: '아침', lunch: '점심', dinner: '저녁',
};

const MEAL_ICONS: Record<string, string> = {
  breakfast: '\u{1F305}', lunch: '\u{2600}\u{FE0F}', dinner: '\u{1F319}',
};

const SYMPTOMS = [
  { value: 'HEADACHE', label: '두통' }, { value: 'DIZZINESS', label: '어지러움' },
  { value: 'FATIGUE', label: '피로감' }, { value: 'INSOMNIA', label: '불면' },
  { value: 'CHEST_PAIN', label: '가슴 통증' }, { value: 'JOINT_PAIN', label: '관절통' },
  { value: 'STOMACH', label: '소화불량' }, { value: 'NAUSEA', label: '메스꺼움' },
  { value: 'COUGH', label: '기침' }, { value: 'FEVER', label: '발열' },
  { value: 'SHORTNESS_OF_BREATH', label: '호흡곤란' }, { value: 'NONE', label: '증상 없음' },
];

const MEAL_TYPES = [
  { value: 'breakfast' as const, label: '아침', icon: '\u{1F305}' },
  { value: 'lunch' as const, label: '점심', icon: '\u{2600}\u{FE0F}' },
  { value: 'dinner' as const, label: '저녁', icon: '\u{1F319}' },
];

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'short',
  });
}

export default function HealthJournalDetailPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);

  const [journal, setJournal] = useState<HealthJournalResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [banner, setBanner] = useState<{ text: string; variant: 'success' | 'error' } | null>(null);

  // Edit form state
  const [editMood, setEditMood] = useState<number | null>(null);
  const [editSymptoms, setEditSymptoms] = useState<string[]>([]);
  const [editMedicines, setEditMedicines] = useState<MedicineEntry[]>([]);
  const [editMeals, setEditMeals] = useState<MealRecord>({ breakfast: null, lunch: null, dinner: null });
  const [editSleepHours, setEditSleepHours] = useState<number | null>(null);
  const [editSleepQuality, setEditSleepQuality] = useState<number | null>(null);
  const [editExerciseMinutes, setEditExerciseMinutes] = useState<number | null>(null);
  const [editPainLevel, setEditPainLevel] = useState<number | null>(null);
  const [editNote, setEditNote] = useState('');

  useEffect(() => {
    if (!isLoggedIn()) { router.push('/login'); return; }
    fetchJournal();
  }, [id]);

  const fetchJournal = async () => {
    try {
      const data = await getHealthJournal(id);
      setJournal(data);
      populateEditForm(data);
    } catch (err) {
      setBanner({ text: getErrorMessage(err, '일지를 불러올 수 없습니다'), variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const populateEditForm = (data: HealthJournalResponse) => {
    setEditMood(data.mood);
    setEditSymptoms(data.symptoms);
    setEditMedicines(data.medicines);
    setEditMeals(data.meals);
    setEditSleepHours(data.sleepHours);
    setEditSleepQuality(data.sleepQuality);
    setEditExerciseMinutes(data.exerciseMinutes);
    setEditPainLevel(data.painLevel);
    setEditNote(data.note || '');
  };

  const handleCancelEdit = () => {
    if (journal) populateEditForm(journal);
    setEditing(false);
  };

  const handleSave = async () => {
    if (!editMood || !journal) return;
    setSaving(true);
    setBanner(null);
    try {
      const updated = await updateHealthJournal(id, {
        date: journal.date,
        mood: editMood,
        symptoms: editSymptoms,
        medicines: editMedicines.filter((m) => m.name.trim()),
        meals: editMeals,
        sleepHours: editSleepHours,
        sleepQuality: editSleepQuality,
        exerciseMinutes: editExerciseMinutes,
        painLevel: editPainLevel,
        note: editNote || undefined,
      });
      setJournal(updated);
      setEditing(false);
      setBanner({ text: '일지가 수정되었습니다', variant: 'success' });
      setTimeout(() => setBanner(null), 3000);
    } catch (error) {
      setBanner({ text: getErrorMessage(error, '수정에 실패했습니다'), variant: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteHealthJournal(id);
      router.push('/health');
    } catch (error) {
      setBanner({ text: getErrorMessage(error, '삭제에 실패했습니다'), variant: 'error' });
      setDeleting(false);
      setShowDeleteConfirm(false);
    }
  };

  const toggleSymptom = (value: string) => {
    if (value === 'NONE') {
      setEditSymptoms(editSymptoms.includes('NONE') ? [] : ['NONE']);
      return;
    }
    const filtered = editSymptoms.filter((s) => s !== 'NONE');
    if (filtered.includes(value)) {
      setEditSymptoms(filtered.filter((s) => s !== value));
    } else {
      setEditSymptoms([...filtered, value]);
    }
  };

  const setMealStatus = (meal: keyof MealRecord, value: boolean) => {
    const current = editMeals[meal];
    setEditMeals({ ...editMeals, [meal]: current === value ? null : value });
  };

  const addMedicine = () => {
    setEditMedicines([...editMedicines, { name: '', taken: false }]);
  };

  const updateMedicine = (index: number, field: keyof MedicineEntry, value: string | boolean) => {
    const updated = [...editMedicines];
    updated[index] = { ...updated[index], [field]: value };
    setEditMedicines(updated);
  };

  const removeMedicine = (index: number) => {
    setEditMedicines(editMedicines.filter((_, i) => i !== index));
  };

  return (
    <PageLayout maxWidth="max-w-2xl">
      {banner && <AlertBanner message={banner.text} variant={banner.variant} />}

      {loading ? (
        <LoadingSpinner />
      ) : !journal ? (
        <div className="text-center py-16">
          <p className="text-gray-500 mb-4">일지를 찾을 수 없습니다</p>
          <Link href="/health" className="text-emerald-600 hover:text-emerald-700 font-medium">
            목록으로 돌아가기
          </Link>
        </div>
      ) : (
        <>
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                {editing ? '건강 일지 수정' : '건강 일지'}
              </h1>
              <p className="text-sm text-gray-500 mt-1">{formatDate(journal.date)}</p>
            </div>
            {!editing && (
              <div className="flex gap-2">
                <button
                  onClick={() => setEditing(true)}
                  className="px-4 py-2 text-sm font-medium text-emerald-600 border border-emerald-300 rounded-lg hover:bg-emerald-50 transition-colors"
                >
                  수정
                </button>
                <button
                  onClick={() => setShowDeleteConfirm(true)}
                  className="px-4 py-2 text-sm font-medium text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors"
                >
                  삭제
                </button>
              </div>
            )}
          </div>

          {/* Delete Confirmation */}
          {showDeleteConfirm && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl">
              <p className="text-sm text-red-800 font-medium mb-3">정말로 이 일지를 삭제하시겠습니까?</p>
              <div className="flex gap-2">
                <button
                  onClick={handleDelete}
                  disabled={deleting}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50 transition-colors"
                >
                  {deleting ? '삭제 중...' : '삭제'}
                </button>
                <button
                  onClick={() => setShowDeleteConfirm(false)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm hover:bg-gray-50 transition-colors"
                >
                  취소
                </button>
              </div>
            </div>
          )}

          {editing ? (
            /* EDIT MODE */
            <div className="space-y-6">
              <MoodSelector value={editMood} onChange={setEditMood} />

              {/* Sleep edit */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">수면</h2>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      수면 시간: <span className="text-emerald-600 font-bold">{editSleepHours ?? '-'}시간</span>
                    </label>
                    <input type="range" min={0} max={14} step={0.5} value={editSleepHours ?? 7}
                      onChange={(e) => setEditSleepHours(parseFloat(e.target.value))}
                      className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-emerald-600" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">수면 질</label>
                    <div className="flex gap-2">
                      {[1, 2, 3, 4, 5].map((q) => (
                        <button key={q} type="button" onClick={() => setEditSleepQuality(q)}
                          className={`flex-1 py-2 rounded-lg text-sm font-medium transition-colors
                            ${editSleepQuality === q ? 'bg-emerald-100 text-emerald-700 border border-emerald-300' : 'bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200'}`}
                          aria-pressed={editSleepQuality === q}>
                          {q === 1 ? '매우 나쁨' : q === 2 ? '나쁨' : q === 3 ? '보통' : q === 4 ? '좋음' : '매우 좋음'}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              {/* Meals edit */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">식사</h2>
                <div className="space-y-4">
                  {MEAL_TYPES.map((meal) => (
                    <div key={meal.value} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="text-lg" aria-hidden="true">{meal.icon}</span>
                        <span className="text-sm font-medium text-gray-700">{meal.label}</span>
                      </div>
                      <div className="flex gap-2">
                        <button type="button" onClick={() => setMealStatus(meal.value, true)}
                          className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${editMeals[meal.value] === true ? 'bg-emerald-100 text-emerald-700 border border-emerald-300' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'}`}
                          aria-pressed={editMeals[meal.value] === true}>먹었어요</button>
                        <button type="button" onClick={() => setMealStatus(meal.value, false)}
                          className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${editMeals[meal.value] === false ? 'bg-red-100 text-red-700 border border-red-300' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'}`}
                          aria-pressed={editMeals[meal.value] === false}>건너뛰었어요</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Symptoms edit */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">증상</h2>
                <div className="flex flex-wrap gap-2">
                  {SYMPTOMS.map((symptom) => (
                    <button key={symptom.value} type="button" onClick={() => toggleSymptom(symptom.value)}
                      className={`inline-flex items-center gap-1.5 px-3 py-2 rounded-full text-sm font-medium transition-all duration-200
                        ${editSymptoms.includes(symptom.value)
                          ? symptom.value === 'NONE' ? 'bg-emerald-100 text-emerald-700 border border-emerald-300' : 'bg-red-100 text-red-700 border border-red-300'
                          : 'bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200'}`}
                      aria-pressed={editSymptoms.includes(symptom.value)}>
                      {symptom.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Medicines edit */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-lg font-semibold text-gray-900">복용 약물</h2>
                  <button type="button" onClick={addMedicine} className="text-sm text-emerald-600 hover:text-emerald-700 font-medium">+ 추가</button>
                </div>
                {editMedicines.length === 0 ? (
                  <p className="text-sm text-gray-400 py-4 text-center">등록된 약물이 없습니다</p>
                ) : (
                  <div className="space-y-3">
                    {editMedicines.map((med, i) => (
                      <div key={i} className="flex items-center gap-2">
                        <input type="text" value={med.name} onChange={(e) => updateMedicine(i, 'name', e.target.value)} placeholder="약물명"
                          className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent" />
                        <label className="flex items-center gap-1.5 shrink-0">
                          <input type="checkbox" checked={med.taken} onChange={(e) => updateMedicine(i, 'taken', e.target.checked)}
                            className="h-5 w-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
                          <span className="text-sm text-gray-600">복용</span>
                        </label>
                        <button type="button" onClick={() => removeMedicine(i)} className="text-gray-400 hover:text-red-500 transition-colors" aria-label="약물 삭제">
                          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Pain edit */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">통증 수준</h2>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  통증: <span className="text-emerald-600 font-bold">{editPainLevel ?? 0} / 10</span>
                </label>
                <input type="range" min={0} max={10} step={1} value={editPainLevel ?? 0}
                  onChange={(e) => setEditPainLevel(parseInt(e.target.value))}
                  className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-emerald-600" />
              </div>

              {/* Note edit */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">메모</h2>
                <textarea value={editNote} onChange={(e) => setEditNote(e.target.value)} rows={4} placeholder="오늘의 건강 상태에 대해 자유롭게 기록해주세요"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm resize-none" />
              </div>

              <div className="flex gap-3">
                <button onClick={handleSave} disabled={saving || !editMood}
                  className="flex-1 py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                  {saving ? '저장 중...' : '수정 저장'}
                </button>
                <button onClick={handleCancelEdit}
                  className="px-6 py-3 border border-gray-300 text-gray-700 rounded-xl font-medium hover:bg-gray-50 transition-colors">
                  취소
                </button>
              </div>
            </div>
          ) : (
            /* VIEW MODE */
            <div className="space-y-6">
              {/* Mood */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
                <span className="text-5xl block mb-2" aria-hidden="true">{MOOD_EMOJI[journal.mood]}</span>
                <p className="text-lg font-semibold text-gray-900">{MOOD_LABEL[journal.mood]}</p>
              </div>

              {/* Sleep */}
              {(journal.sleepHours !== null || journal.sleepQuality !== null) && (
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-3">수면</h2>
                  <div className="grid grid-cols-2 gap-4">
                    {journal.sleepHours !== null && (
                      <div>
                        <p className="text-xs text-gray-500">수면 시간</p>
                        <p className="text-lg font-bold text-gray-900">{journal.sleepHours}시간</p>
                      </div>
                    )}
                    {journal.sleepQuality !== null && (
                      <div>
                        <p className="text-xs text-gray-500">수면 질</p>
                        <p className="text-lg font-bold text-gray-900">
                          {journal.sleepQuality === 1 ? '매우 나쁨' : journal.sleepQuality === 2 ? '나쁨' : journal.sleepQuality === 3 ? '보통' : journal.sleepQuality === 4 ? '좋음' : '매우 좋음'}
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Meals */}
              <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-3">식사</h2>
                <div className="space-y-3">
                  {(['breakfast', 'lunch', 'dinner'] as const).map((meal) => (
                    <div key={meal} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span aria-hidden="true">{MEAL_ICONS[meal]}</span>
                        <span className="text-sm text-gray-700">{MEAL_LABELS[meal]}</span>
                      </div>
                      <span className={`text-sm font-medium ${journal.meals[meal] === true ? 'text-emerald-600' : journal.meals[meal] === false ? 'text-red-600' : 'text-gray-400'}`}>
                        {journal.meals[meal] === true ? '먹었어요' : journal.meals[meal] === false ? '건너뛰었어요' : '미기록'}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Exercise */}
              {journal.exerciseMinutes !== null && (
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-3">운동</h2>
                  <p className="text-lg font-bold text-gray-900">{journal.exerciseMinutes}분</p>
                </div>
              )}

              {/* Symptoms */}
              {journal.symptoms.length > 0 && (
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-3">증상</h2>
                  <div className="flex flex-wrap gap-2">
                    {journal.symptoms.map((s) => (
                      <span key={s} className={`inline-flex px-3 py-1.5 rounded-full text-sm font-medium ${s === 'NONE' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'}`}>
                        {SYMPTOM_LABELS[s] || s}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Medicines */}
              {journal.medicines.length > 0 && (
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-3">복용 약물</h2>
                  <div className="space-y-2">
                    {journal.medicines.map((med, i) => (
                      <div key={i} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                        <span className="text-sm text-gray-700">{med.name}</span>
                        <span className={`text-sm font-medium ${med.taken ? 'text-emerald-600' : 'text-gray-400'}`}>
                          {med.taken ? '복용 완료' : '미복용'}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Pain Level */}
              {journal.painLevel !== null && journal.painLevel > 0 && (
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-3">통증 수준</h2>
                  <div className="flex items-center gap-3">
                    <div className="flex-1 bg-gray-200 rounded-full h-3 overflow-hidden">
                      <div
                        className="bg-red-400 h-full rounded-full transition-all duration-500"
                        style={{ width: `${(journal.painLevel / 10) * 100}%` }}
                        role="progressbar"
                        aria-valuenow={journal.painLevel}
                        aria-valuemin={0}
                        aria-valuemax={10}
                      />
                    </div>
                    <span className="text-lg font-bold text-gray-900 shrink-0">{journal.painLevel}/10</span>
                  </div>
                </div>
              )}

              {/* Note */}
              {journal.note && (
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-3">메모</h2>
                  <p className="text-sm text-gray-600 whitespace-pre-wrap">{journal.note}</p>
                </div>
              )}

              <div className="text-center">
                <Link href="/health" className="text-sm text-gray-500 hover:text-gray-700 font-medium">
                  목록으로 돌아가기
                </Link>
              </div>
            </div>
          )}
        </>
      )}
    </PageLayout>
  );
}
