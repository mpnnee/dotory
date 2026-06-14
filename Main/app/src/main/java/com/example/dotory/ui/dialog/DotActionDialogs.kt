package com.example.dotory.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.dotory.data.model.Dot

@Composable
fun DeleteConfirmDialog(
    dot: Dot,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "도토리 삭제")
        },
        text = {
            Text(text = "'${dot.title}' 기록을 정말 삭제하시겠습니까?\n삭제한 데이터는 복구할 수 없습니다.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = "삭제", color = Color(0xFFC62828))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "취소")
            }
        }
    )
}

@Composable
fun DotActionDialog(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "기록 관리")
        },
        text = {
            Text(text = "이 장소의 기록을 어떻게 처리할까요?")
        },
        confirmButton = {
            TextButton(
                onClick = onEditClick
            ) {
                Text(text = "편집하기", color = Color(0xFF503A34))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDeleteClick
            ) {
                Text(text = "삭제하기", color = Color(0xFFC62828))
            }
        }
    )
}
