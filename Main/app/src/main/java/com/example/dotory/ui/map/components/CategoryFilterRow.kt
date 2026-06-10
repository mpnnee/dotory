package com.example.dotory.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dotory.data.model.DotCategory

@Composable
fun CategoryFilterRow(
    selectedCategory: DotCategory?,
    onCategorySelected: (DotCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(DotCategory.entries) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category)
                    }
                },
                label = {
                    Text(text = "${category.emoji} ${category.label}")
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,               // 선택되지 않았을 때 흰색 배경
                    labelColor = Color.Black,                   // 선택되지 않았을 때 검은색 글자
                    selectedContainerColor = Color(0xFF503A34), // 선택되었을 때 시그니처 갈색
                    selectedLabelColor = Color.White             // 선택되었을 때 흰색 글자
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFF503A34).copy(alpha = 0.2f),
                    selectedBorderColor = Color(0xFF503A34)
                ),
                elevation = FilterChipDefaults.filterChipElevation(
                    elevation = if (isSelected) 6.dp else 4.dp
                )
            )
        }
    }
}
