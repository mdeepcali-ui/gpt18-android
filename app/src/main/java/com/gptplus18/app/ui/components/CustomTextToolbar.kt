package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * ✨ v5.1.8 — Custom Text Toolbar
 * Ferrari-style floating toolbar: نسخ • لصق • تحديد الكل • 🌐 المتصفح
 */
class CustomTextToolbar(
    private val onOpenBrowser: () -> Unit,
    private val canOpenBrowser: () -> Boolean,
) : TextToolbar {

    private var menuState by mutableStateOf<MenuState?>(null)

    override val status: TextToolbarStatus
        get() = if (menuState != null) TextToolbarStatus.Shown else TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        menuState = MenuState(rect, onCopyRequested, onPasteRequested, onSelectAllRequested)
    }

    override fun hide() { menuState = null }

    @Composable
    fun Render() {
        val s = menuState ?: return
        val density = LocalDensity.current
        val x = with(density) { s.rect.left.toInt() - 120 }
        val y = with(density) { s.rect.top.toInt() - 140 }

        Popup(
            offset = IntOffset(x, y),
            properties = PopupProperties(focusable = false),
        ) {
            Row(
                modifier = Modifier
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E1E2E))
                    .border(1.dp, Color(0xFF3A3A4E), RoundedCornerShape(14.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                s.onCopy?.let { ToolbarBtn(Icons.Default.ContentCopy, "نسخ", it) }
                s.onPaste?.let { ToolbarBtn(Icons.Default.ContentPaste, "لصق", it) }
                s.onSelectAll?.let { ToolbarBtn(Icons.Default.SelectAll, "تحديد الكل", it) }
                if (canOpenBrowser()) {
                    ToolbarBtn(Icons.Default.Language, "المتصفح") {
                        onOpenBrowser(); hide()
                    }
                }
            }
        }
    }

    private data class MenuState(
        val rect: Rect,
        val onCopy: (() -> Unit)?,
        val onPaste: (() -> Unit)?,
        val onSelectAll: (() -> Unit)?,
    )
}

@Composable
private fun ToolbarBtn(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFE0E0F0),
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(text = label, color = Color(0xFFE0E0F0), fontSize = 11.sp)
    }
}
