package com.example.dotory.ui.map

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dotory.ui.map.components.LocationSelectBar
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var kakaoMapInstance by remember { mutableStateOf<KakaoMap?>(null) }
    var currentCameraCenter by remember { mutableStateOf<LatLng?>(null) }

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
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
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

        // 2. 위치 추가 모드 진입용 FAB (추가 모드가 아닐 때만 노출)
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
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "위치 추가 모드 전환"
                )
            }
        }

        // 3. 하단 "여기에 추가" 버튼 바 배치 (추가 모드일 때만 노출)
        if (uiState.isAddMode) {
            LocationSelectBar(
                onAddClick = {
                    currentCameraCenter?.let { center ->
                        viewModel.onLocationConfirmed(center)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
