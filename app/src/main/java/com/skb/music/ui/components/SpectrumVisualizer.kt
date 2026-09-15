package com.skb.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SpectrumVisualizer(
    bass: Float,
    mid: Float,
    treble: Float,
    activeColor: Color = Color(0xFF1DB954),
    modifier: Modifier = Modifier
) {
    val b by animateFloatAsState(targetValue = bass, animationSpec = tween(120), label = "b")
    val m by animateFloatAsState(targetValue = mid, animationSpec = tween(120), label = "m")
    val t by animateFloatAsState(targetValue = treble, animationSpec = tween(120), label = "t")

    val bars = listOf(b, m, t)

    Row(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEachIndexed { i, level ->
            val h = (0.08f + level.coerceIn(0f, 1f) * 0.92f) * 56f
            Box(
                Modifier
                    .width(18.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                activeColor,
                                activeColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
