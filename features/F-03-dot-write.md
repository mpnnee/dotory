
# FR-03 — dot 작성 및 저장

| 항목              | 값                                                |
| ----------------- | ------------------------------------------------- |
| 우선순위          | High                                              |
| 예상 변경 파일 수 | 4~5                                               |
| 신규 의존성       | `io.coil-kt:coil-compose:2.7.0`, Room DB 의존성 |
| 선행 조건         | `FR-02` 완료                                    |

---

## 1. 목표

사용자가 전달받은 좌표 위에 사진, 코멘트, 카테고리를 채워 넣어 최종 dot 데이터를 생성하고, 이를 Room 로컬 DB에 안전하게 영구 저장한다.

## 2. 인수 기준 (Acceptance Criteria)

- [ ] 갤러리 런처를 활용해 사용자가 스마트폰 내부의 로컬 사진을 정상적으로 선택할 수 있다.
- [ ] 장소 이름(`title`)과 코멘트(`comment`)를 입력받는 텍스트 필드를 제공하며, 코멘트는 최대 100자까지만 입력되도록 제한한다.
- [ ] "음식점, 카페, 여행, 자연, 기타" 5가지 카테고리 선택 기능을 칩 형태로 제공한다.
- [ ] 카테고리 UI는 반드시 이모지가 앞에 오는 `"${category.emoji} ${category.label}"` 형식으로 빌드한다.
- [ ] [저장] 버튼 선택 시 Room DB에 데이터가 들어가고 메인 지도 화면으로 자동으로 복귀한다.

## 3. 변경 범위

### 신규 파일

- `ui/write/WriteDotScreen.kt` — 작성 및 편집을 담당하는 폼 레이아웃 화면
- `ui/write/WriteDotViewModel.kt` — 입력 필드 상태 관리 및 DB 저장 트리거 뷰모델
- `data/local/DotEntity.kt` / `DotDao.kt` / `DotDatabase.kt` — Room DB 기반 핵심 파일들
- `data/repository/DotRepository.kt` — 데이터 소스 접근을 추상화하는 레포지토리 클래스

### 수정 파일

- `MainActivity.kt` — 네비게이션 경로에 `WriteDotScreen` 추가 및 선택된 위도/경도 파라미터 전달 설정

## 4. 구현 가이드

### 4.1 글자 수 제한 및 이모지 선행 매핑

코멘트 필드 작성 시 `maxLines` 제어와 글자 수 필터링을 걸어주고, 카테고리는 미리 정의된 `DotCategory` enum 구조를 순회하며 렌더링한다.

```kotlin
// WriteDotScreen.kt 내부 구현 가이드
OutlinedTextField(
    value = comment,
    onValueChange = { if (it.length <= 100) comment = it }, // 100자 제한
    label = { Text("코멘트 (최대 100자)") }
)

// 카테고리 칩 가로 배치
Row {
    DotCategory.values().forEach { category ->
        FilterChip(
            selected = (selectedCategory == category),
            onClick = { selectedCategory = category },
            label = { Text("${category.emoji} ${category.label}") } // 이모지 앞으로!
        )
    }
}
```

## 5. 검증

1. 코멘트 입력창에 글자를 마구 쳤을 때 100자가 넘어가는 순간 더 이상 글자가 입력되지 않는지 확인.
2. 카테고리 선택 칩들이 은영님이 의도한 대로 `[ 🍣 음식점 ]`, `[ ☕ 카페 ]` 형태로 이모지가 깔끔하게 전면에 잘 나오는지 확인.
3. 저장 버튼을 누르고 메인 지도로 돌아왔을 때, 앱이 꺼지지 않고 정상적으로 홈 화면 스택으로 복귀하는지 확인.

## 6. 비고

* 갤러리에서 가져온 사진의 URI 권한(`photoUri`)이 앱 재시작 후에도 유실되지 않도록 테이크 퍼시스턴트(Take Persistent) 권한 처리를 고려한다.
* Room DB 저장 작업은 UI 스레드를 멈추지 않도록 코루틴 스코프(`suspend fun`) 내에서 비동기로 처리한다.

```

```
