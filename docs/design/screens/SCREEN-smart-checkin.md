# 스마트 체크인 알림 시스템 화면 설계

## 개요
기존 단순 텍스트 기반 체크인을 확장하여, 기분/건강 상태 빠른 선택, 체크인 히스토리 타임라인, 알림 스케줄 설정, 리마인더 토스트/배너 UI를 제공한다.
고령 사용자가 최소 터치로 체크인을 완료할 수 있도록 큰 터치 영역과 직관적 아이콘을 사용한다.

## 영향받는 페이지
| 페이지 | 경로 | 변경 유형 |
|--------|------|-----------|
| 홈 대시보드 | `/` | 기존 체크인 카드 확장 |
| 스마트 체크인 | `/checkin` | 신규 페이지 |
| 체크인 히스토리 | `/checkin/history` | 신규 페이지 |
| 알림 설정 | `/checkin/settings` | 신규 페이지 |

---

## 1. 스마트 체크인 페이지 (`/checkin`)

### 레이아웃
`PageLayout` 컴포넌트를 사용하며, `max-w-2xl` 중앙 정렬. 상단에서 하단으로 세 개 섹션이 순차 배치된다.

```
[Header - 공통]
[페이지 타이틀 + 서브텍스트]
[기분 선택 섹션 - MoodSelector]
[건강 상태 퀵 선택 - HealthQuickSelect]
[메시지 입력 (선택)]
[체크인 버튼]
[최근 체크인 요약 카드]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| MoodSelector | 5단계 기분 이모지 선택기 | `value`, `onChange` |
| HealthQuickSelect | 건강 상태 칩 다중 선택 | `selected`, `onChange` |
| CheckInSubmitButton | 체크인 제출 버튼 | `loading`, `disabled`, `onClick` |
| RecentCheckInSummary | 마지막 체크인 요약 카드 | `checkIn` |

#### 1-1. MoodSelector

5단계 기분을 원형 버튼으로 나열한다. 선택 시 테두리 강조.

```tsx
interface MoodSelectorProps {
  value: number | null;       // 1~5 (1: 매우 나쁨, 5: 매우 좋음)
  onChange: (mood: number) => void;
}
```

기분 단계 정의:
| 값 | 라벨 | 이모지 | 선택 시 배경 |
|----|------|--------|-------------|
| 1 | 매우 나쁨 | 😢 | `bg-red-100 border-red-400` |
| 2 | 나쁨 | 😟 | `bg-orange-100 border-orange-400` |
| 3 | 보통 | 😐 | `bg-yellow-100 border-yellow-400` |
| 4 | 좋음 | 😊 | `bg-emerald-100 border-emerald-400` |
| 5 | 매우 좋음 | 😄 | `bg-blue-100 border-blue-400` |

마크업 구조:

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <h2 className="text-lg font-semibold text-gray-900 mb-2">오늘 기분은 어떠세요?</h2>
  <p className="text-sm text-gray-500 mb-4">가장 가까운 표정을 선택해주세요</p>
  <div className="flex justify-between gap-2 sm:gap-4">
    {moods.map((mood) => (
      <button
        key={mood.value}
        onClick={() => onChange(mood.value)}
        className={`flex-1 flex flex-col items-center gap-1.5 py-3 px-2 rounded-xl border-2 transition-all duration-200
          ${value === mood.value
            ? `${mood.selectedBg} ${mood.selectedBorder}`
            : 'border-transparent hover:bg-gray-50'
          }`}
        aria-label={mood.label}
        aria-pressed={value === mood.value}
      >
        <span className="text-3xl sm:text-4xl" role="img" aria-hidden="true">{mood.emoji}</span>
        <span className="text-xs text-gray-600 font-medium">{mood.label}</span>
      </button>
    ))}
  </div>
</div>
```

#### 1-2. HealthQuickSelect

건강 관련 상태를 칩(chip) 형태로 다중 선택한다.

```tsx
interface HealthQuickSelectProps {
  selected: string[];
  onChange: (selected: string[]) => void;
}
```

기본 옵션 목록:
| 라벨 | 값 | 아이콘(선택) |
|------|-----|-------------|
| 잘 잤어요 | `SLEPT_WELL` | 🌙 |
| 식사 했어요 | `ATE_MEAL` | 🍚 |
| 운동 했어요 | `EXERCISED` | 🏃 |
| 약 먹었어요 | `TOOK_MEDICINE` | 💊 |
| 외출 했어요 | `WENT_OUT` | 🚶 |
| 통증 있어요 | `IN_PAIN` | 🤕 |
| 기운 없어요 | `LOW_ENERGY` | 😮‍💨 |

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <h2 className="text-lg font-semibold text-gray-900 mb-2">오늘의 건강 상태</h2>
  <p className="text-sm text-gray-500 mb-4">해당하는 항목을 모두 선택해주세요</p>
  <div className="flex flex-wrap gap-2">
    {options.map((option) => (
      <button
        key={option.value}
        onClick={() => toggleOption(option.value)}
        className={`inline-flex items-center gap-1.5 px-4 py-2.5 rounded-full text-sm font-medium transition-all duration-200
          ${isSelected(option.value)
            ? 'bg-emerald-100 text-emerald-700 border border-emerald-300'
            : 'bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200'
          }`}
        aria-pressed={isSelected(option.value)}
      >
        <span aria-hidden="true">{option.icon}</span>
        {option.label}
      </button>
    ))}
  </div>
</div>
```

#### 1-3. 메시지 입력 + 체크인 버튼

기존 대시보드의 체크인 폼을 확장한다.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <label className="block text-sm font-medium text-gray-700 mb-2">
    하고 싶은 말 <span className="text-gray-400 font-normal">(선택)</span>
  </label>
  <textarea
    value={message}
    onChange={(e) => setMessage(e.target.value)}
    placeholder="오늘 하루는 어떠셨나요?"
    rows={3}
    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent text-sm resize-none"
  />
  <button
    onClick={handleCheckIn}
    disabled={checkingIn || !mood}
    className="w-full mt-4 py-4 bg-emerald-600 text-white rounded-xl font-semibold text-lg hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
    aria-label="체크인 하기"
  >
    {checkingIn ? (
      <span className="flex items-center justify-center gap-2">
        <LoadingSpinner size="sm" />
        체크인 중...
      </span>
    ) : '체크인 하기'}
  </button>
</div>
```

### 반응형 대응
| 브레이크포인트 | 변화 |
|---------------|------|
| 모바일 (< 640px) | 기분 이모지 `text-3xl`, 칩 2줄 이상 wrap, 버튼 `py-4` 유지 (터치 영역 확보) |
| 태블릿 이상 (>= 640px) | 기분 이모지 `text-4xl`, 칩 한 줄 또는 2줄, 카드 패딩 `p-6` |

### 상태 처리
| 상태 | UI |
|------|-----|
| 로딩 | `LoadingSpinner` 전체 영역 중앙 |
| 기분 미선택 | 체크인 버튼 `disabled`, 상단에 안내 텍스트 |
| 체크인 진행 중 | 버튼 내 스피너 + "체크인 중..." 텍스트, 모든 입력 비활성 |
| 체크인 성공 | `AlertBanner variant="success"` + 자동 대시보드 이동 (2초 후) |
| 체크인 실패 | `AlertBanner variant="error"` + 재시도 가능 |
| 오늘 이미 체크인 | 상단에 완료 배지 표시, 재체크인 가능 안내 |

---

## 2. 체크인 히스토리 타임라인 (`/checkin/history`)

### 레이아웃
`PageLayout` 사용, `max-w-2xl`. 상단에 기간 필터, 아래에 타임라인 목록.

```
[Header - 공통]
[페이지 타이틀 + 기간 필터 (7일/30일/전체)]
[타임라인 리스트]
[더보기 버튼 또는 무한 스크롤]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| PeriodFilter | 기간 선택 탭 | `value`, `onChange`, `options` |
| CheckInTimeline | 날짜별 그룹핑된 타임라인 | `items` |
| CheckInTimelineItem | 개별 체크인 항목 | `checkIn` |

#### 2-1. PeriodFilter

```tsx
<div className="flex gap-2 mb-6">
  {periods.map((period) => (
    <button
      key={period.value}
      onClick={() => onChange(period.value)}
      className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
        ${value === period.value
          ? 'bg-emerald-600 text-white'
          : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
        }`}
      aria-pressed={value === period.value}
    >
      {period.label}
    </button>
  ))}
</div>
```

기간 옵션: `7일`, `30일`, `전체`

#### 2-2. CheckInTimeline

날짜별로 그룹핑하여 왼쪽에 세로선(타임라인) UI를 표시한다.

```tsx
<div className="space-y-8">
  {groupedByDate.map(([date, items]) => (
    <div key={date}>
      <h3 className="text-sm font-semibold text-gray-500 mb-3 sticky top-0 bg-gray-50 py-1">
        {formatDate(date)}
      </h3>
      <div className="relative pl-6 border-l-2 border-gray-200 space-y-4">
        {items.map((item) => (
          <CheckInTimelineItem key={item.id} checkIn={item} />
        ))}
      </div>
    </div>
  ))}
</div>
```

#### 2-3. CheckInTimelineItem

```tsx
<div className="relative">
  {/* 타임라인 점 */}
  <div className={`absolute -left-[25px] top-1 w-3 h-3 rounded-full border-2 border-white
    ${getMoodColor(item.mood)}`} />

  <div className="bg-white rounded-lg border border-gray-200 p-4">
    <div className="flex items-center justify-between mb-2">
      <div className="flex items-center gap-2">
        <span className="text-xl" aria-hidden="true">{getMoodEmoji(item.mood)}</span>
        <span className="text-sm font-medium text-gray-900">{getMoodLabel(item.mood)}</span>
      </div>
      <span className="text-xs text-gray-400">{formatTime(item.checkedAt)}</span>
    </div>

    {/* 건강 상태 칩 */}
    {item.healthTags?.length > 0 && (
      <div className="flex flex-wrap gap-1.5 mb-2">
        {item.healthTags.map((tag) => (
          <span key={tag} className="inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-600">
            {getTagLabel(tag)}
          </span>
        ))}
      </div>
    )}

    {item.message && (
      <p className="text-sm text-gray-600">{item.message}</p>
    )}
  </div>
</div>
```

기분별 타임라인 점 색상:
| 기분 | 색상 클래스 |
|------|-----------|
| 1 (매우 나쁨) | `bg-red-500` |
| 2 (나쁨) | `bg-orange-500` |
| 3 (보통) | `bg-yellow-500` |
| 4 (좋음) | `bg-emerald-500` |
| 5 (매우 좋음) | `bg-blue-500` |

### 상태 처리
| 상태 | UI |
|------|-----|
| 로딩 | `LoadingSpinner` 중앙 |
| 데이터 없음 | 빈 상태 일러스트 + "아직 체크인 기록이 없습니다" + 체크인 페이지 링크 버튼 |
| 에러 | `AlertBanner variant="error"` + 재시도 버튼 |
| 더보기 로딩 | 목록 하단에 소형 스피너 |

---

## 3. 알림 설정 페이지 (`/checkin/settings`)

### 레이아웃
`PageLayout` 사용, `max-w-2xl`. 카드 형태로 설정 섹션을 구분한다.

```
[Header - 공통]
[페이지 타이틀]
[체크인 스케줄 설정 카드]
[리마인더 설정 카드]
[에스컬레이션 설정 카드]
[저장 버튼]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| ScheduleSettingsCard | 체크인 스케줄 설정 | `settings`, `onChange` |
| ReminderSettingsCard | 리마인더 알림 설정 | `settings`, `onChange` |
| EscalationSettingsCard | 에스컬레이션 단계 설정 | `settings`, `onChange` |
| ToggleSwitch | 토글 스위치 컴포넌트 | `checked`, `onChange`, `label` |

#### 3-1. ScheduleSettingsCard

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <h2 className="text-lg font-semibold text-gray-900 mb-1">체크인 스케줄</h2>
  <p className="text-sm text-gray-500 mb-4">체크인 알림을 받을 시간과 주기를 설정합니다</p>

  <div className="space-y-4">
    <FormField
      label="체크인 주기"
      type="number"
      min={1} max={72}
      value={settings.intervalHours}
      onChange={(e) => onChange({ ...settings, intervalHours: Number(e.target.value) })}
      hint="1~72시간 사이로 설정"
    />
    <div className="grid grid-cols-2 gap-4">
      <FormField
        label="활동 시작 시간"
        type="time"
        value={settings.activeStart}
        onChange={(e) => onChange({ ...settings, activeStart: e.target.value })}
      />
      <FormField
        label="활동 종료 시간"
        type="time"
        value={settings.activeEnd}
        onChange={(e) => onChange({ ...settings, activeEnd: e.target.value })}
      />
    </div>
    <p className="text-xs text-gray-400">
      활동 시간 외에는 알림이 발송되지 않습니다
    </p>
  </div>
</div>
```

#### 3-2. ReminderSettingsCard

```tsx
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
        onChange={(v) => onChange({ ...settings, pushEnabled: v })}
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
        onChange={(v) => onChange({ ...settings, emailEnabled: v })}
        label="이메일 알림 토글"
      />
    </div>

    <div className="h-px bg-gray-100" />

    <FormField
      label="미리 알림 (분)"
      type="number"
      min={5} max={60}
      value={settings.reminderBeforeMinutes}
      onChange={(e) => onChange({ ...settings, reminderBeforeMinutes: Number(e.target.value) })}
      hint="체크인 예정 시간 N분 전에 알림 (5~60분)"
    />
  </div>
</div>
```

#### 3-3. EscalationSettingsCard

미응답 시 에스컬레이션 단계를 시각적으로 보여준다.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <h2 className="text-lg font-semibold text-gray-900 mb-1">에스컬레이션 설정</h2>
  <p className="text-sm text-gray-500 mb-4">미응답 시 보호자에게 단계적으로 알림을 보냅니다</p>

  <div className="space-y-3">
    {/* 단계 1 */}
    <div className="flex items-start gap-3 p-3 rounded-lg bg-yellow-50 border border-yellow-200">
      <div className="w-8 h-8 rounded-full bg-yellow-400 text-white flex items-center justify-center text-sm font-bold shrink-0">1</div>
      <div className="flex-1">
        <p className="text-sm font-medium text-gray-900">주의 (WARNING)</p>
        <p className="text-xs text-gray-600 mt-0.5">체크인 미응답 시 본인에게 반복 알림</p>
        <FormField
          label=""
          type="number" min={1} max={24}
          value={settings.warningAfterHours}
          onChange={(e) => onChange({ ...settings, warningAfterHours: Number(e.target.value) })}
          hint="미응답 후 N시간 경과 시"
        />
      </div>
    </div>

    {/* 단계 2 */}
    <div className="flex items-start gap-3 p-3 rounded-lg bg-orange-50 border border-orange-200">
      <div className="w-8 h-8 rounded-full bg-orange-500 text-white flex items-center justify-center text-sm font-bold shrink-0">2</div>
      <div className="flex-1">
        <p className="text-sm font-medium text-gray-900">위험 (DANGER)</p>
        <p className="text-xs text-gray-600 mt-0.5">보호자에게 알림 발송</p>
        <FormField
          label=""
          type="number" min={2} max={48}
          value={settings.dangerAfterHours}
          onChange={(e) => onChange({ ...settings, dangerAfterHours: Number(e.target.value) })}
          hint="미응답 후 N시간 경과 시"
        />
      </div>
    </div>

    {/* 단계 3 */}
    <div className="flex items-start gap-3 p-3 rounded-lg bg-red-50 border border-red-200">
      <div className="w-8 h-8 rounded-full bg-red-600 text-white flex items-center justify-center text-sm font-bold shrink-0">3</div>
      <div className="flex-1">
        <p className="text-sm font-medium text-gray-900">긴급 (CRITICAL)</p>
        <p className="text-xs text-gray-600 mt-0.5">관리자 및 긴급 연락처에 알림</p>
        <FormField
          label=""
          type="number" min={4} max={72}
          value={settings.criticalAfterHours}
          onChange={(e) => onChange({ ...settings, criticalAfterHours: Number(e.target.value) })}
          hint="미응답 후 N시간 경과 시"
        />
      </div>
    </div>
  </div>
</div>
```

#### 3-4. ToggleSwitch (공통 컴포넌트)

**파일 위치**: `frontend/src/components/common/ToggleSwitch.tsx`

```tsx
interface ToggleSwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label: string;       // aria-label 용
  disabled?: boolean;
}
```

```tsx
<button
  type="button"
  role="switch"
  aria-checked={checked}
  aria-label={label}
  onClick={() => !disabled && onChange(!checked)}
  disabled={disabled}
  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200
    ${checked ? 'bg-emerald-600' : 'bg-gray-300'}
    ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
>
  <span
    className={`inline-block h-4 w-4 rounded-full bg-white shadow-sm transform transition-transform duration-200
      ${checked ? 'translate-x-6' : 'translate-x-1'}`}
  />
</button>
```

---

## 4. 체크인 리마인더 알림 UI

### 4-1. CheckInReminderToast

화면 우상단에 표시되는 토스트 알림. 일정 시간 후 자동 사라짐.

**파일 위치**: `frontend/src/components/common/CheckInReminderToast.tsx`

```tsx
interface CheckInReminderToastProps {
  visible: boolean;
  onClose: () => void;
  onCheckIn: () => void;
  dueMinutes: number;    // 체크인까지 남은 분
}
```

```tsx
{visible && (
  <div
    className="fixed top-4 right-4 z-50 w-80 max-w-[calc(100vw-2rem)] animate-slide-in-right"
    role="alert"
    aria-live="polite"
  >
    <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-4">
      <div className="flex items-start gap-3">
        <div className="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
          <svg className="h-5 w-5 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold text-gray-900">체크인 시간이에요!</p>
          <p className="text-xs text-gray-500 mt-0.5">
            {dueMinutes > 0
              ? `${dueMinutes}분 후 체크인 예정입니다`
              : '체크인 시간이 지났습니다'}
          </p>
          <button
            onClick={onCheckIn}
            className="mt-2 px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-xs font-medium hover:bg-emerald-700 transition-colors"
          >
            지금 체크인
          </button>
        </div>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600 shrink-0"
          aria-label="알림 닫기"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
  </div>
)}
```

### 4-2. CheckInReminderBanner

대시보드 상단에 표시되는 인라인 배너. 체크인이 지연되었을 때 노출.

```tsx
interface CheckInReminderBannerProps {
  overdue: boolean;        // 지연 여부
  overdueHours: number;    // 지연 시간
  onCheckIn: () => void;
  onDismiss: () => void;
}
```

```tsx
<div className={`rounded-xl p-4 flex items-center gap-3 ${
  overdue ? 'bg-orange-50 border border-orange-200' : 'bg-blue-50 border border-blue-200'
}`}
  role="alert"
>
  <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${
    overdue ? 'bg-orange-100' : 'bg-blue-100'
  }`}>
    <svg className={`h-5 w-5 ${overdue ? 'text-orange-600' : 'text-blue-600'}`}
      fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
        d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
    </svg>
  </div>
  <div className="flex-1 min-w-0">
    <p className={`text-sm font-medium ${overdue ? 'text-orange-800' : 'text-blue-800'}`}>
      {overdue
        ? `체크인이 ${overdueHours}시간 지연되었습니다`
        : '체크인 시간이 다가오고 있습니다'}
    </p>
  </div>
  <button
    onClick={onCheckIn}
    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
      overdue
        ? 'bg-orange-600 text-white hover:bg-orange-700'
        : 'bg-blue-600 text-white hover:bg-blue-700'
    }`}
  >
    체크인
  </button>
  <button onClick={onDismiss} className="text-gray-400 hover:text-gray-600" aria-label="닫기">
    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
    </svg>
  </button>
</div>
```

---

## 5. 인터랙션 플로우

### 5-1. 스마트 체크인 플로우
```
사용자: /checkin 페이지 접근
  -> 기분 5단계 중 하나 선택 (필수)
  -> 건강 상태 칩 0개 이상 선택 (선택)
  -> 메시지 입력 (선택)
  -> "체크인 하기" 버튼 클릭
  -> POST /api/checkins { mood, healthTags, message }
  -> 성공: AlertBanner("체크인 완료!") + 2초 후 / 대시보드 이동
  -> 실패: AlertBanner("체크인에 실패했습니다") + 재시도 가능
```

### 5-2. 리마인더 알림 플로우
```
시스템: 체크인 예정 시간 N분 전
  -> CheckInReminderToast 표시 (우상단)
  -> 사용자: "지금 체크인" 클릭 -> /checkin 이동
  -> 또는: 닫기 버튼 -> 토스트 사라짐

시스템: 체크인 시간 초과
  -> 대시보드에 CheckInReminderBanner 표시
  -> 사용자: "체크인" 클릭 -> /checkin 이동
  -> 또는: 닫기 -> 배너 숨김 (다음 접속 시 재표시)
```

### 5-3. 알림 설정 변경 플로우
```
사용자: /checkin/settings 접근
  -> 기존 설정 불러오기 (GET /api/checkin-settings)
  -> 스케줄/리마인더/에스컬레이션 값 변경
  -> "저장" 버튼 클릭
  -> PUT /api/checkin-settings
  -> 성공: AlertBanner("설정이 저장되었습니다")
  -> 실패: AlertBanner("저장에 실패했습니다")
```

---

## 6. 접근성 (A11y)

| 항목 | 적용 내용 |
|------|-----------|
| 기분 선택 | `aria-pressed` 속성으로 선택 상태 전달, `aria-label`로 기분 라벨 제공 |
| 건강 칩 | `aria-pressed`로 선택 상태, 이모지는 `aria-hidden="true"` 처리 |
| 토글 스위치 | `role="switch"`, `aria-checked`, `aria-label` 적용 |
| 토스트 알림 | `role="alert"`, `aria-live="polite"` 로 스크린리더 자동 읽기 |
| 배너 알림 | `role="alert"` 적용 |
| 색상 대비 | 모든 텍스트 WCAG AA 기준 충족 (4.5:1 이상) |
| 키보드 | 모든 interactive 요소 Tab 탐색 가능, Enter/Space 활성화 |
| 포커스 | `focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2` 적용 |

---

## 7. Tailwind 클래스 가이드

### 카드 기본
```
bg-white rounded-xl shadow-sm border border-gray-200 p-6
```

### 기분 선택 버튼 (미선택/선택)
```
미선택: border-2 border-transparent hover:bg-gray-50
선택:   border-2 bg-{color}-100 border-{color}-400
```

### 건강 칩 (미선택/선택)
```
미선택: bg-gray-100 text-gray-600 border border-transparent hover:bg-gray-200
선택:   bg-emerald-100 text-emerald-700 border border-emerald-300
```

### 체크인 메인 버튼
```
w-full py-4 bg-emerald-600 text-white rounded-xl font-semibold text-lg
hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors
```

### 토스트 위치
```
fixed top-4 right-4 z-50 w-80 max-w-[calc(100vw-2rem)]
```

### 타임라인 라인
```
pl-6 border-l-2 border-gray-200
```

---

## 8. tailwind.config.ts 변경사항

토스트 슬라이드 인 애니메이션을 추가한다.

```typescript
// tailwind.config.ts > theme.extend
{
  keyframes: {
    'slide-in-right': {
      '0%': { transform: 'translateX(100%)', opacity: '0' },
      '100%': { transform: 'translateX(0)', opacity: '1' },
    },
  },
  animation: {
    'slide-in-right': 'slide-in-right 0.3s ease-out',
  },
}
```

---

## 9. 프론트엔드 팀 전달사항

### 신규 파일
| 파일 | 설명 |
|------|------|
| `src/app/checkin/page.tsx` | 스마트 체크인 메인 페이지 |
| `src/app/checkin/history/page.tsx` | 체크인 히스토리 타임라인 |
| `src/app/checkin/settings/page.tsx` | 알림 설정 페이지 |
| `src/components/common/ToggleSwitch.tsx` | 토글 스위치 공통 컴포넌트 |
| `src/components/checkin/MoodSelector.tsx` | 기분 선택기 |
| `src/components/checkin/HealthQuickSelect.tsx` | 건강 상태 칩 선택기 |
| `src/components/checkin/CheckInTimeline.tsx` | 타임라인 컴포넌트 |
| `src/components/checkin/CheckInReminderToast.tsx` | 리마인더 토스트 |
| `src/components/checkin/CheckInReminderBanner.tsx` | 리마인더 배너 |

### 수정 파일
| 파일 | 변경 내용 |
|------|-----------|
| `src/app/page.tsx` | 대시보드에 `CheckInReminderBanner` 추가, 빠른 메뉴에 `/checkin` 링크 추가 |
| `src/types/index.ts` | 체크인 관련 타입 확장 (`mood`, `healthTags` 필드) |
| `tailwind.config.ts` | `slide-in-right` 애니메이션 추가 |

### 백엔드 팀 전달사항
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/checkins` | 기존 API에 `mood: number`, `healthTags: string[]` 필드 추가 |
| GET | `/api/checkins` | 응답에 `mood`, `healthTags` 필드 포함 |
| GET | `/api/checkin-settings` | 체크인 알림 설정 조회 |
| PUT | `/api/checkin-settings` | 체크인 알림 설정 수정 |
