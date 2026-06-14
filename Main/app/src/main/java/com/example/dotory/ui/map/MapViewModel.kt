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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.os.Looper
import android.annotation.SuppressLint
import android.util.Log

enum class ZoomEvent {
    ZoomIn, ZoomOut
}

sealed interface MapNavigationEvent {
    data class NavigateToWriteDot(val latitude: Double, val longitude: Double) : MapNavigationEvent
    data class NavigateToEditDot(val dotId: Long) : MapNavigationEvent
}

class MapViewModel(private val repository: DotRepository) : ViewModel() {
    private val _selectedCategory = MutableStateFlow<DotCategory?>(null)
    private val _selectedDot = MutableStateFlow<Dot?>(null)
    private val _isAddMode = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _currentLocation = MutableStateFlow<LatLng>(LatLng.from(35.156927, 129.119642))
    val currentLocation: StateFlow<LatLng> = _currentLocation

    val uiState: StateFlow<MapUiState> = combine(
        repository.getAllDots(),
        _selectedCategory,
        _selectedDot,
        _isAddMode,
        _isLoading
    ) { dots, selectedCategory, selectedDot, isAddMode, isLoading ->
        val filtered = if (selectedCategory == null) {
            dots
        } else {
            dots.filter { it.category == selectedCategory }
        }
        MapUiState(
            dots = dots,
            selectedCategory = selectedCategory,
            filteredDots = filtered,
            selectedDot = selectedDot,
            isAddMode = isAddMode,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState()
    )

    private val _zoomEvent = MutableSharedFlow<ZoomEvent>()
    val zoomEvent: SharedFlow<ZoomEvent> = _zoomEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<MapNavigationEvent>()
    val navigationEvent: SharedFlow<MapNavigationEvent> = _navigationEvent.asSharedFlow()

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
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
        _isAddMode.value = !_isAddMode.value
    }

    // F-02 위치 선택 확정
    fun onLocationConfirmed(latLng: LatLng) {
        viewModelScope.launch {
            _navigationEvent.emit(MapNavigationEvent.NavigateToWriteDot(latLng.latitude, latLng.longitude))
            _isAddMode.value = false
        }
    }

    // F-04 카테고리 필터 선택
    fun selectCategory(category: DotCategory?) {
        _selectedCategory.value = category
    }

    // F-04 팝업 노출할 dot 선택
    fun selectDot(dot: Dot?) {
        _selectedDot.value = dot
    }

    // F-06 dot 수정 화면 전환 트리거
    fun editDot(dotId: Long) {
        viewModelScope.launch {
            _navigationEvent.emit(MapNavigationEvent.NavigateToEditDot(dotId))
        }
    }

    // F-04 dot 비동기 삭제 트리거
    fun deleteDot(dot: Dot) {
        viewModelScope.launch {
            setLoading(true)
            repository.deleteDot(dot)
            // 삭제 시 현재 팝업이 이 dot을 보고 있었다면 팝업 닫기
            if (_selectedDot.value?.id == dot.id) {
                selectDot(null)
            }
            setLoading(false)
        }
    }

    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = LatLng.from(latitude, longitude)
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate(fusedLocationClient: FusedLocationProviderClient) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d("MapViewModel", "Location request success: lat=${location.latitude}, lng=${location.longitude}")
                } else {
                    Log.d("MapViewModel", "Location request returned null")
                }
                updateCurrentLocation(35.156927, 129.119642)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
