# 이웃 돌봄 매칭 시스템 화면 설계

## 개요
1인 가구와 지역 자원봉사자를 매칭하여 정기적인 돌봄 방문을 지원하는 기능이다.
자원봉사자 등록, 근거리 매칭, 방문 일정 관리, 방문 보고서 작성, 신뢰도 점수 표시를 포함한다.

## 영향받는 페이지
| 페이지 | 경로 | 변경 유형 |
|--------|------|-----------|
| 이웃 돌봄 메인 | `/care` | 신규 페이지 |
| 자원봉사 등록 | `/care/volunteer` | 신규 페이지 |
| 돌봄 매칭 | `/care/match` | 신규 페이지 |
| 방문 일정 | `/care/schedule` | 신규 페이지 |
| 방문 보고서 작성 | `/care/report/new` | 신규 페이지 |
| 방문 보고서 상세 | `/care/report/[id]` | 신규 페이지 |

---

## 1. 이웃 돌봄 메인 페이지 (`/care`)

### 레이아웃
`PageLayout` 사용, `max-w-4xl`. 역할(자원봉사자/돌봄대상)에 따라 다른 대시보드를 보여준다.

```
[Header - 공통]
[페이지 타이틀 + 역할 배지]
[내 매칭 현황 카드]
[다가오는 방문 일정 카드]
[최근 방문 보고서 목록]
[빠른 액션 버튼 그룹]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| CareRoleBadge | 역할 표시 배지 | `role: 'volunteer' \| 'recipient'` |
| CareMatchCard | 매칭 상대 정보 카드 | `match` |
| UpcomingVisitCard | 다가오는 방문 일정 | `visit` |
| VisitReportListItem | 방문 보고서 목록 항목 | `report` |
| TrustScoreBadge | 신뢰도 점수 표시 | `score`, `level` |

#### 1-1. CareMatchCard

현재 매칭된 상대방 정보를 보여주는 카드.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <div className="flex items-center justify-between mb-4">
    <h2 className="text-lg font-semibold text-gray-900">내 돌봄 매칭</h2>
    <span className="text-xs text-gray-400">매칭일: {formatDate(match.matchedAt)}</span>
  </div>

  {match ? (
    <div className="flex items-center gap-4">
      {/* 프로필 아바타 */}
      <div className="w-14 h-14 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
        <span className="text-xl font-bold text-emerald-600">
          {match.name.charAt(0)}
        </span>
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <p className="text-base font-semibold text-gray-900 truncate">{match.name}</p>
          <TrustScoreBadge score={match.trustScore} level={match.trustLevel} />
        </div>
        <p className="text-sm text-gray-500 mt-0.5">{match.distance}</p>
        <p className="text-xs text-gray-400 mt-0.5">방문 {match.visitCount}회 완료</p>
      </div>

      <Link
        href={`/care/schedule`}
        className="px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors whitespace-nowrap"
      >
        일정 보기
      </Link>
    </div>
  ) : (
    <div className="text-center py-8">
      <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-3">
        <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
            d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
      </div>
      <p className="text-sm text-gray-500 mb-3">아직 매칭된 이웃이 없습니다</p>
      <Link
        href="/care/match"
        className="inline-flex px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
      >
        이웃 찾기
      </Link>
    </div>
  )}
</div>
```

#### 1-2. TrustScoreBadge

```tsx
interface TrustScoreBadgeProps {
  score: number;         // 0~100
  level: 'new' | 'bronze' | 'silver' | 'gold';
}
```

레벨별 스타일:
| 레벨 | 라벨 | 색상 |
|------|------|------|
| new | 신규 | `bg-gray-100 text-gray-600` |
| bronze | 브론즈 | `bg-amber-100 text-amber-700` |
| silver | 실버 | `bg-slate-100 text-slate-600` |
| gold | 골드 | `bg-yellow-100 text-yellow-700` |

```tsx
<span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${levelStyles[level]}`}>
  <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
  </svg>
  {getLevelLabel(level)} {score}
</span>
```

---

## 2. 자원봉사 등록 (`/care/volunteer`)

### 레이아웃
`PageLayout` 사용, `max-w-2xl`. 단계별 폼이 아닌 단일 폼으로 구성.

```
[Header - 공통]
[페이지 타이틀 + 안내 텍스트]
[기본 정보 섹션]
[활동 가능 정보 섹션]
[활동 지역/반경 섹션]
[동의 체크박스]
[등록 버튼]
```

### 마크업 구조

```tsx
<PageLayout maxWidth="max-w-2xl">
  <div className="mb-6">
    <h1 className="text-2xl font-bold text-gray-900">자원봉사 등록</h1>
    <p className="text-sm text-gray-500 mt-1">이웃의 안전을 함께 지켜주세요</p>
  </div>

  {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

  <form onSubmit={handleSubmit} className="space-y-6">
    {/* 기본 정보 */}
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">기본 정보</h2>
      <div className="space-y-4">
        <FormField label="이름" type="text" value={form.name} readOnly
          hint="프로필에서 가져옵니다" />
        <FormField label="연락처" type="tel" value={form.phone}
          onChange={(e) => setForm({ ...form, phone: e.target.value })}
          placeholder="010-0000-0000" />
        <FormField as="textarea" label="자기소개" value={form.introduction}
          onChange={(e) => setForm({ ...form, introduction: e.target.value })}
          rows={3} placeholder="간단한 자기소개를 작성해주세요" optional />
      </div>
    </div>

    {/* 활동 가능 정보 */}
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">활동 가능 시간</h2>
      <div className="space-y-4">
        {/* 요일 선택 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">가능한 요일</label>
          <div className="flex gap-2">
            {DAYS.map((day) => (
              <button
                key={day.value}
                type="button"
                onClick={() => toggleDay(day.value)}
                className={`w-10 h-10 rounded-full text-sm font-medium transition-colors
                  ${form.availableDays.includes(day.value)
                    ? 'bg-emerald-600 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                aria-pressed={form.availableDays.includes(day.value)}
                aria-label={day.label}
              >
                {day.short}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <FormField label="시작 시간" type="time" value={form.availableStart}
            onChange={(e) => setForm({ ...form, availableStart: e.target.value })} />
          <FormField label="종료 시간" type="time" value={form.availableEnd}
            onChange={(e) => setForm({ ...form, availableEnd: e.target.value })} />
        </div>
      </div>
    </div>

    {/* 활동 지역 */}
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">활동 지역</h2>
      <div className="space-y-4">
        <FormField label="주소" type="text" value={form.address}
          onChange={(e) => setForm({ ...form, address: e.target.value })}
          placeholder="활동 가능한 기준 주소" />

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">활동 반경</label>
          <div className="flex gap-2">
            {RADIUS_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                onClick={() => setForm({ ...form, radiusKm: opt.value })}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
                  ${form.radiusKm === opt.value
                    ? 'bg-emerald-600 text-white'
                    : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
                  }`}
                aria-pressed={form.radiusKm === opt.value}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>

    {/* 동의 */}
    <label className="flex items-start gap-3 cursor-pointer">
      <input
        type="checkbox"
        checked={form.agreed}
        onChange={(e) => setForm({ ...form, agreed: e.target.checked })}
        className="mt-0.5 h-5 w-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500"
      />
      <span className="text-sm text-gray-700">
        이웃 돌봄 자원봉사 활동 가이드라인에 동의합니다.
        <button type="button" className="text-emerald-600 hover:underline ml-1">상세 보기</button>
      </span>
    </label>

    <button
      type="submit"
      disabled={submitting || !form.agreed}
      className="w-full py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
    >
      {submitting ? '등록 중...' : '자원봉사 등록'}
    </button>
  </form>
</PageLayout>
```

요일 상수:
```typescript
const DAYS = [
  { value: 'MON', short: '월', label: '월요일' },
  { value: 'TUE', short: '화', label: '화요일' },
  { value: 'WED', short: '수', label: '수요일' },
  { value: 'THU', short: '목', label: '목요일' },
  { value: 'FRI', short: '금', label: '금요일' },
  { value: 'SAT', short: '토', label: '토요일' },
  { value: 'SUN', short: '일', label: '일요일' },
];
```

반경 옵션:
```typescript
const RADIUS_OPTIONS = [
  { value: 1, label: '1km' },
  { value: 3, label: '3km' },
  { value: 5, label: '5km' },
  { value: 10, label: '10km' },
];
```

### 상태 처리
| 상태 | UI |
|------|-----|
| 로딩 | `LoadingSpinner` (프로필 정보 로드 중) |
| 제출 중 | 버튼 "등록 중..." + 폼 비활성 |
| 성공 | `AlertBanner variant="success"` + `/care` 이동 |
| 실패 | `AlertBanner variant="error"` 표시 |
| 이미 등록됨 | 안내 메시지 + 수정 모드 전환 |

---

## 3. 돌봄 매칭 (`/care/match`)

### 레이아웃
`PageLayout` 사용, `max-w-4xl`. 상단에 뷰 전환(리스트/지도), 필터, 매칭 목록.

```
[Header - 공통]
[페이지 타이틀]
[검색 필터 바 (거리, 가능 시간)]
[뷰 전환 탭 (리스트 | 지도)]
[매칭 후보 리스트 또는 지도 뷰]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| CareMatchFilter | 필터 바 | `filters`, `onChange` |
| ViewToggle | 리스트/지도 뷰 전환 | `view`, `onChange` |
| CareMatchList | 매칭 후보 리스트 | `items` |
| CareMatchListItem | 개별 후보 카드 | `candidate` |
| CareMatchMap | 지도 뷰 (향후 구현) | `items`, `center` |

#### 3-1. CareMatchFilter

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 flex flex-col sm:flex-row gap-3">
  <div className="flex-1">
    <select
      value={filters.distance}
      onChange={(e) => onChange({ ...filters, distance: e.target.value })}
      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
      aria-label="거리 필터"
    >
      <option value="1">1km 이내</option>
      <option value="3">3km 이내</option>
      <option value="5">5km 이내</option>
      <option value="10">10km 이내</option>
    </select>
  </div>
  <div className="flex-1">
    <select
      value={filters.day}
      onChange={(e) => onChange({ ...filters, day: e.target.value })}
      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
      aria-label="요일 필터"
    >
      <option value="">전체 요일</option>
      {DAYS.map((d) => <option key={d.value} value={d.value}>{d.label}</option>)}
    </select>
  </div>
  <button
    onClick={onSearch}
    className="px-6 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
  >
    검색
  </button>
</div>
```

#### 3-2. ViewToggle

```tsx
<div className="flex bg-gray-100 rounded-lg p-1 w-fit">
  <button
    onClick={() => onChange('list')}
    className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors
      ${view === 'list' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
    aria-pressed={view === 'list'}
  >
    리스트
  </button>
  <button
    onClick={() => onChange('map')}
    className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors
      ${view === 'map' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
    aria-pressed={view === 'map'}
  >
    지도
  </button>
</div>
```

#### 3-3. CareMatchListItem

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition-shadow">
  <div className="flex items-start gap-4">
    {/* 아바타 */}
    <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
      <span className="text-lg font-bold text-blue-600">{candidate.name.charAt(0)}</span>
    </div>

    <div className="flex-1 min-w-0">
      <div className="flex items-center gap-2 mb-1">
        <h3 className="text-base font-semibold text-gray-900 truncate">{candidate.name}</h3>
        <TrustScoreBadge score={candidate.trustScore} level={candidate.trustLevel} />
      </div>

      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-500 mb-2">
        <span className="inline-flex items-center gap-1">
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
          </svg>
          {candidate.distance}
        </span>
        <span className="inline-flex items-center gap-1">
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {candidate.availableTime}
        </span>
        <span>방문 {candidate.visitCount}회</span>
      </div>

      {candidate.introduction && (
        <p className="text-sm text-gray-600 line-clamp-2">{candidate.introduction}</p>
      )}
    </div>

    <button
      onClick={() => onRequestMatch(candidate.id)}
      className="px-4 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors whitespace-nowrap shrink-0"
    >
      매칭 요청
    </button>
  </div>
</div>
```

#### 3-4. CareMatchMap (Placeholder)

지도 뷰는 추후 카카오맵 또는 네이버맵 API 연동 시 구현. 현재는 플레이스홀더.

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
  <div className="aspect-[4/3] bg-gray-100 rounded-lg flex items-center justify-center">
    <div className="text-center">
      <svg className="h-12 w-12 text-gray-300 mx-auto mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
          d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
      </svg>
      <p className="text-sm text-gray-400">지도 기능 준비 중입니다</p>
    </div>
  </div>
</div>
```

### 상태 처리
| 상태 | UI |
|------|-----|
| 로딩 | `LoadingSpinner` 리스트 영역 |
| 검색 결과 없음 | 빈 상태 아이콘 + "조건에 맞는 이웃을 찾지 못했습니다" + 필터 변경 안내 |
| 에러 | `AlertBanner variant="error"` |
| 매칭 요청 중 | 해당 카드 버튼 "요청 중..." + 스피너 |
| 매칭 요청 완료 | 버튼 -> "요청 완료" (비활성, 회색) |

---

## 4. 방문 일정 캘린더 (`/care/schedule`)

### 레이아웃
`PageLayout` 사용, `max-w-4xl`. 월간 캘린더와 일별 방문 목록을 결합.

```
[Header - 공통]
[페이지 타이틀 + 새 일정 버튼]
[월 네비게이션 (< 2026년 3월 >)]
[캘린더 그리드]
[선택된 날짜의 방문 목록]
```

### 컴포넌트 목록

| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| MonthNavigator | 월 이동 네비게이션 | `year`, `month`, `onPrev`, `onNext` |
| CareCalendar | 월간 캘린더 그리드 | `year`, `month`, `visits`, `selectedDate`, `onSelectDate` |
| DayVisitList | 선택된 날짜의 방문 목록 | `visits`, `date` |
| VisitScheduleModal | 새 일정 등록 모달 | `open`, `onClose`, `onSubmit` |

#### 4-1. MonthNavigator

```tsx
<div className="flex items-center justify-between mb-4">
  <button
    onClick={onPrev}
    className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
    aria-label="이전 달"
  >
    <svg className="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
    </svg>
  </button>
  <h2 className="text-lg font-semibold text-gray-900">
    {year}년 {month}월
  </h2>
  <button
    onClick={onNext}
    className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
    aria-label="다음 달"
  >
    <svg className="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
    </svg>
  </button>
</div>
```

#### 4-2. CareCalendar

```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
  {/* 요일 헤더 */}
  <div className="grid grid-cols-7 bg-gray-50 border-b border-gray-200">
    {['일', '월', '화', '수', '목', '금', '토'].map((day, i) => (
      <div key={day} className={`py-2 text-center text-xs font-medium
        ${i === 0 ? 'text-red-500' : i === 6 ? 'text-blue-500' : 'text-gray-500'}`}>
        {day}
      </div>
    ))}
  </div>

  {/* 날짜 그리드 */}
  <div className="grid grid-cols-7">
    {calendarDays.map((day) => (
      <button
        key={day.date}
        onClick={() => onSelectDate(day.date)}
        disabled={day.isOtherMonth}
        className={`relative h-16 sm:h-20 p-1 border-b border-r border-gray-100 transition-colors
          ${day.isToday ? 'bg-emerald-50' : ''}
          ${day.date === selectedDate ? 'ring-2 ring-inset ring-emerald-500' : ''}
          ${day.isOtherMonth ? 'opacity-30' : 'hover:bg-gray-50'}
        `}
        aria-label={`${day.date}, 방문 ${day.visitCount}건`}
        aria-current={day.isToday ? 'date' : undefined}
      >
        <span className={`text-sm font-medium
          ${day.isToday ? 'text-emerald-600' : 'text-gray-900'}`}>
          {day.dayNumber}
        </span>
        {day.visitCount > 0 && (
          <div className="absolute bottom-1 left-1/2 -translate-x-1/2 flex gap-0.5">
            {Array.from({ length: Math.min(day.visitCount, 3) }).map((_, i) => (
              <div key={i} className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
            ))}
          </div>
        )}
      </button>
    ))}
  </div>
</div>
```

#### 4-3. DayVisitList

```tsx
<div className="mt-4 space-y-3">
  <h3 className="text-sm font-semibold text-gray-700">
    {formatDate(date)} 방문 일정
  </h3>

  {visits.length === 0 ? (
    <p className="text-sm text-gray-400 py-4 text-center">이 날짜에 예정된 방문이 없습니다</p>
  ) : (
    visits.map((visit) => (
      <div key={visit.id} className="bg-white rounded-lg border border-gray-200 p-4 flex items-center gap-3">
        <div className={`w-1 h-12 rounded-full shrink-0 ${
          visit.status === 'COMPLETED' ? 'bg-emerald-500' :
          visit.status === 'UPCOMING' ? 'bg-blue-500' :
          'bg-gray-300'
        }`} />

        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-gray-900">{visit.partnerName}</p>
          <p className="text-xs text-gray-500">{visit.time} | {visit.duration}</p>
        </div>

        <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${
          visit.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-700' :
          visit.status === 'UPCOMING' ? 'bg-blue-100 text-blue-700' :
          visit.status === 'CANCELLED' ? 'bg-gray-100 text-gray-500' :
          'bg-yellow-100 text-yellow-700'
        }`}>
          {getStatusLabel(visit.status)}
        </span>
      </div>
    ))
  )}
</div>
```

방문 상태:
| 상태 | 라벨 | 색상 |
|------|------|------|
| UPCOMING | 예정 | `bg-blue-100 text-blue-700` |
| COMPLETED | 완료 | `bg-emerald-100 text-emerald-700` |
| CANCELLED | 취소 | `bg-gray-100 text-gray-500` |
| IN_PROGRESS | 진행중 | `bg-yellow-100 text-yellow-700` |

### 반응형 대응
| 브레이크포인트 | 변화 |
|---------------|------|
| 모바일 (< 640px) | 캘린더 셀 `h-16`, 날짜 숫자만 표시 |
| 태블릿 이상 (>= 640px) | 캘린더 셀 `h-20`, 점 표시 여유 |

---

## 5. 방문 보고서 작성 (`/care/report/new`)

### 레이아웃
`PageLayout` 사용, `max-w-2xl`.

```
[Header - 공통]
[페이지 타이틀]
[방문 정보 요약 (자동 채움)]
[돌봄 대상 상태 평가]
[관찰 사항 텍스트]
[특이사항 체크리스트]
[사진 첨부 (선택)]
[제출 버튼]
```

### 마크업 구조

```tsx
<PageLayout maxWidth="max-w-2xl">
  <div className="mb-6">
    <h1 className="text-2xl font-bold text-gray-900">방문 보고서 작성</h1>
    <p className="text-sm text-gray-500 mt-1">방문 내용을 기록해주세요</p>
  </div>

  {message && <AlertBanner message={message} variant={isError ? 'error' : 'success'} />}

  <form onSubmit={handleSubmit} className="space-y-6">
    {/* 방문 정보 */}
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">방문 정보</h2>
      <dl className="space-y-3">
        <div className="flex justify-between">
          <dt className="text-sm text-gray-600">방문 대상</dt>
          <dd className="text-sm font-medium text-gray-900">{visit.recipientName}</dd>
        </div>
        <div className="flex justify-between">
          <dt className="text-sm text-gray-600">방문 일시</dt>
          <dd className="text-sm font-medium text-gray-900">{formatDateTime(visit.scheduledAt)}</dd>
        </div>
        <div className="flex justify-between">
          <dt className="text-sm text-gray-600">방문 시간</dt>
          <dd className="text-sm font-medium text-gray-900">{visit.duration}</dd>
        </div>
      </dl>
    </div>

    {/* 돌봄 대상 상태 평가 */}
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-2">돌봄 대상 상태</h2>
      <p className="text-sm text-gray-500 mb-4">방문 시 관찰한 상태를 선택해주세요</p>

      <div className="space-y-3">
        {/* 전반적 상태 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">전반적 상태</label>
          <div className="flex gap-2">
            {CONDITIONS.map((c) => (
              <button
                key={c.value}
                type="button"
                onClick={() => setForm({ ...form, condition: c.value })}
                className={`flex-1 py-2.5 rounded-lg text-sm font-medium transition-colors border
                  ${form.condition === c.value
                    ? `${c.selectedBg} ${c.selectedText} ${c.selectedBorder}`
                    : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
                  }`}
                aria-pressed={form.condition === c.value}
              >
                {c.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>

    {/* 관찰 사항 */}
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">관찰 사항</h2>
      <FormField
        as="textarea"
        label="방문 내용"
        value={form.observation}
        onChange={(e) => setForm({ ...form, observation: e.target.value })}
        rows={5}
        placeholder="방문 시 관찰한 내용을 자유롭게 작성해주세요"
      />
    </div>

    {/* 특이사항 체크리스트 */}
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">특이사항</h2>
      <div className="space-y-3">
        {CONCERNS.map((concern) => (
          <label key={concern.value} className="flex items-center gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={form.concerns.includes(concern.value)}
              onChange={() => toggleConcern(concern.value)}
              className="h-5 w-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500"
            />
            <span className="text-sm text-gray-700">{concern.label}</span>
          </label>
        ))}
      </div>
    </div>

    <button
      type="submit"
      disabled={submitting || !form.condition || !form.observation}
      className="w-full py-3 bg-emerald-600 text-white rounded-xl font-semibold hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
    >
      {submitting ? '제출 중...' : '보고서 제출'}
    </button>
  </form>
</PageLayout>
```

전반적 상태 옵션:
```typescript
const CONDITIONS = [
  { value: 'GOOD', label: '양호', selectedBg: 'bg-emerald-100', selectedText: 'text-emerald-700', selectedBorder: 'border-emerald-300' },
  { value: 'FAIR', label: '보통', selectedBg: 'bg-yellow-100', selectedText: 'text-yellow-700', selectedBorder: 'border-yellow-300' },
  { value: 'POOR', label: '주의', selectedBg: 'bg-orange-100', selectedText: 'text-orange-700', selectedBorder: 'border-orange-300' },
  { value: 'CRITICAL', label: '위험', selectedBg: 'bg-red-100', selectedText: 'text-red-700', selectedBorder: 'border-red-300' },
];
```

특이사항 체크리스트:
```typescript
const CONCERNS = [
  { value: 'HYGIENE', label: '위생 상태 불량' },
  { value: 'MALNUTRITION', label: '식사/영양 부족 의심' },
  { value: 'INJURY', label: '부상 또는 상처 발견' },
  { value: 'MENTAL_HEALTH', label: '정서적 불안 또는 우울 징후' },
  { value: 'MEDICATION', label: '약물 복용 미이행' },
  { value: 'MOBILITY', label: '거동 불편 심화' },
  { value: 'HOME_SAFETY', label: '주거 환경 안전 문제' },
  { value: 'OTHER', label: '기타 (관찰 사항에 기재)' },
];
```

### 상태 처리
| 상태 | UI |
|------|-----|
| 로딩 | `LoadingSpinner` (방문 정보 로드) |
| 필수 입력 미완료 | 제출 버튼 `disabled`, 미입력 필드 하단에 안내 |
| 제출 중 | 버튼 "제출 중..." + 폼 비활성 |
| 제출 성공 | `AlertBanner variant="success"` + `/care` 이동 |
| 제출 실패 | `AlertBanner variant="error"` |

---

## 6. 인터랙션 플로우

### 6-1. 자원봉사 등록 플로우
```
사용자: /care/volunteer 접근
  -> 기본 정보 자동 입력 (프로필에서 로드)
  -> 활동 가능 요일/시간 선택
  -> 활동 지역/반경 설정
  -> 가이드라인 동의 체크
  -> "자원봉사 등록" 버튼 클릭
  -> POST /api/care/volunteers
  -> 성공: /care 이동 + 안내 메시지
```

### 6-2. 매칭 요청 플로우
```
사용자: /care/match 접근
  -> 거리/요일 필터 설정 -> 검색
  -> GET /api/care/matches?distance=3&day=MON
  -> 매칭 후보 리스트 표시
  -> "매칭 요청" 버튼 클릭
  -> POST /api/care/matches/{candidateId}/request
  -> 상대방 알림 발송 (Kafka 이벤트)
  -> 버튼 -> "요청 완료" 상태
```

### 6-3. 방문 보고서 플로우
```
사용자: 방문 완료 후 /care/report/new?visitId=123 접근
  -> 방문 정보 자동 로드
  -> 돌봄 대상 상태 평가 선택
  -> 관찰 사항 텍스트 작성
  -> 특이사항 체크 (해당 시)
  -> "보고서 제출" 클릭
  -> POST /api/care/reports
  -> CRITICAL 상태 선택 시: 관리자 즉시 알림 발송
```

---

## 7. 접근성 (A11y)

| 항목 | 적용 내용 |
|------|-----------|
| 요일 선택 | `aria-pressed`, `aria-label`로 전체 요일명 제공 |
| 캘린더 | `aria-label`로 날짜와 방문 건수 안내, `aria-current="date"`로 오늘 표시 |
| 상태 배지 | 색상만이 아닌 텍스트 라벨로 상태 전달 |
| 뷰 전환 | `aria-pressed`로 현재 뷰 상태 전달 |
| 체크박스 | 네이티브 `<input type="checkbox">` 사용, `<label>` 연결 |
| 색상 대비 | 모든 텍스트 WCAG AA 충족, 상태 칩은 텍스트+배경 조합 4.5:1 이상 |
| 포커스 | `focus:ring-2 focus:ring-emerald-500` 적용 |

---

## 8. 반응형 브레이크포인트별 대응

| 브레이크포인트 | 변화 |
|---------------|------|
| 모바일 (< 640px) | 필터 바 세로 배치(`flex-col`), 매칭 카드 버튼 풀 너비, 캘린더 셀 축소 |
| 태블릿 (>= 640px) | 필터 바 가로 배치(`flex-row`), 카드 인라인 버튼 |
| 데스크탑 (>= 1024px) | `max-w-4xl` 유지, 매칭 리스트와 지도 2컬럼 가능 (향후) |

---

## 9. Tailwind 클래스 가이드

### 카드 기본
```
bg-white rounded-xl shadow-sm border border-gray-200 p-6
```

### 아바타 (이니셜)
```
w-12 h-12 rounded-full bg-{color}-100 flex items-center justify-center
text-lg font-bold text-{color}-600
```

### 상태 배지
```
px-2.5 py-1 rounded-full text-xs font-medium bg-{color}-100 text-{color}-700
```

### 요일 선택 버튼 (미선택/선택)
```
미선택: w-10 h-10 rounded-full bg-gray-100 text-gray-600 hover:bg-gray-200
선택:   w-10 h-10 rounded-full bg-emerald-600 text-white
```

### 캘린더 셀
```
relative h-16 sm:h-20 p-1 border-b border-r border-gray-100 transition-colors
```

### 필터 select
```
w-full px-3 py-2 border border-gray-300 rounded-lg text-sm
focus:ring-2 focus:ring-emerald-500 focus:border-transparent
```

---

## 10. tailwind.config.ts 변경사항

별도 커스텀 설정 추가 불필요. 기존 Tailwind 유틸리티로 충분히 구현 가능.

---

## 11. 프론트엔드 팀 전달사항

### 신규 파일
| 파일 | 설명 |
|------|------|
| `src/app/care/page.tsx` | 이웃 돌봄 메인 대시보드 |
| `src/app/care/volunteer/page.tsx` | 자원봉사 등록 폼 |
| `src/app/care/match/page.tsx` | 돌봄 매칭 리스트/지도 |
| `src/app/care/schedule/page.tsx` | 방문 일정 캘린더 |
| `src/app/care/report/new/page.tsx` | 방문 보고서 작성 |
| `src/app/care/report/[id]/page.tsx` | 방문 보고서 상세 |
| `src/components/care/CareMatchCard.tsx` | 매칭 카드 |
| `src/components/care/TrustScoreBadge.tsx` | 신뢰도 점수 배지 |
| `src/components/care/CareCalendar.tsx` | 월간 캘린더 |
| `src/components/care/CareMatchListItem.tsx` | 매칭 후보 항목 |

### 수정 파일
| 파일 | 변경 내용 |
|------|-----------|
| `src/app/page.tsx` | 빠른 메뉴에 `/care` 이웃 돌봄 링크 추가 |
| `src/components/common/Header.tsx` | 네비게이션에 "이웃 돌봄" 메뉴 추가 |
| `src/types/index.ts` | 돌봄 관련 타입 추가 (`CareMatch`, `CareVisit`, `CareReport`, `Volunteer`) |

### 백엔드 팀 전달사항
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/care/volunteers` | 자원봉사 등록 |
| GET | `/api/care/volunteers/me` | 내 자원봉사 정보 조회 |
| PUT | `/api/care/volunteers/me` | 자원봉사 정보 수정 |
| GET | `/api/care/matches` | 매칭 후보 목록 (필터: distance, day) |
| POST | `/api/care/matches/{id}/request` | 매칭 요청 |
| GET | `/api/care/matches/me` | 내 매칭 현황 |
| GET | `/api/care/visits` | 방문 일정 목록 (필터: year, month) |
| POST | `/api/care/visits` | 방문 일정 등록 |
| PUT | `/api/care/visits/{id}` | 방문 일정 수정/취소 |
| POST | `/api/care/reports` | 방문 보고서 제출 |
| GET | `/api/care/reports/{id}` | 방문 보고서 상세 |
| GET | `/api/care/reports` | 방문 보고서 목록 |
