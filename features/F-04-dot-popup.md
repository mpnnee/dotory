
# FR-04 — dot 카드 팝업

| 항목              | 값                                                                |
| ----------------- | ----------------------------------------------------------------- |
| 우선순위          | Medium                                                            |
| 예상 변경 파일 수 | 2~3                                                               |
| 신규 의존성       | 없음                                                              |
| 선행 조건         | `FR-03` 완료 (DB에 최소 1개 이상의 데이터가 적재되어 있어야 함) |

---

## 1. 목표

지도 상에 뿌려진 마커를 사용자가 터치했을 때, 해당 마커 바로 위에 풍선 도움말 형태로 장소 정보가 요약된 예쁜 커스텀 카드 팝업을 띄운다.

## 2. 인수 기준 (Acceptance Criteria)

- [ ] 특정 dot 마커를 가볍게 탭하면 마커 상단에 Compose `Popup` 기반의 정보 카드가 팝업된다.
- [ ] 카드 내부에는 로컬에서 로딩한 사진(`AsyncImage`), 볼드 처리된 장소 이름, 코멘트, 그리고 카테고리 뱃지가 예쁘게 배치된다.
- [ ] 와이어프레임(S-02)에 맞게 흰 배경 + 둥근 모서리 디자인을 적용하고 하단에 마커를 가리키는 꼬리(tail) 모양을 심는다.
- [ ] 카드가 띄워진 상태에서 카드 바깥의 아무 영역이나 터치하면 팝업 카드가 자연스럽게 닫힌다.

## 3. 변경 범위

### 신규 파일

- `ui/map/components/DotCardPopup.kt` — 마커 위에 얹을 꼬리가 달린 커스텀 카드 레이아웃 컴포저블

### 수정 파일

- `ui/map/MapScreen.kt` — 마커 클릭 리스너 가로채기 및 `selectedDot` 유무에 따른 `DotCardPopup` 호출 코드 추가

## 4. 구현 가이드

### 4.1 커스텀 꼬리(Tail) 모양 그리기

카드의 테두리와 하단 정중앙에 삼각형 모양의 꼬리를 달아주기 위해 Compose의 `Canvas` 또는 `Path` 문법을 활용한 커스텀 셰이프(Shape) 디자인을 적용한다.

```kotlin
// DotCardPopup.kt 셰이프 구성 힌트
val cardShape = RoundedCornerShape(16.dp)

Popup(
    alignment = Alignment.TopCenter,
    offset = IntOffset(0, -50), // 마커 머리 위로 올리기 조절
    onDismissRequest = { onDismiss() } // 바깥 누르면 닫히기
) {
    Surface(
        shape = cardShape,
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 이미지 및 텍스트 렌더링 구역
        }
    }
}
```


## 5. 검증

1. 지도 위의 마커를 클릭했을 때 엉뚱한 화면 중앙이 아니라, 클릭한 마커 대가리(머리) 바로 위에 오차 없이 카드가 안착하는지 확인.
2. 이미지가 없는 데이터의 경우 미리 준비한 카테고리 이모지 중심의 기본 플레이스홀더 이미지가 가독성 좋게 대치되는지 확인.
3. 지도를 빈 곳을 탁 쳤을 때 열려있던 팝업 카드가 버벅임 없이 싹 사라지는지 확인.

## 6. 비고

* 카카오 지도 SDK v2 안에서 마커 객체(`Label`) 자체의 클릭 이벤트를 구독하여 Compose의 `MapUiState.selectedDot` 상태로 안전하게 변환 및 브릿지 연결하는 것이 가장 큰 관전 포인트다.
