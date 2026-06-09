
# FR-06 — dot 편집 및 삭제

| 항목              | 값                                  |
| ----------------- | ----------------------------------- |
| 우선순위          | High                                |
| 예상 변경 파일 수 | 4                                   |
| 신규 의존성       | 없음                                |
| 선행 조건         | Phase 1 핵심 데이터 파이프라인 완비 |

---

## 1. 목표

이미 맵 상에 영구 저장된 나만의 dot 기록을 길게 눌러(롱클릭) 마음에 안 드는 내용을 통째로 편집하여 갱신하거나 아예 영구 삭제할 수 있는 뒤처리 관리 기능을 구현한다.

## 2. 인수 기준 (Acceptance Criteria)

- [ ] 지도 위에 생성된 마커를 1초 이상 길게 누르면(롱탭) 화면 중앙에 "편집하기 / 삭제하기" 분기 선택 팝업 다이얼로그(`DotActionDialogs`)가 나타난다.
- [ ] **[편집하기] 선택 시:** 해당 dot의 기존 텍스트와 사진이 빈칸 없이 100% 꽉 채워진 상태로 `WriteDotScreen` 양식 화면으로 진입한다.
- [ ] 편집창에서 내용을 수정한 뒤 저장 버튼을 누르면 `DotRepository.updateDot()`이 작동해 데이터가 바뀌고 지도로 복귀한다.
- [ ] **[삭제하기] 선택 시:** "진짜로 삭제하시겠습니까?" 라는 안전장치용 최종 컨펌 다이얼로그를 거친다.
- [ ] 최종 삭제 컨펌 시 DB에서 영구 삭제 처리(`deleteDot()`)를 수행하며 해당 마커는 즉시 지도 위에서 소멸한다.

## 3. 변경 범위

### 신규 파일

- `ui/dialog/DotActionDialogs.kt` — 편집/삭제 분기 및 삭제 경고 확인용 커스텀 다이얼로그 컴포넌트 묶음

### 수정 파일

- `data/local/DotDao.kt` — 지정 ID 기반 단일 행 조회를 위한 `getDotById()` 및 `@Update`, `@Delete` 쿼리 바인딩 확인
- `ui/map/MapScreen.kt` — 카카오맵 마커의 롱클릭 리스너 이벤트 가로채기 연결 구문 추가
- `ui/write/WriteDotScreen.kt` / `WriteDotViewModel.kt` — 진입 시 넘겨받은 특정 Dot ID가 존재할 경우 수정 모드로 자동 세팅되는 전환 분기 추가

## 4. 구현 가이드

### 4.1 롱클릭 연동 및 분기 처리

카카오 지도 v2의 롱클릭 리스너를 연동해 다이얼로그 창을 띄워주고, 편집 모드 진입 시 뷰모델에 기존 상태를 강제로 바인딩시킨다.

```kotlin
// DotActionDialogs.kt 구현 가이드
@Composable
fun DotActionDialog(
    onEditClick = { /* 뷰모델을 통해 해당 ID를 들고 WriteDotScreen 이동 처리 */ },
    onDeleteClick = { /* 삭제 경고 다이얼로그 한 단 더 띄우기 */ },
    onDismiss = { /* 창 닫기 */ }
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("기록 관리") },
        text = { Text("이 장소의 기록을 어떻게 처리할까요?") },
        confirmButton = { TextButton(onClick = onEditClick) { Text("편집하기") } },
        dismissButton = { TextButton(onClick = onDeleteClick) { Text("삭제하기") } }
    )
}
```


## 5. 검증

1. 마커를 길게 꾹 눌렀을 때 "편집/삭제" 분기 창이 딜레이 없이 단단하게 노출되는지 확인.
2. 편집창에 들어갔을 때 내가 예전에 입력했던 을지로 국밥집 장소 이름과 사진, 코멘트 100자가 그대로 로딩되어 빈칸 없이 채워져 있는지 확인.
3. 삭제를 완료하고 메인 지도로 튕겨 나왔을 때, 방금 지운 그 장소의 마커가 지도 위에서 귀신같이 흔적도 없이 사라졌는지 확인.

## 6. 비고

* 작성 화면(`WriteDotScreen`) 하나를 가지고 '새 글 쓰기' 모드와 '기존 글 수정' 모드를 완벽하게 공용 재사용(`Reuse`)할 수 있도록 아키텍처 다리를 매끄럽게 놓아주는 것이 이번 프로젝트의 최종 클라이맥스 핵심 공략이다.

```

```
