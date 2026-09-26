package com.gptplus18.app.ui.components

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgSecondary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary

// ═══════════════════════════════════════════════════════
// 🎵 Audio Player — Waveform + Play Button + Progress
// ═══════════════════════════════════════════════════════
@Composable
fun AudioPlayerCard(
    audioUrl: String,
    title: String = "أغنية",
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isReady by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)))
            prepare()
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        duration = this@apply.duration.coerceAtLeast(0L)
                        isReady = true
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        val ticker = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    position = exoPlayer.currentPosition
                    Thread.sleep(400)
                } catch (_: Exception) {}
            }
        }
        ticker.start()
        onDispose { ticker.interrupt(); exoPlayer.release() }
    }

    val progress by animateFloatAsState(
        targetValue = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
        animationSpec = tween(300, easing = LinearEasing),
        label = "audio_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1F1F26), Color(0xFF141419))
                )
            )
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // زر التشغيل الدائري
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Accent, Accent.copy(alpha = 0.7f))
                        )
                    )
                    .clickable(enabled = isReady) {
                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.take(35),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isPlaying) "يعزف الآن" else if (isReady) "جاهز للتشغيل" else "جاري التحضير...",
                    color = TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Waveform visualization
        AudioWaveform(
            isPlaying = isPlaying,
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(36.dp),
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(_fmtMillis(position), color = TextSecondary, fontSize = 10.sp)
            Text(_fmtMillis(duration), color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AudioWaveform(
    isPlaying: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    // ⭐ v2.0: نلتقط الألوان قبل Canvas (لأن drawScope ليس composable)
    val accentColor = Accent
    val inactiveColor = TextSecondary.copy(alpha = 0.3f)

    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Canvas(modifier = modifier) {
        val barCount = 42
        val barWidth = size.width / (barCount * 1.6f)
        val gap = barWidth * 0.6f
        val centerY = size.height / 2f
        val progressX = size.width * progress.coerceIn(0f, 1f)

        for (i in 0 until barCount) {
            val x = i * (barWidth + gap)
            val played = x <= progressX

            // ارتفاع عمود متذبذب
            val wave = if (isPlaying) {
                (kotlin.math.sin(phase + i * 0.35f).toFloat() + 1f) / 2f
            } else {
                0.5f + (i % 7) * 0.06f
            }
            val height = size.height * (0.25f + wave * 0.75f)
            val top = centerY - height / 2f

            val color = if (played) accentColor else inactiveColor

            drawRoundRect(
                color = color,
                topLeft = Offset(x, top),
                size = Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 🎬 Video Player — Portrait 9:16 + Overlay Play
// ═══════════════════════════════════════════════════════
@Composable
fun VideoPlayerCard(
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                    if (playing) hasStarted = true
                }
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)              // ⭐ طولي
            .shadow(10.dp, RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black)
            .clickable {
                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
            },
    ) {
        AndroidView(
            factory = { c ->
                PlayerView(c).apply {
                    player = exoPlayer
                    useController = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // غطاء قبل التشغيل — زر التشغيل الكبير
        if (!hasStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.75f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "pulse_scale",
                )
                Box(
                    modifier = Modifier
                        .size((72 * pulse).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Accent.copy(alpha = 0.95f), Accent.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp),
                    )
                }
            }
        }

        // مؤشر تحميل (Buffering)
        if (isBuffering && hasStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = Accent,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                )
            }
        }

        // شريط سفلي ناعم + أيقونة
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                ),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🎬", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) "يعرض الآن" else "فيديو",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// ✨ Skeleton Loader — يُعرض أثناء التوليد
// ═══════════════════════════════════════════════════════
@Composable
fun MediaSkeletonLoader(
    type: String,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "skel")
    val shimmer by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer",
    )

    val (emoji, label) = when (type) {
        "video" -> "🎬" to "جاري إنشاء الفيديو..."
        "song" -> "🎵" to "جاري تلحين الأغنية..."
        "image" -> "🎨" to "جاري إنشاء الصورة..."
        else -> "⏳" to "جاري التحميل..."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (type == "video") Modifier.aspectRatio(9f / 16f)
                else Modifier.height(80.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(BgSecondary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                        startX = shimmer * 600f,
                        endX = shimmer * 600f + 300f,
                    )
                ),
        )

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                color = TextPrimary.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// Helper
private fun _fmtMillis(ms: Long): String {
    if (ms <= 0) return "00:00"
    val s = ms / 1000
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}
