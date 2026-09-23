package com.gptplus18.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.TextPrimary
import androidx.compose.ui.res.stringResource
import com.gptplus18.app.R

// ═══════════════════════════════════════════
// المكونات الإضافية
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonsSheet(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
        ) {
            Text(
                stringResource(R.string.t_006),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            SubItem(Icons.Default.Code, "شرح كود", "اشرحلي كود معقد") {
                onSelect("اشرحلي هذا الكود بالتفصيل")
                onDismiss()
            }
            SubItem(Icons.Default.Translate, "ترجم", "ترجم نص للعربية") {
                onSelect("ترجم لي هذا النص إلى العربية")
                onDismiss()
            }
            SubItem(Icons.Default.Brush, "صمم لي", "تصميم إبداعي") {
                onSelect("صمم لي تصميماً احترافياً")
                onDismiss()
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════
// فكّر بعمق
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepThinkSheet(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
        ) {
            Text(
                stringResource(R.string.t_007),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            SubItem(Icons.Default.Psychology, "تحليل عميق", "حلّل بأكثر من زاوية") {
                onSelect("حلّل الموضوع بأكثر من زاوية وبعمق، واذكر الاحتمالات المختلفة")
                onDismiss()
            }
            SubItem(Icons.Default.Lightbulb, "أفكار إبداعية", "اقترح أفكار جديدة") {
                onSelect("اقترح 5 أفكار إبداعية مختلفة")
                onDismiss()
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SubItem(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Right)
            Text(description, color = Color(0xFF9A9AA0), fontSize = 12.sp,
                textAlign = TextAlign.Right)
        }
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .size(40.dp)
                .background(Color(0xFF3A3A3C), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
