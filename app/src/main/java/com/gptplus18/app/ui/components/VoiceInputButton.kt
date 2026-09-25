package com.gptplus18.app.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.Accent
import java.util.Locale

@Composable
fun VoiceInputButton(
    enabled: Boolean,
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val isArabic = Locale.getDefault().language == "ar"

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val text = data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotBlank()) onResult(text)
        }
    }

    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .clickable(enabled = enabled) {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                        )
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE,
                            if (isArabic) "ar-SA" else "en-US",
                        )
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                            if (isArabic) "ar-SA" else "en-US",
                        )
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "\u0627\u062a\u0643\u0644\u0645...")
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }
                    launcher.launch(intent)
                } catch (_: Exception) {
                    // في حال ما في SpeechRecognizer على الجهاز
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Mic,
            contentDescription = "voice",
            tint = if (enabled) Color(0xFF9CA3AF) else Color(0xFF4B5563),
            modifier = Modifier.size(22.dp),
        )
    }
}
