package com.gptplus18.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextTertiary

@Composable
fun ThinkingProcess(
    status: String,
    steps: List<String>,
    rawText: String? = null,
    modifier: Modifier = Modifier,
) {
    // إذا ما في لا steps ولا rawText — ما نعرض شي
    if (steps.isEmpty() && rawText.isNullOrBlank()) return

    var expanded by remember { mutableStateOf(true) }

    val label = when (status) {
        "think" -> "يفكر"
        "analyze" -> "يحلل"
        "create" -> "ينشئ"
        "write" -> "يكتب"
        else -> "يفكر"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .background(BgSecondary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$label...",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "طي",
                tint = TextTertiary,
                modifier = Modifier.size(14.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
        ) {
            Column(Modifier.padding(top = 4.dp)) {
                if (!rawText.isNullOrBlank()) {
                    // التفكير الحقيقي من السيرفر
                    Text(
                        text = rawText.trim(),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                    )
                } else {
                    // الخطوات الثابتة (placeholder)
                    steps.forEach { step ->
                        Text(
                            text = "• $step",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 1.dp),
                        )
                    }
                }
            }
        }
    }
}
