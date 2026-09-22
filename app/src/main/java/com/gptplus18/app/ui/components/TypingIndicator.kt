package com.gptplus18.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gptplus18.app.ui.theme.TextSecondary

@Composable
fun TypingIndicator(
    type: String = "think",
    modifier: Modifier = Modifier,
) {
    val label: String
    val gifUrl: String

    when (type) {
        "think" -> { label = "يفكر"; gifUrl = "https://gptplus18.com/static/typing.gif" }
        "analyze" -> { label = "يحلل"; gifUrl = "https://gptplus18.com/static/typing-2.gif" }
        "create" -> { label = "ينشئ"; gifUrl = "https://gptplus18.com/static/typing-3.gif" }
        "write" -> { label = "يكتب"; gifUrl = "https://gptplus18.com/static/typing-4.gif" }
        else -> { label = "يفكر"; gifUrl = "https://gptplus18.com/static/typing.gif" }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // الـ GIF — كبير وواضح
        AsyncImage(
            model = gifUrl,
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(40.dp)
                .widthIn(min = 100.dp, max = 180.dp),
        )

        Spacer(Modifier.width(8.dp))

        // النص الصغير — رمادي هادئ
        Text(
            text = "$label...",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}
