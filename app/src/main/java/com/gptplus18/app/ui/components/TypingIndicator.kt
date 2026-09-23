package com.gptplus18.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

@Composable
fun TypingIndicator(
    type: String = "think",
    modifier: Modifier = Modifier,
) {
    val gifUrl: String = when (type) {
        "think" -> "https://gptplus18.com/static/typing.gif"
        "analyze" -> "https://gptplus18.com/static/typing-2.gif"
        "create" -> "https://gptplus18.com/static/typing-3.gif"
        "write" -> "https://gptplus18.com/static/typing-4.gif"
        else -> "https://gptplus18.com/static/typing.gif"
    }

    val ctx = LocalContext.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 🎬 GIF — حجم صغير
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(gifUrl)
                .crossfade(false)
                .allowHardware(false)
                .build(),
            contentDescription = stringResource(R.string.t_257),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(28.dp)
                .widthIn(min = 70.dp, max = 120.dp),
        )
    }
}
