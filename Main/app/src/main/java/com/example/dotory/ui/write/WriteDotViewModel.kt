package com.example.dotory.ui.write

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dotory.data.model.Dot
import com.example.dotory.data.model.DotCategory
import com.example.dotory.data.repository.DotRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WriteUiState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "",
    val comment: String = "",
    val photoUri: String = "",
    val category: DotCategory = DotCategory.FOOD,
    val isSaveSuccess: Boolean = false,
    val isLoading: Boolean = false
)

class WriteDotViewModel(private val repository: DotRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    private val _saveEvent = MutableSharedFlow<Unit>()
    val saveEvent: SharedFlow<Unit> = _saveEvent.asSharedFlow()

    fun initCoordinates(latitude: Double, longitude: Double) {
        _uiState.value = WriteUiState(
            latitude = latitude,
            longitude = longitude
        )
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onCommentChange(comment: String) {
        if (comment.length <= 100) {
            _uiState.value = _uiState.value.copy(comment = comment)
        }
    }

    fun onPhotoUriChange(uri: String) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun onCategoryChange(category: DotCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun saveDot() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val newDot = Dot(
                latitude = state.latitude,
                longitude = state.longitude,
                title = state.title,
                comment = state.comment,
                photoUri = state.photoUri,
                category = state.category,
                createdAt = System.currentTimeMillis()
            )
            repository.insertDot(newDot)
            _uiState.value = _uiState.value.copy(isLoading = false, isSaveSuccess = true)
            _saveEvent.emit(Unit)
        }
    }
}

class WriteDotViewModelFactory(private val repository: DotRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WriteDotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WriteDotViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
