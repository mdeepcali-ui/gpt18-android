package com.gptplus18.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.LocalAppColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import kotlin.math.max
import kotlin.math.min
import com.gptplus18.app.ui.theme.ScheherazadeFontFamily

/**
 * صندوق الرد الجاري:
 * - عنوان "التفكير" خارج المربع (فوقه)
 * - مربع أسود بدون حدود مرئية (منطقة لون فقط)
 * - نص التفكير يتمرر من تحت لفوق (auto-scroll ناعم)
 * - 3 نقاط تتحرك بالتتابع عندما النص فاضي
 */
private data class StatusInfo(val icon: ImageVector, val label: String)

private fun parseStatus(status: String): StatusInfo {
    val s = status.trim()
    return when {
        s.contains("أغنية") || s.contains("🎵") -> StatusInfo(Icons.Default.MusicNote, "ينشئ الأغنية")
        s.contains("صورة") || s.contains("🖼") -> StatusInfo(Icons.Default.Image, "ينشئ الصورة")
        s.contains("معرفة") || s.contains("📚") -> StatusInfo(Icons.Default.MenuBook, "يبحث في المعرفة")
        s.contains("الإنترنت") || s.contains("الويب") || s.contains("🔍") -> StatusInfo(Icons.Default.Language, "يبحث في الويب")
        s.contains("يفكر") || s.contains("التفكير") -> StatusInfo(Icons.Default.Psychology, "التفكير")
        s.contains("أحلل") || s.contains("يحلل") || s.contains("تحليل") -> StatusInfo(Icons.Default.Psychology, "يحلل")
        s.contains("أصيغ") || s.contains("يصيغ") || s.contains("أكتب") || s.contains("يكتب") -> StatusInfo(Icons.Default.EditNote, "يصيغ")
        s.contains("يجهز") || s.contains("تجهيز") -> StatusInfo(Icons.Default.AutoAwesome, "يجهز")
        else -> StatusInfo(Icons.Default.Psychology, "التفكير")
    }
}


private fun Modifier.innerEdgeShadow(
    isLight: Boolean,
): Modifier = this.drawWithContent {
    drawContent()

    // أعلى + أسفل: تدرّج كبير يمتد نحو الوسط
    val topAlpha = if (isLight) 0.15f else 0.80f
    val botAlpha = if (isLight) 0.13f else 0.70f
    val vShadow = size.height * 0.38f

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = topAlpha), Color.Transparent),
            startY = 0f, endY = vShadow,
        ),
        size = androidx.compose.ui.geometry.Size(size.width, vShadow),
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = botAlpha)),
            startY = size.height - vShadow, endY = size.height,
        ),
        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - vShadow),
        size = androidx.compose.ui.geometry.Size(size.width, vShadow),
    )

    // يمين + يسار: تظليل صغير لا يمتد
    val sAlpha = if (isLight) 0.10f else 0.50f
    val hShadow = 14f

    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Black.copy(alpha = sAlpha), Color.Transparent),
            startX = 0f, endX = hShadow,
        ),
        size = androidx.compose.ui.geometry.Size(hShadow, size.height),
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = sAlpha)),
            startX = size.width - hShadow, endX = size.width,
        ),
        topLeft = androidx.compose.ui.geometry.Offset(size.width - hShadow, 0f),
        size = androidx.compose.ui.geometry.Size(hShadow, size.height),
    )
}

@Composable
fun ThinkingShimmer(
    text: String,
    status: String = "",
    modifier: Modifier = Modifier,
) {
    val statusInfo = parseStatus(status)
    val colors = LocalAppColors.current
    val isLight = colors.isLight

    val boxBg = if (isLight) Color(0xFFF0F1F3) else Color(0xFF0A0A0D)
    val titleColor = if (isLight) Color(0xFF6E6E73) else Color(0xFFA0A2A8)
    val baseText = if (isLight) Color(0xFF3A3A40) else Color(0xFFA8A8B0)
    val shineColor = if (isLight) Color(0xFF1A1A1C) else Color(0xFFFFFFFF)

    // ✨ shimmer — لمعة تمر من اليسار لليمين
    val transition = rememberInfiniteTransition(label = "shine")
    val shimmerX by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shine_x",
    )

    // ⭐ v5.0.3: shimmer ثاني لكلمة "التفكير" — غيمة سوداء تمر
    val titleTransition = rememberInfiniteTransition(label = "title_shine")
    val titleShimmerX by titleTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "title_shine_x",
    )

    val scrollState = rememberScrollState()

    // ⭐ v19.0: typewriter + persistent chase — انسياب احترافي
    val textState = androidx.compose.runtime.rememberUpdatedState(text)
    var revealed: Int by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    LaunchedEffect(text.isEmpty()) {
        if (text.isEmpty()) revealed = 0
    }

    LaunchedEffect(scrollState) {
        while (true) {
            androidx.compose.runtime.withFrameNanos { }
            val targetLen = textState.value.length
            if (revealed < targetLen) {
                val diff = targetLen - revealed
                val step = when {
                    diff > 200 -> 10
                    diff > 80  -> 5
                    diff > 20  -> 3
                    diff > 5   -> 2
                    else       -> 1
                }
                revealed = min(revealed + step, targetLen)
            } else if (revealed > targetLen) {
                revealed = targetLen
            }
            val targetScroll = scrollState.maxValue
            val current = scrollState.value
            if (targetScroll > current) {
                val jump = targetScroll - current
                val step = when {
                    jump > 200 -> 15
                    jump > 80  -> 8
                    jump > 20  -> 4
                    else       -> 1
                }
                scrollState.scrollTo(min(current + step, targetScroll))
            }
        }
    }

    val displayText = textState.value.take(revealed)
    val hasText = displayText.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
    ) {
        // ── عنوان "التفكير" مع غيمة سوداء تمر ──
        val _titleDarkColor = if (isLight) Color(0xFF1A1A1C) else Color(0xFF000000)
        val _titleShine = if (isLight) Color(0xFF000000) else Color(0xFFE8E8F0)
        val _titleGradient = Brush.horizontalGradient(
            colors = listOf(
                titleColor,
                titleColor,
                _titleShine,
                _titleDarkColor,
                titleColor,
            ),
            startX = titleShimmerX * 400f - 100f,
            endX = titleShimmerX * 400f + 250f,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, bottom = 5.dp),
        ) {
            Icon(
                imageVector = statusInfo.icon,
                contentDescription = null,
                tint = if (isLight) Color(0xFF6E6E73) else Color(0xFFA0A2A8),
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = statusInfo.label,
                style = TextStyle(
                    fontFamily = ScheherazadeFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    brush = _titleGradient,
                ),
            )
        }

        // ── المربع الأسود بدون حدود ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .innerEdgeShadow(isLight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            boxBg,
                            Color(0xFF000000).copy(alpha = if (isLight) 0.04f else 0.55f),
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (hasText) {
                // ⭐ v5.0.6: كشف أي كود (مكتمل أو غير مكتمل)
                val hasCode = displayText.contains("```") || displayText.contains("`")
                // ⭐ v5.0.6: نُكمل الـfence الناقص (stream في المنتصف)
                val safeText = run {
                    val fences = Regex("```").findAll(displayText).count()
                    if (fences % 2 == 1) "$displayText\n```" else displayText
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 90.dp)
                        .verticalScroll(scrollState),
                ) {
                    if (hasCode) {
                        // 🎨 فيه كود — MarkdownText يلوّنه
                        MarkdownText(
                            text = safeText,
                            textColor = baseText,
                            fontSize = 10,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        // ✨ نص عادي — shimmer
                        val gradient = Brush.horizontalGradient(
                            colors = listOf(
                                baseText, baseText, shineColor, baseText, baseText,
                            ),
                            startX = shimmerX * 800f - 200f,
                            endX = shimmerX * 800f + 200f,
                        )
                        Text(
                            text = displayText,
                            style = TextStyle(
                                fontFamily = ScheherazadeFontFamily,
                                fontSize = 9.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.2.sp,
                                brush = gradient,
                            ),
                        )
                    }
                }
            } else {
                // 3 نقاط تتحرك
                TypingDots(
                    color = baseText.copy(alpha = 0.85f),
                )
            }
        }
    }
}

/**
 * 3 نقاط تتحرك بالتتابع (تكبر/تشف ثم تصغر/تخفت).
 */
@Composable
private fun TypingDots(color: Color) {
    val transition = rememberInfiniteTransition(label = "dots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
    ) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 550,
                        delayMillis = i * 180,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_$i",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
