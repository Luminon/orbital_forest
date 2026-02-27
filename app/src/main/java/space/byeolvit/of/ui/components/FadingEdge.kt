package space.byeolvit.of.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

val topFadeBrush = Brush.verticalGradient(
    0f to Color.Transparent,
    0.08f to Color.Black
)

val bottomFadeBrush = Brush.verticalGradient(
    0.92f to Color.Black,
    1f to Color.Transparent
)

fun Modifier.fadingEdges(
    showTop: Boolean,
    showBottom: Boolean
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        if (showTop) drawRect(brush = topFadeBrush, blendMode = BlendMode.DstIn)
        if (showBottom) drawRect(brush = bottomFadeBrush, blendMode = BlendMode.DstIn)
    }
