package com.example.skinscan.ui.bodymap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.skinscan.data.db.entity.PhotoEntity
import com.example.skinscan.data.model.BodyRegion
import com.example.skinscan.data.model.BodyView
import kotlin.math.hypot

/**
 * The "drawing of the body" used to guide the user to each exact spot. Filled
 * dots mark areas already photographed for the active session; tapping anywhere
 * opens the nearest region.
 */
@Composable
fun BodyDiagram(
    view: BodyView,
    photosByRegion: Map<String, PhotoEntity>,
    onRegionClick: (BodyRegion) -> Unit,
    modifier: Modifier = Modifier,
    onRegionLongClick: (BodyRegion) -> Unit = {},
) {
    val regions = remember(view) { BodyRegion.forView(view) }
    val silhouette = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.onSurfaceVariant
    val covered = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = modifier.pointerInput(view, photosByRegion) {
            detectTapGestures(
                onTap = { offset ->
                    nearestRegion(regions, offset.x, offset.y, size.width.toFloat(), size.height.toFloat())
                        ?.let(onRegionClick)
                },
                onLongPress = { offset ->
                    nearestRegion(regions, offset.x, offset.y, size.width.toFloat(), size.height.toFloat())
                        ?.let(onRegionLongClick)
                },
            )
        },
    ) {
        drawBody(view, outline, silhouette)

        val dotRadius = size.width * 0.028f
        regions.forEach { region ->
            val center = Offset(region.cx * size.width, region.cy * size.height)
            if (photosByRegion.containsKey(region.id)) {
                drawCircle(color = covered, radius = dotRadius, center = center)
                drawCircle(color = Color.White, radius = dotRadius * 0.4f, center = center)
            } else {
                drawCircle(
                    color = empty.copy(alpha = 0.7f),
                    radius = dotRadius * 0.7f,
                    center = center,
                    style = Stroke(width = size.width * 0.005f),
                )
            }
        }
    }
}

private fun nearestRegion(
    regions: List<BodyRegion>,
    tapX: Float,
    tapY: Float,
    width: Float,
    height: Float,
): BodyRegion? = regions.minByOrNull {
    hypot(it.cx * width - tapX, it.cy * height - tapY)
}

private fun DrawScope.drawBody(view: BodyView, outline: Color, fill: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.012f)

    val torso = Path().apply {
        moveTo(0.34f * w, 0.165f * h)
        lineTo(0.66f * w, 0.165f * h)
        lineTo(0.61f * w, 0.375f * h)
        lineTo(0.39f * w, 0.375f * h)
        close()
    }
    drawPath(torso, fill)
    drawPath(torso, outline, style = stroke)

    drawCircle(fill, radius = 0.072f * w, center = Offset(0.5f * w, 0.075f * h))
    drawCircle(outline, radius = 0.072f * w, center = Offset(0.5f * w, 0.075f * h), style = stroke)

    drawRect(fill, topLeft = Offset(0.465f * w, 0.115f * h), size = Size(0.07f * w, 0.06f * h))

    drawLine(fill, Offset(0.35f * w, 0.185f * h), Offset(0.25f * w, 0.40f * h), strokeWidth = 0.055f * w, cap = StrokeCap.Round)
    drawLine(fill, Offset(0.65f * w, 0.185f * h), Offset(0.75f * w, 0.40f * h), strokeWidth = 0.055f * w, cap = StrokeCap.Round)

    drawLine(fill, Offset(0.46f * w, 0.36f * h), Offset(0.44f * w, 0.72f * h), strokeWidth = 0.085f * w, cap = StrokeCap.Round)
    drawLine(fill, Offset(0.54f * w, 0.36f * h), Offset(0.56f * w, 0.72f * h), strokeWidth = 0.085f * w, cap = StrokeCap.Round)

    if (view == BodyView.BACK) {
        drawLine(outline, Offset(0.5f * w, 0.18f * h), Offset(0.5f * w, 0.355f * h), strokeWidth = w * 0.006f)
    }
}
