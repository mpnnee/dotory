# Dotory — 나만의 사진 지도

Jetpack Compose 기반의 장소 기록 안드로이드 앱이다.
가본 곳을 지도 위에 dot으로 찍고, 사진·코멘트와 함께 기록한다.
본 문서는 프로젝트 전반의 작업 기준이다.

---

## 1. 프로젝트 개요

| 항목          | 값                           |
| ------------- | ---------------------------- |
| 언어          | Kotlin                       |
| UI 프레임워크 | Jetpack Compose + Material 3 |
| 최소 SDK      | 26                           |
| 타겟 SDK      | 34                           |
| 패키지        | `com.example.dotory`       |
| 앱 이름       | Dotory                       |

본 프로젝트는 두 단계로 구현된다.

- **Phase 1** — 지도 + 핵심 기록 기능 구현
- **Phase 2** — 관리 및 디테일 편집 기능 구현

---

## 2. 사용 Skills

| Skill                      | 적용 영역                   |
| -------------------------- | --------------------------- |
| mobile-android-design      | UI 컴포넌트, 레이아웃, 테마 |
| android-clean-architecture | 계층 분리, UseCase, DI      |

작업 호출 규칙:

- 새 UI Composable 작성 → mobile-android-design 패턴
- 새 데이터 흐름 작성 → android-clean-architecture 권고
- 두 Skill 충돌 시 → 디자인 표면은 mobile, 그 외는 clean

---

## 3. 핵심 의존성

```kotlin
// Compose
implementation(platform("androidx.compose:compose-bom:2024.10.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.activity:activity-compose:1.9.3")

// Lifecycle / ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

// Kakao Maps SDK v2 (핵심 변경 사항)
implementation("com.kakao.vectormap:android:2.11.1")

// Room DB (로컬 영구 저장용)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// 이미지 로딩 (갤러리 사진 표시용)
implementation("io.coil-kt:coil-compose:2.7.0")
```

플러그인: kotlin("kapt")

권한: INTERNET, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_MEDIA_IMAGES (Android 13+용) 또는 READ_EXTERNAL_STORAGE (구버전 대응)

---

## 4. 패키지 구조

```
com.example.dotory/
├── MainActivity.kt
├── DotoryApplication.kt       // 카카오 SDK 초기화
├── data/
│   ├── model/        Dot, DotCategory
│   ├── local/        DotDao, DotDatabase, DotEntity
│   └── repository/   DotRepository
├── ui/
│   ├── theme/        Color, Theme
│   ├── map/  
│   │   ├── MapScreen.kt       // 메인 지도, 상단 칩, 중앙 고정 핀 포함 
│   │   ├── MapViewModel.kt
│   │   └── components/
│   │       ├── DotCardPopup.kt      // 마커 클릭 시 뜨는 카드 
│   │       ├── CategoryFilterRow.kt // 상단 카테고리 칩 목록
│   │       └── LocationSelectBar.kt  // 하단 "여기에 추가" 바
│   ├── write/  
│   │   ├── WriteDotScreen.kt  // 작성 및 편집 통합 화면
│   │   └── WriteDotViewModel.kt
│   └── dialog/   
│       └── DotActionDialogs.kt // 롱탭 시 편집/삭제 분기 및 삭제 확인 다이얼로그 
└── util/             PermissionUtil, ImageUtil
```

---

## 5. 데이터 모델

```kotlin
// UI 모델 (Domain Model)
import com.kakao.vectormap.LatLng // 카카오 맵 좌표 매핑용 임포트 추가

data class Dot(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val title: String,           // 장소 이름 (예: "을지로 국밥집")
    val comment: String,         // 한 줄 코멘트 (최대 100자)
    val photoUri: String,        // 로컬 이미지 URI
    val category: DotCategory,
    val createdAt: Long          // System.currentTimeMillis()
) {
    // 카카오 지도 마커 표시를 위한 편의용 확장 함수 추가
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}

enum class DotCategory(val label: String, val emoji: String, val colorHex: Long) {
    FOOD("음식점", "🍣", 0xFFEE8130),
    CAFE("카페", "☕️", 0xFF6F4E37),
    TRAVEL("여행", "✈️", 0xFF6390F0),
    NATURE("자연", "🌿", 0xFF7AC74C),
    ETC("기타", "📍", 0xFFA8A77A)
}

// Room DB Entity
import androidx.room.Entity       // Room 어노테이션 명시
import androidx.room.PrimaryKey

@Entity(tableName = "dots")
data class DotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val comment: String,
    val photoUri: String,
    val category: String,        // DotCategory.name 형태로 저장
    val createdAt: Long
)
```

---

## 6. Room DB

### 6.1 DAO

```kotlin
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DotDao {
    @Query("SELECT * FROM dots ORDER BY createdAt DESC")
    fun getAllDots(): Flow<List<DotEntity>>

    @Query("SELECT * FROM dots WHERE category = :category ORDER BY createdAt DESC")
    fun getDotsByCategory(category: String): Flow<List<DotEntity>>

    // [수정/추가] 편집 화면 진입 시 기존 데이터를 ID로 조회하기 위한 단일 쿼리
    @Query("SELECT * FROM dots WHERE id = :id")
    suspend fun getDotById(id: Long): DotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDot(dot: DotEntity): Long

    @Update
    suspend fun updateDot(dot: DotEntity)

    @Delete
    suspend fun deleteDot(dot: DotEntity)
}
```

### 6.2 Database

```kotlin
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DotEntity::class], version = 1, exportSchema = false)
abstract class DotDatabase : RoomDatabase() {
    abstract fun dotDao(): DotDao

    companion object {
        @Volatile private var INSTANCE: DotDatabase? = null
        fun getDatabase(context: Context): DotDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext, // 메모리 누수 방지를 위해 applicationContext 사용 권장
                    DotDatabase::class.java, 
                    "dot_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
```

---

## 7. Phase 1 — 지도 + dot 기록

### 7.1 메인 화면 — `MapScreen`

- `Scaffold` + `TopAppBar`("Dotory")
- 본문은 컴포즈용 'KakaoMap' 컴포저블을 전체화면으로 배치
- 저장된 dot마다 카테고리 색상('DotCategory.colorHex')의 동그라미 마커로 표시
- 우하단 `FloatingActionButton` → 클릭 시 위치 지정 모드 `(isAddMode = true)`로 전환
- 상단에 `CategoryFilterRow` (`LazyRow` 칩) — 음식점·카페·여행·자연·기타
- 필터 칩 기본값은 전체 표시, 탭하면 해당 카테고리만 표시, 다시 탭하면 전체로 복귀

### 7.2 위치 지정 모드

- `isAddMode == true` 일 때 화면 동작 분기
- 지도 중앙에 빨간색 핀 고정 및 지도를 드래그하여 원하는 위치에 핀 맞추기
- 하단에 "여기에 추가" 버튼 팝업 표시
- "여기에 추가" 버튼 탭 → 현재 지도 중앙 좌표(`CameraUpdate'` 중심점)를 가지고 `WriteDotScreen`으로 이동

### 7.3 dot 작성 화면 — `WriteDotScreen`

- 입력 항목:
  - 장소 이름 (`OutlinedTextField`)
  - 사진 선택 (갤러리 런처 — `rememberLauncherForActivityResult`)
  - 코멘트 (`OutlinedTextField`, 최대 100자 제한 적용)
  - 카테고리 선택 (가로 칩 나열, 선택 시 이모지가 앞에 오도록 `"${category.emoji} ${category.label}"` 형태로 UI 표시)
- 저장 버튼 → `DotRepository.insertDot()` 호출 → `MapScreen`으로 복귀 및 `isAddMode = false` 초기화ㅣ

### 7.4 dot 카드 팝업 — `DotCardPopup`

- 지도에서 마커 탭 → 마커 바로 위에 커스텀 뷰 팝업으로 표시
- 표시 항목: 사진(`AsyncImage`, `ContentScale.Crop`), 장소 이름(Bold), 코멘트, 카테고리 뱃지
- 팝업 하단에 dot을 가리키는 꼬리(tail) 연결
- 카드 외부 탭 시 팝업 닫힘
- 마커 롱탭 시 편집/삭제 분기 다이얼로그(`DotActionDialogs`) 표시
- 삭제 선택 → `AlertDialog` 확인 후 `DotRepository.deleteDot()` 호출 및 팝업 닫기

### 7.5 카테고리 필터 및 상태 관리

- `MapViewModel`이 `selectedCategory: DotCategory?` 상태를 관리
- `filteredDots`는 `dots`와 `selectedCategory`를 조합한 양방향 데이터 바인딩 상태로 관리

```kotlin
```kotlin
data class MapUiState(
    val dots: List<Dot> = emptyList(),
    val selectedCategory: DotCategory? = null,
    val selectedDot: Dot? = null,        // 카드 팝업 대상
    val isAddMode: Boolean = false,      // 위치 지정 모드 활성화 여부
    val isLoading: Boolean = false
)
```

---

## 8. Phase 2 — dot 편집

### 8.1 dot 상세 편집 및 수정

* 마커 롱클릭 후 **[편집하기]** 선택 → 선택된 `Dot` 객체(또는 ID)를 들고 `WriteDotScreen`으로 이동
* `WriteDotScreen` 진입 시 기존 데이터가 각 텍스트 필드와 이미지 영역에 자동으로 채워짐
* 수정 완료 후 저장 버튼 탭 → `DotRepository.updateDot()` 호출하여 DB 업데이트 후 `MapScreen`으로 복귀

---

## 9. 디자인 원칙

- 지도는 심플하게 배경처럼 표시, dot이 포인트로 보이도록 한다.
- dot 마커는 카테고리별 색상 동그라미 (`Canvas` 또는 KakaoMap 커스텀 `Label` 활용).
- 카드 팝업: 흰 배경 + 둥근 모서리 + 하단 꼬리 모양 (`Popup` 컴포저블 활용).
- 카테고리 칩: 카테고리 고유 색 + 흰 글씨, `RoundedCornerShape(50)`.
- 사진 없을 때는 카테고리 이모지를 중앙에 표시하는 플레이스홀더 구성.
- 색상 하드코딩 금지 — `MaterialTheme.colorScheme` 또는 `DotCategory.colorHex` 사용.

---

## 10. 구현 순서

1. 프로젝트 생성 + 의존성 + 카카오맵 API 키 및 `DotoryApplication` 설정 + 빌드 확인
2. 데이터 모델 (`Dot`, `DotCategory`, `DotEntity`)
3. Room DB (`DotDao`, `DotDatabase`, `DotRepository`)
4. `MapViewModel` + `MapUiState`
5. `MapScreen` — 카카오맵 `KakaoMap` 컴포저블 표시
6. 위치 지정 모드 — 중앙 고정 핀 + 하단 "여기에 추가" 바 구현
7. `WriteDotScreen` — 갤러리 사진 선택 + 새 dot 저장 로직 구현
8. dot 마커 표시 + `CategoryFilterRow` + 필터링 연동 로직
9. `DotCardPopup` — 커스텀 카드 팝업 표시 + `DotActionDialogs`를 통한 삭제 기능 구현
10. 디자인 마감 (카테고리 색상·이모지가 앞에 오는 칩·플레이스홀더)
11. Phase 2 — `WriteDotScreen`을 활용한 기존 dot 상세 편집 모드 구현

※ 각 단계 완료 시 빌드와 에뮬레이터 동작을 확인한 뒤 다음 단계로 진행한다.

---

## 11. 코딩 규칙

- 모든 Composable에 `@Composable` 어노테이션 부여 및 가능하면 `@Preview` 동반.
- UI 상태는 ViewModel의 단일 `StateFlow<UiState>` 구조로만 노출한다.
- 이미지 URI는 `String`으로 저장하며, 이미지 로딩 및 캐싱은 Coil의 `AsyncImage`를 사용한다.
- 권한 요청(`INTERNET`, 위치 권한 등)은 `util/PermissionUtil.kt`에서 일괄 관리한다.
- 색상 하드코딩 금지 — 반드시 `MaterialTheme.colorScheme` 또는 `DotCategory.colorHex`를 활용한다.
- 매직 넘버는 `dp`, `sp` 또는 명명된 상수로 분리하여 정의한다.
- 한글 주석 사용을 적극 허용한다. 단, 함수·변수명은 명확한 영문으로 작성한다.
