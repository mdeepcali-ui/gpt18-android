package com.gptplus18.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.gptplus18.app.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageTimestamp(ts: Double, modifier: Modifier = Modifier) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = Date((ts * 1000).toLong())
    Text(
        text = fmt.format(date),
        color = TextTertiary,
        fontSize = 10.sp,
        modifier = modifier,
    )
}
