package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.R
import com.gptplus18.app.data.models.ChatMode
import com.gptplus18.app.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelector(
    currentMode: ChatMode,
    isSubscribed: Boolean,
    onModeSelected: (ChatMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    // ⭐ لقطة ثابتة لألوان الوضع الحالي — تُمرّر للـ Popup
    val colors = LocalAppColors.current

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .background(colors.surface, RoundedCornerShape(20.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(currentMode.color, RoundedCornerShape(50)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = currentMode.label,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = stringResource(R.string.t_234),
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp),
            )
        }

        // ⭐ نمرر الألوان صراحةً للـ Popup (DropdownMenu يستخدم نافذة منفصلة)
        CompositionLocalProvider(LocalAppColors provides colors) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.surface,
                modifier = Modifier.width(260.dp),
            ) {
                Text(
                    stringResource(R.string.t_005),
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                ChatMode.values().forEach { mode ->
                    val locked = false  // ⭐ v2.0: الباكند يقرر (is_owner/can_image) — لا فحص محلي
                    DropdownMenuItem(
                        modifier = Modifier.background(colors.surface),
                        colors = MenuDefaults.itemColors(
                            textColor = colors.textPrimary,
                            leadingIconColor = colors.textPrimary,
                            trailingIconColor = colors.textSecondary,
                        ),
                        text = {
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(mode.emoji, fontSize = 16.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        mode.label,
                                        color = if (locked) colors.textTertiary else colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    if (locked) {
                                        Spacer(Modifier.width(6.dp))
                                        Text("🔒", fontSize = 12.sp)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    if (mode == currentMode) {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    mode.description,
                                    color = if (locked) colors.textTertiary else colors.textSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 26.dp),
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            if (!locked) onModeSelected(mode)
                        },
                    )
                }
            }
        }
    }
}
