# FR-05 — 카테고리 필터

| 항목              | value          |
| ----------------- | -------------- |
| 우선순위          | Medium         |
| 예상 변경 파일 수 | 2              |
| 신규 의존성       | 없음           |
| 선행 조건         | `FR-01` 완료 |

---

## 1. 목표

지도 화면 상단에 깔끔한 가로 필터 칩 바를 제공하여 사용자가 보고 싶어 하는 카테고리의 dot들만 지도에 선택적으로 렌더링되도록 스위칭한다.

## 2. 인수 기준 (Acceptance Criteria)

- [ ] 지도 앱 최상단 영역에 "음식점, 카페, 여행, 자연, 기타" 필터 칩 목록이 가로 스크롤(`LazyRow`) 형태로 깔끔하게 안착한다.
- [ ] 초기 기본값은 아무것도 선택되지 않은 전체 표시 상태이다.
- [ ] 특정 필터 칩(예: 카페)을 선택하면 지도 위에 해당 카테고리를 가진 dot 마커들만 필터링되어 화면에 남는다.
- [ ] 이미 선택된 필터 칩을 다시 한 번 터치하면 선택이 해제되면서 원래대로 전체 데이터 마커들이 지도에 복귀한다.

## 3. 변경 범위

### 신규 파일

- `ui/map/components/CategoryFilterRow.kt` — 상단 가로 스크롤형 필터 칩 컴포저블

### 수정 파일

- `ui/map/MapScreen.kt` — `KakaoMap` 레이아웃 윗단 공간에 `CategoryFilterRow` 컴포넌트 결합
- `ui/map/MapViewModel.kt` — `selectedCategory` 상태값 변화에 따라 Room DB에서 읽어온 전체 `dots` 리스트를 `filteredDots`로 가공하여 방출하는 파이프라인 구축

## 4. 구현 가이드

### 4.1 가로 스크롤 및 양방향 필터 로직

`LazyRow`를 사용해 칩들을 유연하게 정렬하고, 클릭 시 뷰모델의 상태 값을 스위칭(`null` ↔️ `Category`)해 준다.

```kotlin
// CategoryFilterRow.kt 예시 가이드
LazyRow(
    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(DotCategory.values()) { category ->
        val isSelected = uiState.selectedCategory == category
        FilterChip(
            selected = isSelected,
            onClick = { viewModel.toggleCategoryFilter(category) },
            label = { Text("${category.emoji} ${category.label}") }
        )
    }
}
```


## 5. 검증

1. 상단 '카페' 칩을 누르는 순간 지도 화면에서 음식점이나 자연 마커들이 눈 깜짝할 새 사라지고 오직 카페 관련 마커들만 남는지 눈으로 확인.
2. '카페'가 켜진 상태에서 한 번 더 '카페'를 누르면 다시 온 사방의 모든 마커들이 누락 없이 완전하게 복구되는지 확인.
3. 가로 드래그 시 필터 바 영역이 부드럽게 스크롤링되는지 체킹.

## 6. 비고

* 매번 필터 칩을 누를 때마다 DB에 쿼리를 매번 다시 날리는 비효율적인 방식 대신, Kotlin Flow의 `combine` 연동 기법을 써서 메모리 상에서 `dots`와 `selectedCategory`를 조합해 연산(MVI 스타일 상태 가공)하도록 유도한다.
