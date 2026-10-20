package com.gptplus18.app.ui.screens.code

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CodeThinkingBox(
    state: CodeUiState,
    modifier: Modifier = Modifier,
) {
    if (!state.isRunning) return
    val think = state.thinkingText
    val reply = state.messages.lastOrNull { it.id == -2 }?.content?.trim().orEmpty()
    if (think.isBlank() && reply.isBlank() && state.statusLabel.isBlank()) return

    Column(
        modifier = modifier.fillMaxWidth()
            .background(Color(0xFF0E0E12), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(
            "✦ التفكير",
            color = Color(0xFF6EE7B7),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
        )
        if (think.isNotBlank()) {
            val sc = rememberScrollState()
            LaunchedEffect(think.length) {
                sc.animateScrollTo(sc.maxValue)
            }
            Text(
                text = think,
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .verticalScroll(sc)
                    .padding(top = 6.dp),
                color = Color(0xFFB8B8C0),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
            )
        }
        if (reply.isNotBlank()) {
            Text(
                text = reply,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                color = Color(0xFFE8E8EE),
                fontSize = 13.sp,
            )
        }
        if (think.isBlank() && reply.isBlank() && state.statusLabel.isNotBlank()) {
            Text(state.statusLabel, color = Color(0xFF9CA3AF), fontSize = 12.sp)
        }
    }
}
