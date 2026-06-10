package com.example.dotory.ui.map.components

import android.graphics.Point
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import coil.compose.AsyncImage
import com.example.dotory.data.model.Dot
import com.example.dotory.data.model.DotCategory

class BubbleShape(
    private val cornerRadius: Dp = 16.dp,
    private val arrowWidth: Dp = 16.dp,
    private val arrowHeight: Dp = 12.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radiusPx = with(density) { cornerRadius.toPx() }
        val arrowWidthPx = with(density) { arrowWidth.toPx() }
        val arrowHeightPx = with(density) { arrowHeight.toPx() }

        val path = Path().apply {
            val bodyHeight = size.height - arrowHeightPx
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, size.width, bodyHeight),
                    topLeft = CornerRadius(radiusPx),
                    topRight = CornerRadius(radiusPx),
                    bottomRight = CornerRadius(radiusPx),
                    bottomLeft = CornerRadius(radiusPx)
                )
            )
            val centerX = size.width / 2f
            moveTo(centerX - (arrowWidthPx / 2f), bodyHeight)
            lineTo(centerX, size.height)
            lineTo(centerX + (arrowWidthPx / 2f), bodyHeight)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * 마커의 픽셀 좌표를 기반으로 팝업 카드가 항상 마커 머리 위에 조준되도록 오프셋을 동적으로 계산합니다.
 */
class MarkerPopupPositionProvider(
    private val markerPoint: Point,
    private val markerRadiusPx: Int,
    private val arrowHeightPx: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = markerPoint.x - (popupContentSize.width / 2)
        // 꼬리 끝이 마커 머리(마커 최상단)에 정확하게 안착되도록 여백 10px 제거
        val y = markerPoint.y - markerRadiusPx - popupContentSize.height
        return IntOffset(x, y)
    }
}

@Composable
fun DotCardPopup(
    dot: Dot,
    screenPoint: Point,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    markerRadiusPx: Int = 17
) {
    Popup(
        popupPositionProvider = MarkerPopupPositionProvider(
            markerPoint = screenPoint,
            markerRadiusPx = markerRadiusPx,
            arrowHeightPx = 12
        ),
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = BubbleShape(cornerRadius = 16.dp, arrowWidth = 16.dp, arrowHeight = 12.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = modifier.width(260.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .padding(bottom = 12.dp)
            ) {
                // 1. 장소 사진 (4:3 비율)
                if (dot.photoUri.isNotEmpty()) {
                    AsyncImage(
                        model = dot.photoUri,
                        contentDescription = "장소 사진",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                    )
                } else {
                    // 사진 플레이스홀더
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = dot.category.emoji,
                            fontSize = 48.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. 타이틀 및 수정/삭제
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dot.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "수정",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. 코멘트 본문
                if (dot.comment.isNotEmpty()) {
                    Text(
                        text = dot.comment,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                } else {
                    Text(
                        text = "등록된 코멘트가 없습니다.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. 카테고리 뱃지 우측 하단 이동
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    CategoryBadge(category = dot.category)
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(
    category: DotCategory,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(category.colorHex)
        ),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = "${category.emoji} ${category.label}",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
