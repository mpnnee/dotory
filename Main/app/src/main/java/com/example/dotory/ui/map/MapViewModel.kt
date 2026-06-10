package com.example.dotory.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ZoomEvent {
    ZoomIn, ZoomOut
}

sealed interface MapNavigationEvent {
    data class NavigateToWriteDot(val latitude: Double, val longitude: Double) : MapNavigationEvent
}

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _zoomEvent = MutableSharedFlow<ZoomEvent>()
    val zoomEvent: SharedFlow<ZoomEvent> = _zoomEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<MapNavigationEvent>()
    val navigationEvent: SharedFlow<MapNavigationEvent> = _navigationEvent.asSharedFlow()

    // F-01 단계의 기본 상태 설정
    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    fun zoomIn() {
        viewModelScope.launch {
            _zoomEvent.emit(ZoomEvent.ZoomIn)
        }
    }

    fun zoomOut() {
        viewModelScope.launch {
            _zoomEvent.emit(ZoomEvent.ZoomOut)
        }
    }

    // F-02 위치 지정 모드 토글
    fun toggleAddMode() {
        _uiState.value = _uiState.value.copy(isAddMode = !_uiState.value.isAddMode)
    }

    // F-02 위치 선택 확정
    fun onLocationConfirmed(latLng: LatLng) {
        viewModelScope.launch {
            _navigationEvent.emit(MapNavigationEvent.NavigateToWriteDot(latLng.latitude, latLng.longitude))
            _uiState.value = _uiState.value.copy(isAddMode = false)
        }
    }
}
