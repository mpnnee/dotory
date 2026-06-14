package com.example.dotory.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.zIndex
import com.example.dotory.data.model.Dot
import com.example.dotory.ui.dialog.DeleteConfirmDialog
import com.example.dotory.ui.dialog.DotActionDialog
import com.example.dotory.ui.map.components.CategoryFilterRow
import com.example.dotory.ui.map.components.DotCardPopup
import com.example.dotory.ui.map.components.LocationSelectBar
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var kakaoMapInstance by remember { mutableStateOf<KakaoMap?>(null) }
    var currentCameraCenter by remember { mutableStateOf<LatLng?>(null) }
    var selectedDotScreenPoint by remember { mutableStateOf<android.graphics.Point?>(null) }

    // 선택된 dot의 스크린 좌표 실시간 업데이트를 위한 LaunchedEffect (드래그/줌 시 마커를 따라 매끄럽게 이동)
    LaunchedEffect(uiState.selectedDot, kakaoMapInstance) {
        val selectedDot = uiState.selectedDot
        val map = kakaoMapInstance
        if (selectedDot != null && map != null) {
            // 매 프레임마다 마커의 화면상 픽셀 좌표를 갱신하여 지도 이동 시 한 몸처럼 스르륵 이동하도록 함
            while (true) {
                selectedDotScreenPoint = map.toScreenPoint(selectedDot.toLatLng())
                withFrameNanos { }
            }
        } else {
            selectedDotScreenPoint = null
        }
    }

    // 마커 삭제 및 액션을 위한 상태 변수
    var showDeleteDialog by remember { mutableStateOf<Dot?>(null) }
    var showActionDialog by remember { mutableStateOf<Dot?>(null) }



    // 뷰모델의 줌 이벤트를 감지하여 카메라 조작
    LaunchedEffect(viewModel) {
        viewModel.zoomEvent.collect { event ->
            kakaoMapInstance?.let { map ->
                when (event) {
                    ZoomEvent.ZoomIn -> map.moveCamera(CameraUpdateFactory.zoomIn())
                    ZoomEvent.ZoomOut -> map.moveCamera(CameraUpdateFactory.zoomOut())
                }
            }
        }
    }

    // DB 데이터 혹은 선택 카테고리 변경 시 지도 마커 갱신
    LaunchedEffect(uiState.filteredDots, kakaoMapInstance) {
        val map = kakaoMapInstance ?: return@LaunchedEffect
        val layer = map.labelManager?.layer ?: return@LaunchedEffect

        // 기존 라벨을 모두 삭제
        layer.removeAll()

        // 마커 생성 및 맵 추가
        uiState.filteredDots.forEach { dot ->
            val colorArgb = Color(dot.category.colorHex).toArgb()
            
            // 줌 레벨별 마커 비트맵 3종 동적 생성
            val smallBitmap = createCircleMarkerBitmap(context, colorArgb, size = 18, strokeWidth = 2.5f)
            val mediumBitmap = createCircleMarkerBitmap(context, colorArgb, size = 34, strokeWidth = 4.5f)
            val largeBitmap = createCircleMarkerBitmap(context, colorArgb, size = 50, strokeWidth = 6f)
            
            // 라벨 스타일 등록 및 생성 (줌 레벨에 따른 스타일 매핑 포함)
            val styles = map.labelManager?.addLabelStyles(
                LabelStyles.from(
                    "style_${dot.category.name}_${dot.id}",
                    LabelStyle.from(smallBitmap).apply { setZoomLevel(0) },      // 0 ~ 12 레벨: 작게 축소
                    LabelStyle.from(mediumBitmap).apply { setZoomLevel(13) },   // 13 ~ 15 레벨: 중간
                    LabelStyle.from(largeBitmap).apply { setZoomLevel(16) }     // 16 레벨 이상: 크게 확대
                )
            )
            val options = LabelOptions.from(dot.toLatLng()).setStyles(styles).setTag(dot.id)
            layer.addLabel(options)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 지도 뷰
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    start(object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            // 지도 API 종료 시 호출
                        }

                        override fun onMapError(error: Exception?) {
                            // 에러 발생 시 호출
                        }
                    }, object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            kakaoMapInstance = kakaoMap
                            currentCameraCenter = kakaoMap.cameraPosition?.position

                            // 카메라 이동 종료 시 좌표 갱신 리스너 부착
                            kakaoMap.setOnCameraMoveEndListener { _, cameraPosition, _ ->
                                currentCameraCenter = cameraPosition.position
                            }

                            // 마커 클릭 리스너 설정
                            kakaoMap.setOnLabelClickListener { _, _, label ->
                                val dotId = label.tag as? Long
                                if (dotId != null) {
                                    val dot = uiState.dots.firstOrNull { it.id == dotId }
                                    if (dot != null) {
                                        viewModel.selectDot(dot)
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            }

                            // 마커 및 지도 롱클릭 리스너 설정 (편집/삭제 다이얼로그 노출)
                            kakaoMap.setOnTerrainLongClickListener { _, _, point ->
                                val touchX = point.x
                                val touchY = point.y
                                val thresholdPx = 60f // 터치 오차 범위
                                
                                val clickedLabelDot = viewModel.uiState.value.dots.firstOrNull { dot ->
                                    val screenPoint = kakaoMap.toScreenPoint(dot.toLatLng())
                                    if (screenPoint != null) {
                                        val dx = screenPoint.x - touchX
                                        val dy = screenPoint.y - touchY
                                        (dx * dx + dy * dy) <= (thresholdPx * thresholdPx)
                                    } else {
                                        false
                                    }
                                }
                                
                                if (clickedLabelDot != null) {
                                    showActionDialog = clickedLabelDot
                                }
                            }

                            // 지도 빈 영역 클릭 시 팝업 닫기
                            kakaoMap.setOnMapClickListener { _, _, _, _ ->
                                viewModel.selectDot(null)
                            }
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize().zIndex(1f)
        )

        // 1. 위치 지정 모드 정중앙 고정 핀 표시
        if (uiState.isAddMode) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "선택 핀",
                tint = Color.Red,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-24).dp) // 핀 하단 끝점을 정중앙에 조준하기 위한 디테일 오프셋
                    .zIndex(2f)
            )
        }

        // 2. 상단 카테고리 필터 칩 바 (위치 지정 모드가 아닐 때만 노출)
        if (!uiState.isAddMode) {
            CategoryFilterRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp) // 시스템 스테이터스 바 공간 회피를 위한 여백
                    .zIndex(2f)
            )
        }

        // 우측 하단 줌 컨트롤 버튼 레이아웃
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                // 추가 모드일 때는 하단에 위치 선택 바가 들어오므로 줌 버튼의 bottom 여백을 더 주어 겹치지 않게 조절
                .padding(
                    end = 16.dp,
                    bottom = if (uiState.isAddMode) 220.dp else 32.dp
                )
                .zIndex(2f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { viewModel.zoomIn() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "확대",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.width(24.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                IconButton(
                    onClick = { viewModel.zoomOut() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "축소",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 3. 위치 추가 모드 진입용 FAB (추가 모드가 아닐 때만 노출)
        if (!uiState.isAddMode) {
            FloatingActionButton(
                onClick = { viewModel.toggleAddMode() },
                shape = CircleShape,
                containerColor = Color(0xFF503A34),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = 32.dp)
                    .zIndex(2f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "위치 추가 모드 전환"
                )
            }
        }

        // 4. 하단 "여기에 추가" 버튼 바 배치 (추가 모드일 때만 노출)
        if (uiState.isAddMode) {
            LocationSelectBar(
                onAddClick = {
                    currentCameraCenter?.let { center ->
                        viewModel.onLocationConfirmed(center)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
            )
        }

        // 5. 마커 클릭 시 나타나는 장소 정보 카드 팝업
        uiState.selectedDot?.let { dot ->
            selectedDotScreenPoint?.let { point ->
                val currentZoomLevel = kakaoMapInstance?.zoomLevel ?: 15
                val markerRadiusPx = when {
                    currentZoomLevel <= 12 -> 9
                    currentZoomLevel in 13..15 -> 17
                    else -> 25
                }

                DotCardPopup(
                    dot = dot,
                    screenPoint = point,
                    modifier = Modifier.zIndex(1f),
                    markerRadiusPx = markerRadiusPx,
                    onDismiss = { viewModel.selectDot(null) },
                    onEditClick = {
                        viewModel.selectDot(null)
                        viewModel.editDot(dot.id)
                    },
                    onDeleteClick = {
                        showDeleteDialog = dot
                    }
                )
            }
        }

        // 6. 도토리 삭제 확인 다이얼로그
        showDeleteDialog?.let { dot ->
            DeleteConfirmDialog(
                dot = dot,
                onConfirm = {
                    viewModel.deleteDot(dot)
                    showDeleteDialog = null
                },
                onDismiss = {
                    showDeleteDialog = null
                }
            )
        }

        // 7. 기록 관리 분기 다이얼로그 (편집/삭제 분기 선택)
        showActionDialog?.let { dot ->
            DotActionDialog(
                onEditClick = {
                    showActionDialog = null
                    viewModel.editDot(dot.id)
                },
                onDeleteClick = {
                    showActionDialog = null
                    showDeleteDialog = dot
                },
                onDismiss = {
                    showActionDialog = null
                }
            )
        }
    }
}

/**
 * 카테고리 색상을 반영한 둥근 모양의 비트맵 마커를 생성합니다.
 */
private fun createCircleMarkerBitmap(context: Context, color: Int, size: Int = 48, strokeWidth: Float = 6f): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
    }
    val radius = size / 2f
    val strokeOffset = strokeWidth / 2f
    canvas.drawCircle(radius, radius, radius - strokeOffset, paint)
    canvas.drawCircle(radius, radius, radius - strokeOffset, strokePaint)
    return bitmap
}
