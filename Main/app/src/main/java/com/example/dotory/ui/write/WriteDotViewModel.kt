package com.example.dotory.ui.write

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WriteUiState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isLoading: Boolean = false
)

class WriteDotViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    fun initCoordinates(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude
        )
    }
}
