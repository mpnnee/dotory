package com.example.dotory.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dotory.data.model.Dot
import com.example.dotory.data.model.DotCategory
import com.example.dotory.data.repository.DotRepository
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ZoomEvent {
    ZoomIn, ZoomOut
}

sealed interface MapNavigationEvent {
    data class NavigateToWriteDot(val latitude: Double, val longitude: Double) : MapNavigationEvent
    data class NavigateToEditDot(val dotId: Long) : MapNavigationEvent
}

class MapViewModel(private val repository: DotRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _zoomEvent = MutableSharedFlow<ZoomEvent>()
    val zoomEvent: SharedFlow<ZoomEvent> = _zoomEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<MapNavigationEvent>()
    val navigationEvent: SharedFlow<MapNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        // 로컬 DB의 dot 목록 실시간 구독
        viewModelScope.launch {
            repository.getAllDots().collect { allDots ->
                _uiState.update { it.copy(dots = allDots) }
            }
        }
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
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
        _uiState.update { it.copy(isAddMode = !it.isAddMode) }
    }

    // F-02 위치 선택 확정
    fun onLocationConfirmed(latLng: LatLng) {
        viewModelScope.launch {
            _navigationEvent.emit(MapNavigationEvent.NavigateToWriteDot(latLng.latitude, latLng.longitude))
            _uiState.update { it.copy(isAddMode = false) }
        }
    }

    // F-04 카테고리 필터 선택
    fun selectCategory(category: DotCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    // F-04 팝업 노출할 dot 선택
    fun selectDot(dot: Dot?) {
        _uiState.update { it.copy(selectedDot = dot) }
    }

    // F-04 dot 비동기 삭제 트리거
    fun deleteDot(dot: Dot) {
        viewModelScope.launch {
            setLoading(true)
            repository.deleteDot(dot)
            // 삭제 시 현재 팝업이 이 dot을 보고 있었다면 팝업 닫기
            if (_uiState.value.selectedDot?.id == dot.id) {
                selectDot(null)
            }
            setLoading(false)
        }
    }
}

class MapViewModelFactory(private val repository: DotRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
