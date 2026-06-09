# F-01 — 카카오맵 표시

| 항목              | 값                                                   |
| ----------------- | ---------------------------------------------------- |
| 우선순위          | High                                                 |
| 예상 변경 파일 수 | 4~5                                                  |
| 신규 의존성       | `com.kakao.vectormap:android:2.11.1`               |
| 선행 조건         | 초기 빈 Android (Compose) 프로젝트 생성 및 빌드 확인 |

---

## 1. 목표

앱 실행 시 지도를 전체 화면으로 표시하고, 저장된 dot들이 지도 위에 마커로 보일 수 있도록 카카오 지도 SDK v2 기반의 베이스를 다진다.

## 2. 인수 기준 (Acceptance Criteria)

- [ ] 앱 실행 시 지도가 전체 화면에 1초 이내로 표시된다.
- [ ] 지도를 핀치, 스크롤로 자유롭게 탐색할 수 있다.
- [ ] Jetpack Compose 환경에서 카카오 지도 SDK v2의 `KakaoMap` 컴포저블을 안정적으로 렌더링한다.

- [ ] 화면 우측에 지도를 확대할 수 있는 `+` 버튼과 축소할 수 있는 `-` 버튼(줌 컨트롤러)이 세로로 배치된다.
- [ ] 각 버튼을 탭하면 현재 지도 카메라의 줌 레벨(Zoom Level)이 한 단계씩 부드럽게 확대/축소된다.

## 3. 변경 범위

### 신규 파일

* `DotoryApplication.kt` — 카카오 지도 SDK 초기화를 위한 애플리케이션 클래스
* `ui/map/MapScreen.kt` — 메인 지도 및 상단 필터 칩 뷰 레이아웃 컴포저블
* `ui/map/MapViewModel.kt` — 지도 상태 및 데이터 스트림 관리용 뷰모델

### 수정 파일

* `settings.gradle.kts` — 카카오 마벤(Maven) 저장소 레포지토리 주소 추가
* `build.gradle.kts (Module :app)` — 카카오 지도 SDK v2 및 라이프사이클 의존성 추가
* `AndroidManifest.xml` — 카카오 맵 API 네이티브 키 설정 및 인터넷/위치 권한 등록
* `MainActivity.kt` — `MapScreen` 진입점 설정 및 Compose 테마 래핑

## 4. 구현 가이드

### 4.1 SDK 초기화 및 매니페스트 설정

카카오 지도 v2를 연동하기 위해 최상위 Application 클래스에서 초기화를 수행하고, 메타데이터에 발급받은 네이티브 앱 키를 명시한다.

```kotlin
// DotoryApplication.kt
class DotoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 카카오 지도 SDK v2 초기화
        KakaoMapSdk.init(this, "YOUR_KAKAO_NATIVE_APP_KEY")
    }
}
```

```
<manifest xmlns:android="http://schemas.microsoft.com/apk/Polygon">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  
    <application
        android:name=".DotoryApplication"
        ...>
        <meta-data
            android:name="com.kakao.vectormap.APP_KEY"
            android:value="YOUR_KAKAO_NATIVE_APP_KEY" />
    </application>
</manifest>
```

### 4.2 메인 지도 렌더링 가이드

`MapScreen` 컴포저블 내에 전체화면 영역을 확보하고, 카카오 맵 컴포저블을 배치하여 지도가 심플하게 배경처럼 깔리도록 구성한다.

```
// ui/map/MapScreen.kt 내부 선언 가이드
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 카카오 지도 v2 컴포저블 표시 구현 구역
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    // 지도 로딩 및 베이스 설정 레이아웃 구성
                }
            },
            modifier = Modifier.fillMaxSize()
        )
  
        // TODO: FR-05 상단 카테고리 필터 칩 가로 레이아웃 위치
    }
}
```

```
// ui/map/MapScreen.kt 내부 줌 컨트롤러 배치 가이드
Box(modifier = Modifier.fillMaxSize()) {
    AndroidView( ... )

    // 우측 중앙 혹은 우하단에 줌 버튼 세로 정렬 배치
    Column(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = { viewModel.zoomIn() }) { ... } // 확대
        IconButton(onClick = { viewModel.zoomOut() }) { ... } // 축소
    }
}
```

## 5. 검증

1. 앱 실행 즉시 크래시(앱 종료) 없이 카카오 지도 타일이 전체 화면에 1초 이내로 부드럽게 로딩되는지 확인.
2. 손가락으로 지도를 드래그(스크롤)하거나 핀치 줌(확대/축소)을 했을 때 끊김 없이 탐색이 가능한지 확인.
3. 로그캣(Logcat) 창에 카카오 SDK 인증 에러(`AuthError`)나 권한 거부 로그가 찍히지 않는지 체크.

## 6. 비고

* 본 단계에서는 카카오맵 API 키 세팅과 빈 화면에 지도를 올바르게 띄우는 베이스 빌드 검증에 집중한다.
* 데이터베이스(Room)와 연결하여 실제 마커를 찍는 연동 작업은 데이터 모델링 및 DAO 구축(`FR-02`, `FR-03`)이 완료된 이후 순차적으로 진행한다.
