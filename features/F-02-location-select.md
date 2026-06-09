# FR-02 — 위치 지정 (dot 추가 모드)

| 항목              | 값             |
| ----------------- | -------------- |
| 우선순위          | High           |
| 예상 변경 파일 수 | 2              |
| 신규 의존성       | 없음           |
| 선행 조건         | `FR-01` 완료 |

---

## 1. 목표

사용자가 지도 중앙에 고정된 핀을 이용해 dot을 추가할 위치의 좌표를 명확히 지정하고 작성 화면으로 스무스하게 넘어갈 수 있도록 흐름을 잡는다.

## 2. 인수 기준 (Acceptance Criteria)

- [ ] 우하단 `FloatingActionButton` 탭 시 위치 지정 모드(`isAddMode = true`)로 전환된다.
- [ ] 위치 지정 모드 진입 시 지도 중앙에 빨간색 고정 핀 UI가 표시된다.
- [ ] 사용자가 지도를 드래그하여 움직이면 중앙 핀의 좌표가 실시간으로 조준된다.
- [ ] 화면 하단에 "여기에 추가" 버튼이 팝업으로 표시된다.
- [ ] "여기에 추가" 버튼을 탭하면 현재 중앙 좌표를 저장하고 `WriteDotScreen`으로 화면을 이동한다.

## 3. 변경 범위

### 신규 파일

- 없음 (기존 지도 화면 확장)

### 수정 파일

- `ui/map/MapScreen.kt` — `isAddMode` 상태에 따른 중앙 핀 UI 및 하단 "여기에 추가" 바 레이아웃 추가
- `ui/map/MapViewModel.kt` — 위치 지정 모드 상태 토글 메서드 및 선택된 좌표 보관 상태 추가

## 4. 구현 가이드

### 4.1 상태 기반 UI 분기

별도의 화면으로 이동하는 대신, `MapUiState`의 `isAddMode`가 `true`일 때 Compose의 `Box` 레이아웃 중앙에 핀 이미지를 올리고 하단 바를 노출한다.

```kotlin
// MapScreen.kt 내부 구현 가이드
Box(modifier = Modifier.fillMaxSize()) {
    // 카카오맵 배경
    KakaoMap( ... )

    // 위치 지정 모드일 때만 레이어 얹기
    if (uiState.isAddMode) {
        // 1. 화면 정중앙에 고정 핀 배치
        Image(
            painter = painterResource(id = R.drawable.ic_center_pin),
            modifier = Modifier.align(Alignment.Center)
        )
        // 2. 하단 "여기에 추가" 버튼 바 배치
        LocationSelectBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onAddClick = { viewModel.onLocationConfirmed(currentCameraCenter) }
        )
    }
}
```

## 5. 검증

1. 메인 화면 우하단 `+` 버튼을 누르면 화면 전환 딜레이 없이 즉시 지도 중앙에 핀이 고정되는지 확인.
2. 지도를 이리저리 드래그해도 중앙 핀은 화면 한가운데 가만히 고정되어 있는지 시각 확인.
3. "여기에 추가" 버튼을 누르면 앱이 튕기지 않고 빈 작성 화면(`WriteDotScreen`)으로 정상 이동하는지 확인.

## 6. 비고

* 카카오 지도 SDK v2에서 현재 카메라의 중심점 좌표(위도/경도)를 안전하게 추출하는 카메라 리스너 연동이 핵심이다.
* 갤러리 사진이나 코멘트를 입력하는 상세 레이아웃은 다음 단계(`FR-03`)에서 구현한다.
