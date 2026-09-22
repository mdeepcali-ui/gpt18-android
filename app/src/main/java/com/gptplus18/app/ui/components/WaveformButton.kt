package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq

/**
 * الدائرة الزرقاء مع waveform — زي ChatGPT
 */
@Composable
fun WaveformButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0A84FF),
    iconColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        // Waveform bars — 4 أعمدة بأطوال مختلفة
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WaveBar(height = 6.dp, color = iconColor)
            WaveBar(height = 12.dp, color = iconColor)
            WaveBar(height = 8.dp, color = iconColor)
            WaveBar(height = 14.dp, color = iconColor)
            WaveBar(height = 6.dp, color = iconColor)
        }
    }
}

@Composable
private fun WaveBar(height: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .width(2.5.dp)
            .height(height)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
            .background(color),
    )
}
