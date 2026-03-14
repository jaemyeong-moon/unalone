# 건강 일지 & 감정 추적 화면 설계

## 개요
사용자가 매일의 기분, 건강 상태(증상, 복용 약물, 식사)를 기록하고, 주간/월간 트렌드 차트를 통해 패턴을 파악할 수 있는 기능이다.
대시보드에 건강 요약 카드를 추가하여 한눈에 상태를 확인할 수 있도록 한다.

## 영향받는 페이지
| 페이지 | 경로 | 변경 유형 |
|--------|------|-----------|
| 건강 일지 메인 | `/health` | 신규 페이지 |
| 일지 작성 | `/health/new` | 신규 페이지 |
| 일지 상세 | `/health/[id]` | 신규 페이지 |
| 트렌드 차트 | `/health/trends` | 신규 페이지 |
| 홈 대시보드 | `/` | 건강 요약 카드 추가 |

---

## 1. 건강 일지 메인 페이지 (`/health`)

### 레이아웃
`PageLayout` 사용, `max-w-2xl`. 오늘의 기분 입력 영역과 최근 일지 목록을 보여준다.

```
[Header - 공통]
[페이지 타이틀 + 트렌드 보기 링크]
[오늘의 기분 MoodDailySelector]
[주간 기분 미니 차트]
[최근 건강 일지 목록]
[일지 작성 FAB (모바일)]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| MoodDailySelector | 오늘의 기분 5단계 선택 | `value`, `onChange`, `date` |
| WeeklyMoodMiniChart | 최근 7일 기분 미니 바 차트 | `data` |
| HealthJournalList | 일지 목록 | `items` |
| HealthJournalListItem | 개별 일지 항목 | `journal` |
| FloatingActionButton | 일지 작성 FAB | `href`, `label` |

#### 1-1. MoodDailySelector

오늘 날짜를 표시하고 5단계 기분을 선택한다. 이미 선택된 경우 변경 가능.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <div className="flex items-center justify-between mb-4">
    <div>
      <h2 className="text-lg font-semibold text-gray-900">오늘의 기분</h2>
      <p className="text-sm text-gray-500">{formatDate(date)}</p>
    </div>
    {value && (
      <span className="text-xs text-emerald-600 font-medium bg-emerald-50 px-2 py-1 rounded-full">
        기록 완료
      </span>
    )}
  </div>

  <div className="flex justify-between gap-2 sm:gap-4">
    {MOOD_LEVELS.map((mood) => (
      <button
        key={mood.value}
        onClick={() => onChange(mood.value)}
        className={`flex-1 flex flex-col items-center gap-1.5 py-3 px-2 rounded-xl border-2 transition-all duration-200
          ${value === mood.value
            ? `${mood.selectedBg} ${mood.selectedBorder} scale-105`
            : 'border-transparent hover:bg-gray-50'
          }`}
        aria-label={`기분: ${mood.label}`}
        aria-pressed={value === mood.value}
      >
        <span className="text-3xl sm:text-4xl" role="img" aria-hidden="true">{mood.emoji}</span>
        <span className="text-xs text-gray-600 font-medium">{mood.label}</span>
      </button>
    ))}
  </div>
</div>
```

기분 단계:
| 값 | 라벨 | 이모지 | 선택 배경 | 선택 테두리 |
|----|------|--------|----------|------------|
| 1 | 매우 나쁨 | 😢 | `bg-red-50` | `border-red-400` |
| 2 | 나쁨 | 😟 | `bg-orange-50` | `border-orange-400` |
| 3 | 보통 | 😐 | `bg-yellow-50` | `border-yellow-400` |
| 4 | 좋음 | 😊 | `bg-emerald-50` | `border-emerald-400` |
| 5 | 매우 좋음 | 😄 | `bg-blue-50` | `border-blue-400` |

#### 1-2. WeeklyMoodMiniChart

최근 7일간의 기분을 세로 바 차트로 표현한다. CSS만으로 구현 가능한 단순 차트.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <div className="flex items-center justify-between mb-4">
    <h2 className="text-base font-semibold text-gray-900">이번 주 기분</h2>
    <Link href="/health/trends" className="text-sm text-emerald-600 hover:text-emerald-700 font-medium">
      상세 보기
    </Link>
  </div>

  <div className="flex items-end justify-between gap-2 h-24" role="img" aria-label="이번 주 기분 차트">
    {data.map((day) => (
      <div key={day.date} className="flex-1 flex flex-col items-center gap-1">
        <div
          className={`w-full rounded-t-md transition-all duration-300 ${getMoodBarColor(day.mood)}`}
          style={{ height: `${day.mood ? (day.mood / 5) * 100 : 0}%` }}
          aria-label={`${day.label}: ${day.mood ? getMoodLabel(day.mood) : '기록 없음'}`}
        />
        <span className="text-xs text-gray-400">{day.label}</span>
      </div>
    ))}
  </div>

  <div className="flex justify-between mt-2 text-xs text-gray-400">
    <span>나쁨</span>
    <span>좋음</span>
  </div>
</div>
```

기분 바 색상:
| 기분 값 | 색상 |
|---------|------|
| 1 | `bg-red-400` |
| 2 | `bg-orange-400` |
| 3 | `bg-yellow-400` |
| 4 | `bg-emerald-400` |
| 5 | `bg-blue-400` |
| 없음 | `bg-gray-200` |

#### 1-3. HealthJournalListItem

```tsx
<Link
  href={`/health/${journal.id}`}
  className="block bg-white rounded-lg border border-gray-200 p-4 hover:shadow-sm transition-shadow"
>
  <div className="flex items-start gap-3">
    {/* 기분 이모지 */}
    <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${getMoodBg(journal.mood)}`}>
      <span className="text-lg" aria-hidden="true">{getMoodEmoji(journal.mood)}</span>
    </div>

    <div className="flex-1 min-w-0">
      <div className="flex items-center justify-between mb-1">
        <span className="text-sm font-medium text-gray-900">{formatDate(journal.date)}</span>
        <span className="text-xs text-gray-400">{getMoodLabel(journal.mood)}</span>
      </div>

      {/* 태그 요약 */}
      {journal.tags.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-1.5">
          {journal.tags.slice(0, 3).map((tag) => (
            <span key={tag} className="inline-flex px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-500">
              {tag}
            </span>
          ))}
          {journal.tags.length > 3 && (
            <span className="text-xs text-gray-400">+{journal.tags.length - 3}</span>
          )}
        </div>
      )}

      {journal.note && (
        <p className="text-sm text-gray-500 line-clamp-1">{journal.note}</p>
      )}
    </div>
  </div>
</Link>
```

#### 1-4. FloatingActionButton

모바일에서 우하단에 고정 표시되는 일지 작성 버튼.

```tsx
<Link
  href="/health/new"
  className="fixed bottom-6 right-6 z-40 w-14 h-14 bg-emerald-600 text-white rounded-full shadow-lg
    flex items-center justify-center hover:bg-emerald-700 transition-colors
    sm:hidden"
  aria-label="건강 일지 작성"
>
  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
  </svg>
</Link>
```

### 상태 처리
| 상태 | UI |
|------|-----|
| 로딩 | `LoadingSpinner` 중앙 |
| 일지 없음 | 빈 상태 아이콘 + "아직 건강 일지가 없습니다" + 작성 버튼 |
| 오늘 기분 미선택 | `MoodDailySelector` 기본 상태 (선택 전) |
| 오늘 기분 선택 완료 | 기분 아이콘 강조 + "기록 완료" 배지 |

---

## 2. 건강 일지 작성 (`/health/new`)

### 레이아웃
`PageLayout` 사용, `max-w-2xl`.

```
[Header - 공통]
[페이지 타이틀]
[기분 선택 섹션]
[증상 기록 섹션]
[복용 약물 섹션]
[식사 기록 섹션]
[메모 섹션]
[저장 버튼]
```

### 마크업 구조

```tsx
<PageLayout maxWidth="max-w-2xl">
  <div className="mb-6">
    <h1 className="text-2xl font-bold text-gray-900">건강 일지 작성</h1>
    <p className="text-sm text-gray-500 mt-1">{formatDate(today)} 기록</p>
  </div>

  {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

  <form onSubmit={handleSubmit} className="space-y-6">
    {/* 기분 선택 */}
    <MoodDailySelector value={form.mood} onChange={(v) => setForm({ ...form, mood: v })} date={today} />

    {/* 증상 기록 */}
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
                ? 'bg-red-100 text-red-700 border border-red-300'
                : 'bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200'
              }`}
            aria-pressed={form.symptoms.includes(symptom.value)}
          >
            {symptom.label}
          </button>
        ))}
      </div>
    </div>

    {/* 복용 약물 */}
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

    {/* 식사 기록 */}
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

    {/* 메모 */}
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
```

증상 목록:
```typescript
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
```

식사 유형:
```typescript
const MEAL_TYPES = [
  { value: 'breakfast', label: '아침', icon: '🌅' },
  { value: 'lunch', label: '점심', icon: '☀️' },
  { value: 'dinner', label: '저녁', icon: '🌙' },
];
```

### 상태 처리
| 상태 | UI |
|------|-----|
| 기분 미선택 | 저장 버튼 `disabled` |
| 저장 중 | 버튼 "저장 중..." + 폼 비활성 |
| 저장 성공 | `AlertBanner variant="success"` + `/health` 이동 |
| 저장 실패 | `AlertBanner variant="error"` |
| 기존 일지 수정 | 제목 "건강 일지 수정", 기존 데이터 폼에 채움 |

---

## 3. 트렌드 차트 (`/health/trends`)

### 레이아웃
`PageLayout` 사용, `max-w-4xl`.

```
[Header - 공통]
[페이지 타이틀 + 기간 탭 (주간/월간)]
[기분 트렌드 라인 차트]
[건강 지표 요약 카드 그리드]
[증상 빈도 바 차트]
[식사 이행률 차트]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| PeriodTab | 주간/월간 전환 탭 | `value`, `onChange` |
| MoodTrendChart | 기분 트렌드 라인/바 차트 | `data`, `period` |
| HealthStatCards | 건강 지표 요약 카드 | `stats` |
| SymptomFrequencyChart | 증상 빈도 가로 바 차트 | `data` |
| MealComplianceChart | 식사 이행률 차트 | `data` |

#### 3-1. PeriodTab

```tsx
<div className="flex gap-2 mb-6">
  {[
    { value: 'weekly', label: '주간' },
    { value: 'monthly', label: '월간' },
  ].map((tab) => (
    <button
      key={tab.value}
      onClick={() => onChange(tab.value)}
      className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
        ${value === tab.value
          ? 'bg-emerald-600 text-white'
          : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
        }`}
      aria-pressed={value === tab.value}
    >
      {tab.label}
    </button>
  ))}
</div>
```

#### 3-2. MoodTrendChart

CSS 기반 단순 차트. 복잡한 차트가 필요한 경우 Chart.js 또는 recharts 도입을 검토.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <h2 className="text-lg font-semibold text-gray-900 mb-4">기분 변화</h2>

  {/* Y축 라벨 + 차트 영역 */}
  <div className="flex gap-2">
    {/* Y축 */}
    <div className="flex flex-col justify-between text-xs text-gray-400 py-1 w-12 shrink-0">
      <span>매우 좋음</span>
      <span>좋음</span>
      <span>보통</span>
      <span>나쁨</span>
      <span>매우 나쁨</span>
    </div>

    {/* 차트 */}
    <div className="flex-1 relative h-48 border-l border-b border-gray-200">
      {/* 수평 그리드 라인 */}
      {[1, 2, 3, 4].map((i) => (
        <div
          key={i}
          className="absolute w-full border-t border-gray-100"
          style={{ bottom: `${(i / 5) * 100}%` }}
        />
      ))}

      {/* 데이터 포인트 + 연결선 */}
      <svg className="absolute inset-0 w-full h-full" preserveAspectRatio="none">
        <polyline
          points={data.map((d, i) => `${(i / (data.length - 1)) * 100}%,${100 - ((d.mood || 3) / 5) * 100}%`).join(' ')}
          fill="none"
          stroke="#10b981"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>

      {/* 데이터 점 */}
      <div className="absolute inset-0 flex justify-between items-end">
        {data.map((d, i) => (
          <div
            key={i}
            className="relative flex flex-col items-center"
            style={{ bottom: `${((d.mood || 3) / 5) * 100}%` }}
          >
            <div
              className={`w-3 h-3 rounded-full border-2 border-white shadow-sm ${getMoodDotColor(d.mood)}`}
              aria-label={`${d.label}: ${getMoodLabel(d.mood)}`}
            />
          </div>
        ))}
      </div>
    </div>
  </div>

  {/* X축 라벨 */}
  <div className="flex justify-between pl-14 mt-2">
    {data.map((d) => (
      <span key={d.date} className="text-xs text-gray-400">{d.label}</span>
    ))}
  </div>
</div>
```

#### 3-3. HealthStatCards

4개의 요약 카드를 그리드로 배치.

```tsx
<div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
  {/* 평균 기분 */}
  <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
    <p className="text-xs text-gray-500 mb-1">평균 기분</p>
    <div className="flex items-center gap-2">
      <span className="text-2xl" aria-hidden="true">{getAverageMoodEmoji(stats.avgMood)}</span>
      <span className="text-xl font-bold text-gray-900">{stats.avgMood.toFixed(1)}</span>
    </div>
    <p className={`text-xs mt-1 ${stats.moodTrend >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
      {stats.moodTrend >= 0 ? '+' : ''}{stats.moodTrend.toFixed(1)} 지난 주 대비
    </p>
  </div>

  {/* 체크인율 */}
  <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
    <p className="text-xs text-gray-500 mb-1">기록률</p>
    <p className="text-xl font-bold text-gray-900">{stats.journalRate}%</p>
    <p className="text-xs text-gray-400 mt-1">{stats.journalDays}/{stats.totalDays}일 기록</p>
  </div>

  {/* 식사 이행률 */}
  <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
    <p className="text-xs text-gray-500 mb-1">식사 이행률</p>
    <p className="text-xl font-bold text-gray-900">{stats.mealRate}%</p>
    <div className="w-full bg-gray-200 rounded-full h-1.5 mt-2">
      <div
        className="bg-emerald-500 h-1.5 rounded-full transition-all duration-500"
        style={{ width: `${stats.mealRate}%` }}
        role="progressbar"
        aria-valuenow={stats.mealRate}
        aria-valuemin={0}
        aria-valuemax={100}
      />
    </div>
  </div>

  {/* 주요 증상 */}
  <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
    <p className="text-xs text-gray-500 mb-1">가장 많은 증상</p>
    <p className="text-base font-bold text-gray-900">{stats.topSymptom || '없음'}</p>
    <p className="text-xs text-gray-400 mt-1">{stats.topSymptomCount}회 기록</p>
  </div>
</div>
```

#### 3-4. SymptomFrequencyChart

가로 바 차트로 증상 빈도를 시각화한다.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <h2 className="text-lg font-semibold text-gray-900 mb-4">증상 빈도</h2>

  {data.length === 0 ? (
    <p className="text-sm text-gray-400 py-4 text-center">기록된 증상이 없습니다</p>
  ) : (
    <div className="space-y-3">
      {data.map((item) => (
        <div key={item.symptom} className="flex items-center gap-3">
          <span className="text-sm text-gray-700 w-20 shrink-0 text-right">{item.label}</span>
          <div className="flex-1 bg-gray-100 rounded-full h-5 overflow-hidden">
            <div
              className="bg-red-400 h-full rounded-full transition-all duration-500 flex items-center justify-end pr-2"
              style={{ width: `${(item.count / maxCount) * 100}%`, minWidth: item.count > 0 ? '2rem' : '0' }}
            >
              {item.count > 0 && (
                <span className="text-xs text-white font-medium">{item.count}</span>
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  )}
</div>
```

#### 3-5. MealComplianceChart

식사별 이행률을 도넛 또는 바 형태로 표시한다.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <h2 className="text-lg font-semibold text-gray-900 mb-4">식사 이행률</h2>

  <div className="grid grid-cols-3 gap-4">
    {data.map((meal) => (
      <div key={meal.type} className="text-center">
        {/* 원형 프로그레스 */}
        <div className="relative w-20 h-20 mx-auto mb-2">
          <svg className="w-20 h-20 -rotate-90" viewBox="0 0 36 36">
            <circle
              cx="18" cy="18" r="15.91549431"
              fill="none" stroke="#e5e7eb" strokeWidth="3"
            />
            <circle
              cx="18" cy="18" r="15.91549431"
              fill="none" stroke="#10b981" strokeWidth="3"
              strokeDasharray={`${meal.rate} ${100 - meal.rate}`}
              strokeLinecap="round"
              role="progressbar"
              aria-valuenow={meal.rate}
              aria-valuemin={0}
              aria-valuemax={100}
            />
          </svg>
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="text-sm font-bold text-gray-900">{meal.rate}%</span>
          </div>
        </div>
        <p className="text-sm font-medium text-gray-700">{meal.label}</p>
        <p className="text-xs text-gray-400">{meal.completed}/{meal.total}일</p>
      </div>
    ))}
  </div>
</div>
```

### 상태 처리
| 상태 | UI |
|------|-----|
| 로딩 | `LoadingSpinner` 중앙 |
| 데이터 없음 | 각 차트 영역에 "데이터가 부족합니다. 일지를 작성해주세요" + 작성 링크 |
| 에러 | `AlertBanner variant="error"` + 재시도 버튼 |
| 기간 전환 | 로딩 스피너 없이 데이터 교체 (로컬 캐시 활용) |

---

## 4. 대시보드 건강 요약 카드 (`/` 페이지 추가)

홈 대시보드의 체크인 카드와 빠른 메뉴 사이에 건강 요약 카드를 삽입한다.

### 마크업 구조

```tsx
{/* 건강 요약 카드 - 대시보드에 추가 */}
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <div className="flex items-center justify-between mb-4">
    <h2 className="text-lg font-semibold text-gray-900">건강 요약</h2>
    <Link href="/health/trends" className="text-sm text-emerald-600 hover:text-emerald-700 font-medium">
      상세 보기
    </Link>
  </div>

  <div className="grid grid-cols-3 gap-4">
    {/* 오늘 기분 */}
    <div className="text-center">
      <span className="text-3xl block mb-1" aria-hidden="true">
        {todayMood ? getMoodEmoji(todayMood) : '❓'}
      </span>
      <p className="text-xs text-gray-500">
        {todayMood ? '오늘 기분' : '기분 미입력'}
      </p>
    </div>

    {/* 주간 평균 */}
    <div className="text-center">
      <span className="text-2xl font-bold text-gray-900 block mb-1">
        {weeklyAvg?.toFixed(1) ?? '-'}
      </span>
      <p className="text-xs text-gray-500">주간 평균</p>
    </div>

    {/* 기록 연속 */}
    <div className="text-center">
      <span className="text-2xl font-bold text-emerald-600 block mb-1">
        {streak ?? 0}일
      </span>
      <p className="text-xs text-gray-500">연속 기록</p>
    </div>
  </div>

  {/* 주간 미니 차트 */}
  <div className="mt-4 pt-4 border-t border-gray-100">
    <div className="flex items-end justify-between gap-1.5 h-12">
      {weeklyData.map((day) => (
        <div
          key={day.date}
          className={`flex-1 rounded-t transition-all duration-300 ${getMoodBarColor(day.mood)}`}
          style={{ height: `${day.mood ? (day.mood / 5) * 100 : 10}%` }}
          aria-label={`${day.label}: ${day.mood ? getMoodLabel(day.mood) : '기록 없음'}`}
        />
      ))}
    </div>
    <div className="flex justify-between mt-1">
      {weeklyData.map((day) => (
        <span key={day.date} className="flex-1 text-center text-[10px] text-gray-400">
          {day.label}
        </span>
      ))}
    </div>
  </div>
</div>
```

---

## 5. 인터랙션 플로우

### 5-1. 일일 기분 기록 플로우
```
사용자: /health 접근
  -> MoodDailySelector에서 기분 선택
  -> POST /api/health/mood { date, mood }
  -> 즉시 반영: "기록 완료" 배지 표시
  -> 주간 미니 차트 업데이트
```

### 5-2. 건강 일지 작성 플로우
```
사용자: /health/new 접근 (또는 FAB 클릭)
  -> 기분 선택 (필수)
  -> 증상 태그 선택 (선택)
  -> 약물 추가/삭제 + 복용 여부 체크 (선택)
  -> 식사 기록 (선택)
  -> 메모 작성 (선택)
  -> "일지 저장" 클릭
  -> POST /api/health/journals { mood, symptoms, medicines, meals, note }
  -> 성공: /health 이동
```

### 5-3. 트렌드 확인 플로우
```
사용자: /health/trends 접근
  -> 기본 주간(weekly) 탭 활성
  -> GET /api/health/trends?period=weekly
  -> 기분 차트, 건강 지표, 증상 빈도, 식사 이행률 표시
  -> 월간(monthly) 탭 클릭 시 기간 전환
  -> GET /api/health/trends?period=monthly
```

---

## 6. 접근성 (A11y)

| 항목 | 적용 내용 |
|------|-----------|
| 기분 선택 | `aria-pressed`, `aria-label`로 기분 상태 전달. 이모지에 `aria-hidden="true"` |
| 차트 | `role="img"` + `aria-label`로 차트 요약 텍스트 제공 |
| 프로그레스바 | `role="progressbar"`, `aria-valuenow/min/max` 적용 |
| 증상 칩 | `aria-pressed`로 선택 상태 전달 |
| 식사 버튼 | `aria-pressed`로 선택 상태 전달 |
| FAB | `aria-label="건강 일지 작성"` |
| 색상 | 차트 데이터 구분을 색상 외에 라벨 텍스트로도 제공 |
| 키보드 | 모든 버튼/입력 Tab 탐색 가능 |

---

## 7. 반응형 브레이크포인트별 대응

| 브레이크포인트 | 변화 |
|---------------|------|
| 모바일 (< 640px) | 기분 이모지 `text-3xl`, 지표 카드 `grid-cols-2`, FAB 표시, 차트 간소화 |
| 태블릿 (>= 640px) | 기분 이모지 `text-4xl`, FAB 숨김 + 인라인 작성 버튼, 차트 풀 사이즈 |
| 데스크탑 (>= 1024px) | 지표 카드 `grid-cols-4`, 차트 여유 있는 높이 |

---

## 8. Tailwind 클래스 가이드

### 카드 기본
```
bg-white rounded-xl shadow-sm border border-gray-200 p-6
```

### 기분 선택 버튼 (미선택/선택)
```
미선택: border-2 border-transparent hover:bg-gray-50
선택:   border-2 bg-{color}-50 border-{color}-400 scale-105
```

### 증상 칩 (미선택/선택)
```
미선택: bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200
선택:   bg-red-100 text-red-700 border border-red-300
```

### 식사 버튼 (미선택/선택-먹음/선택-안먹음)
```
미선택: bg-gray-100 text-gray-500 hover:bg-gray-200
먹음:   bg-emerald-100 text-emerald-700 border border-emerald-300
안먹음: bg-red-100 text-red-700 border border-red-300
```

### 지표 카드
```
bg-white rounded-xl shadow-sm border border-gray-200 p-4
text-xs text-gray-500 (라벨)
text-xl font-bold text-gray-900 (값)
```

### 수평 바 차트
```
bg-gray-100 rounded-full h-5 overflow-hidden  (배경)
bg-red-400 h-full rounded-full                (바)
```

### 원형 프로그레스
```
SVG 기반: r=15.91549431 (둘레 100), strokeDasharray로 비율 표현
```

### FAB
```
fixed bottom-6 right-6 z-40 w-14 h-14 bg-emerald-600 text-white rounded-full shadow-lg
sm:hidden
```

---

## 9. tailwind.config.ts 변경사항

별도 커스텀 설정 추가 불필요. 기존 Tailwind 유틸리티로 구현 가능.
향후 차트 라이브러리 도입 시 해당 라이브러리의 설정이 추가될 수 있다.

---

## 10. 프론트엔드 팀 전달사항

### 신규 파일
| 파일 | 설명 |
|------|------|
| `src/app/health/page.tsx` | 건강 일지 메인 |
| `src/app/health/new/page.tsx` | 일지 작성 |
| `src/app/health/[id]/page.tsx` | 일지 상세 |
| `src/app/health/trends/page.tsx` | 트렌드 차트 |
| `src/components/health/MoodDailySelector.tsx` | 기분 선택기 |
| `src/components/health/WeeklyMoodMiniChart.tsx` | 주간 미니 차트 |
| `src/components/health/HealthJournalListItem.tsx` | 일지 목록 항목 |
| `src/components/health/MoodTrendChart.tsx` | 기분 트렌드 차트 |
| `src/components/health/HealthStatCards.tsx` | 건강 지표 카드 |
| `src/components/health/SymptomFrequencyChart.tsx` | 증상 빈도 차트 |
| `src/components/health/MealComplianceChart.tsx` | 식사 이행률 차트 |
| `src/components/common/FloatingActionButton.tsx` | FAB 공통 컴포넌트 |

### 수정 파일
| 파일 | 변경 내용 |
|------|-----------|
| `src/app/page.tsx` | 대시보드에 건강 요약 카드 추가, 빠른 메뉴에 `/health` 링크 추가 |
| `src/components/common/Header.tsx` | 네비게이션에 "건강 일지" 메뉴 추가 |
| `src/types/index.ts` | 건강 관련 타입 추가 (`HealthJournal`, `MoodEntry`, `HealthTrend`, `Symptom` 등) |

### 백엔드 팀 전달사항
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/health/mood` | 일일 기분 기록 (`{ date, mood }`) |
| GET | `/api/health/mood/weekly` | 최근 7일 기분 데이터 |
| POST | `/api/health/journals` | 건강 일지 생성 |
| GET | `/api/health/journals` | 건강 일지 목록 (페이징) |
| GET | `/api/health/journals/{id}` | 건강 일지 상세 |
| PUT | `/api/health/journals/{id}` | 건강 일지 수정 |
| DELETE | `/api/health/journals/{id}` | 건강 일지 삭제 |
| GET | `/api/health/trends` | 트렌드 데이터 (쿼리: period=weekly\|monthly) |
| GET | `/api/health/summary` | 대시보드용 건강 요약 (평균 기분, 기록률, 연속일, 주간 데이터) |

### 차트 라이브러리 검토
현재 설계는 CSS + SVG 기반 단순 차트로 구현했으나, 다음 상황에서 Chart.js 또는 recharts 도입을 권장한다:
- 데이터 포인트가 30개 이상인 월간 차트
- 줌/드래그 인터랙션이 필요한 경우
- 복잡한 복합 차트(기분+증상 오버레이 등)가 필요한 경우
