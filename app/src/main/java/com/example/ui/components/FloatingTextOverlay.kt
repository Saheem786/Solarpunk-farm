package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SunGold

data class FloatingTextData(
    val id: Long = System.nanoTime(),
    val text: String,
    val color: Color = SolarEmerald
)

@Composable
fun FloatingTextOverlay(
    floatingTexts: List<FloatingTextData>,
    onComplete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        for (item in floatingTexts) {
            FloatingTextItemView(
                item = item,
                onFinished = { onComplete(item.id) }
            )
        }
    }
}

@Composable
private fun FloatingTextItemView(
    item: FloatingTextData,
    onFinished: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(item.id) {
        offsetY.animateTo(
            targetValue = -90f,
            animationSpec = tween(durationMillis = 1100, easing = LinearEasing)
        )
    }

    LaunchedEffect(item.id) {
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1100, easing = LinearEasing)
        )
        onFinished()
    }

    Text(
        text = item.text,
        color = item.color,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .offset { IntOffset(0, offsetY.value.toInt()) }
            .alpha(alpha.value)
            .padding(4.dp)
    )
}
