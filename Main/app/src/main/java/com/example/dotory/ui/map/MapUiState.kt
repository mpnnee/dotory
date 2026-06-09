package com.example.dotory.ui.map

import com.example.dotory.data.model.Dot
import com.example.dotory.data.model.DotCategory

data class MapUiState(
    val dots: List<Dot> = emptyList(),
    val selectedCategory: DotCategory? = null,
    val selectedDot: Dot? = null,        // 카드 팝업 대상
    val isAddMode: Boolean = false,      // 위치 지정 모드 활성화 여부
    val isLoading: Boolean = false
)
