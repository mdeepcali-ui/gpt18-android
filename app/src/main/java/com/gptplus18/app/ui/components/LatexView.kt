package com.gptplus18.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula

/**
 * 🔬 LatexView — يرسم معادلة LaTeX إلى Bitmap باستخدام JLaTeXMath
 * يستخدم في MarkdownText لعرض $...$ و $$...$$
 */
@Composable
fun LatexView(
    latex: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: Float = 40f,
) {
    val bitmap = remember(latex, textColor) {
        try {
            val formula = TeXFormula(latex)
            val fg = android.graphics.Color.argb(
                (textColor.alpha * 255).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt(),
            )
            val box = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize)
            val w = box.iconWidth.coerceAtLeast(1)
            val h = box.iconHeight.coerceAtLeast(1)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(AndroidColor.TRANSPARENT)
            box.setForeground(fg)
            box.paintIcon(null, canvas, 0, 0)
            bmp
        } catch (e: Throwable) {
            null
        }
    }

    if (bitmap != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F0F12))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = latex,
                modifier = Modifier.heightIn(max = 300.dp),
            )
        }
    } else {
        // فشل الرسم → نعرض النص كما هو
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F0F12))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            androidx.compose.material3.Text(
                text = latex,
                color = textColor,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            )
        }
    }
}
