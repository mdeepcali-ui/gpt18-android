package com.gptplus18.app.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ═══════════════════════════════════════════════════════════
// 🎨 زر RUN — بيضوي أسود، إطار أزرق، نص RUN
// ═══════════════════════════════════════════════════════════
@Composable
fun RunButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val accentBlue = Color(0xFF3B82F6)
    val bgBlack = Color(0xFF000000)
    // ⭐ v19.0: Press Scale
    val _sInteraction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val _sPressed by _sInteraction.collectIsPressedAsState()
    val _sScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (_sPressed && enabled) 0.92f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessHigh,
        ),
        label = "run_btn_scale",
    )

    Box(
        modifier = modifier
            .height(26.dp)
            .graphicsLayer { scaleX = _sScale; scaleY = _sScale }
            .clip(RoundedCornerShape(50))
            .background(bgBlack)
            .border(1.dp, accentBlue, RoundedCornerShape(50))
            .clickable(
                interactionSource = _sInteraction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "RUN",
            color = accentBlue,
            fontSize = 11.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

// ═══════════════════════════════════════════════════════════
// 🪟 Run Dialog — نافذة موحدة مع أدوات تحكم
// ═══════════════════════════════════════════════════════════
@Composable
fun RunDialog(
    onDismiss: () -> Unit,
    onExecute: (code: String, lang: String) -> Unit,
    isRunning: Boolean = false,
    lastError: String = "",
) {
    val accentBlue = Color(0xFF3B82F6)
    val bgBlack = Color(0xFF000000)
    val bgDark = Color(0xFF0F0F0F)
    val bgMid = Color(0xFF1A1A1A)
    val textPrimary = Color(0xFFEDEDED)
    val textDim = Color(0xFF8A8A8A)

    // TextFieldValue للتحكم بالمؤشر
    var codeValue by remember { mutableStateOf(TextFieldValue("")) }
    var lang by remember { mutableStateOf("python") }
    var showLangMenu by remember { mutableStateOf(false) }
    // History — آخر أوامر نُفذت
    var history by remember { mutableStateOf(listOf<String>()) }
    var histIdx by remember { mutableIntStateOf(-1) }

    // إدراج نص في موضع المؤشر
    fun insertAtCursor(text: String) {
        val sel = codeValue.selection
        val before = codeValue.text.substring(0, sel.start)
        val after = codeValue.text.substring(sel.end)
        val newText = before + text + after
        codeValue = codeValue.copy(
            text = newText,
            selection = TextRange(sel.start + text.length),
        )
    }

    Dialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isRunning,
            dismissOnClickOutside = !isRunning,
        ),
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bgDark)
                .border(1.dp, accentBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ── Header: عنوان + Dropdown اللغة + زر إغلاق ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "▶  تنفيذ مباشر",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dropdown اللغة — بدل chips
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(bgMid)
                                    .border(1.dp, accentBlue.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .clickable(enabled = !isRunning) { showLangMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (lang == "python") "Python" else "Shell",
                                    color = accentBlue,
                                    fontSize = 11.sp,
            lineHeight = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = accentBlue,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = showLangMenu,
                                onDismissRequest = { showLangMenu = false },
                                containerColor = bgMid,
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Python", color = textPrimary, fontSize = 12.sp) },
                                    onClick = { lang = "python"; showLangMenu = false },
                                )
                                DropdownMenuItem(
                                    text = { Text("Shell", color = textPrimary, fontSize = 12.sp) },
                                    onClick = { lang = "shell"; showLangMenu = false },
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "✕",
                            color = textDim,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(enabled = !isRunning) { onDismiss() }
                                .padding(4.dp),
                        )
                    }
                }

                // ── حقل الكود ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgBlack)
                        .border(1.dp, accentBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    if (codeValue.text.isEmpty()) {
                        Text(
                            text = if (lang == "python") "print(\"hello world\")"
                                   else "echo hello world",
                            color = textDim,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        )
                    }
                    BasicTextField(
                        value = codeValue,
                        onValueChange = { codeValue = it },
                        enabled = !isRunning,
                        textStyle = TextStyle(
                            color = textPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        ),
                        cursorBrush = SolidColor(accentBlue),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    )
                }

                // ── شريط أدوات التحكم ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ToolKey("⇥", "Tab") {
                        insertAtCursor("    ")
                    }
                    ToolKey("↑", "Prev") {
                        if (history.isNotEmpty()) {
                            histIdx = if (histIdx < history.size - 1) histIdx + 1 else histIdx
                            codeValue = TextFieldValue(history[histIdx])
                        }
                    }
                    ToolKey("↓", "Next") {
                        if (history.isNotEmpty() && histIdx > 0) {
                            histIdx -= 1
                            codeValue = TextFieldValue(history[histIdx])
                        }
                    }
                    ToolKey("⏎", "Newline") {
                        insertAtCursor("\n")
                    }
                    ToolKey("⌫", "Clear") {
                        codeValue = TextFieldValue("")
                    }
                    ToolKey("Esc", "Close") {
                        if (!isRunning) onDismiss()
                    }
                }

                // ── خطأ ──
                if (lastError.isNotBlank() && !isRunning) {
                    Text(
                        text = "⚠ $lastError",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
            lineHeight = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }

                // ── أزرار التنفيذ ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Transparent)
                            .border(1.dp, textDim.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable(enabled = !isRunning) { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("إلغاء", color = textDim, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRunning) accentBlue.copy(alpha = 0.5f) else accentBlue)
                            .clickable(enabled = !isRunning && codeValue.text.isNotBlank()) {
                                // نحفظ في الـ history
                                history = (listOf(codeValue.text) + history).take(20)
                                histIdx = -1
                                onExecute(codeValue.text, lang)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                "▶  نفّذ",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── زر أداة صغير ──
@Composable
private fun ToolKey(
    symbol: String,
    label: String,
    onClick: () -> Unit,
) {
    val accentBlue = Color(0xFF3B82F6)
    val bgBlack = Color(0xFF000000)
    val textDim = Color(0xFF8A8A8A)

    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgBlack)
            .border(1.dp, textDim.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = symbol,
                color = accentBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                color = textDim,
                fontSize = 9.sp,
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════
// 💾 زر SAVE — حفظ المشروع على موبايل العميل
// ═══════════════════════════════════════════════════════════
@Composable
fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val accentBlue = Color(0xFF3B82F6)
    val bgBlack = Color(0xFF000000)
    // ⭐ v19.0: Press Scale
    val _svInteraction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val _svPressed by _svInteraction.collectIsPressedAsState()
    val _svScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (_svPressed && enabled) 0.92f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessHigh,
        ),
        label = "save_btn_scale",
    )

    Box(
        modifier = modifier
            .height(26.dp)
            .graphicsLayer { scaleX = _svScale; scaleY = _svScale }
            .clip(RoundedCornerShape(50))
            .background(bgBlack)
            .border(1.dp, accentBlue, RoundedCornerShape(50))
            .clickable(
                interactionSource = _svInteraction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = com.gptplus18.app.ui.components.icons.SaveIcon,
                contentDescription = null,
                tint = accentBlue,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "SAVE",
                color = accentBlue,
                fontSize = 11.sp,
            lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════
// 💾 SaveDialog — نافذة إدخال اسم المجلد
// ═══════════════════════════════════════════════════════════
@Composable
fun SaveDialog(
    onDismiss: () -> Unit,
    onSave: (folderName: String) -> Unit,
    isSaving: Boolean = false,
    lastError: String = "",
    defaultName: String = "",
) {
    val accentBlue = Color(0xFF3B82F6)
    val bgBlack = Color(0xFF000000)
    val bgDark = Color(0xFF0F0F0F)
    val textPrimary = Color(0xFFEDEDED)
    val textDim = Color(0xFF8A8A8A)

    var name by remember { mutableStateOf(defaultName.ifBlank { "my-project" }) }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isSaving,
            dismissOnClickOutside = !isSaving,
        ),
    ) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bgDark)
                .border(1.dp, accentBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("حفظ على موبايلي", color = textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "X",
                        color = textDim,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !isSaving) { onDismiss() }
                            .padding(4.dp),
                    )
                }

                Text(
                    "رح يُحفظ workspace كامل في Downloads/{اسم}/",
                    color = textDim,
                    fontSize = 11.sp,
            lineHeight = 11.sp,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgBlack)
                        .border(1.dp, accentBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= 64) name = it },
                        enabled = !isSaving,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                        ),
                        cursorBrush = SolidColor(accentBlue),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (lastError.isNotBlank() && !isSaving) {
                    Text(
                        text = "خطأ: " + lastError,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
            lineHeight = 11.sp,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f).height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, textDim.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable(enabled = !isSaving) { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) { Text("إلغاء", color = textDim, fontSize = 13.sp) }

                    Box(
                        modifier = Modifier
                            .weight(2f).height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSaving) accentBlue.copy(alpha = 0.5f) else accentBlue)
                            .clickable(enabled = !isSaving && name.isNotBlank()) { onSave(name.trim()) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("احفظ", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
