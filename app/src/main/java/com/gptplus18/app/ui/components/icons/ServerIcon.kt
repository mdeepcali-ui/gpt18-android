package com.gptplus18.app.ui.components.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * ServerIcon — أيقونة سيرفر مخصصة.
 * تصميم: رفّان أنيقان + LED (نقطة) لكل رف.
 * Stroke رفيع (1.5) + linecap round للحواف الناعمة.
 */
val ServerIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ServerIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // ── الرفّ العلوي ──
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        ) {
            // مستطيل الرفّ العلوي
            moveTo(4f, 5f)
            lineTo(20f, 5f)
            lineTo(20f, 10f)
            lineTo(4f, 10f)
            close()
            // الخط الفاصل بين الرفين (كمؤشر)
        }
        // ── الرفّ السفلي ──
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        ) {
            moveTo(4f, 13f)
            lineTo(20f, 13f)
            lineTo(20f, 18f)
            lineTo(4f, 18f)
            close()
        }
        // ── LED العلوي (نقطة صغيرة يمين) ──
        path(
            fill = SolidColor(Color.White),
            stroke = null,
        ) {
            moveTo(17f, 7.5f)
            curveTo(17f, 7.5f, 17f, 7.5f, 17f, 7.5f)
            // نرسم دائرة صغيرة بقطر 1.4
            moveTo(17.7f, 7.5f)
            curveTo(17.7f, 7.887f, 17.387f, 8.2f, 17f, 8.2f)
            curveTo(16.613f, 8.2f, 16.3f, 7.887f, 16.3f, 7.5f)
            curveTo(16.3f, 7.113f, 16.613f, 6.8f, 17f, 6.8f)
            curveTo(17.387f, 6.8f, 17.7f, 7.113f, 17.7f, 7.5f)
            close()
        }
        // ── LED السفلي ──
        path(
            fill = SolidColor(Color.White),
            stroke = null,
        ) {
            moveTo(17.7f, 15.5f)
            curveTo(17.7f, 15.887f, 17.387f, 16.2f, 17f, 16.2f)
            curveTo(16.613f, 16.2f, 16.3f, 15.887f, 16.3f, 15.5f)
            curveTo(16.3f, 15.113f, 16.613f, 14.8f, 17f, 14.8f)
            curveTo(17.387f, 14.8f, 17.7f, 15.113f, 17.7f, 15.5f)
            close()
        }
        // ── خطوط البيانات (يسار) — ترمز للاتصال ──
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        ) {
            // خط صغير في الرف العلوي
            moveTo(7f, 7.5f)
            lineTo(11f, 7.5f)
            // خط صغير في الرف السفلي
            moveTo(7f, 15.5f)
            lineTo(11f, 15.5f)
        }
    }.build()
}

/**
 * FolderIcon — أيقونة مجلد مخصصة للمشاريع.
 */
val FolderIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FolderIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        ) {
            // شكل المجلد: مستطيل مع لسان أعلى
            moveTo(3.5f, 6.5f)
            lineTo(9f, 6.5f)
            lineTo(11f, 8.5f)
            lineTo(20.5f, 8.5f)
            lineTo(20.5f, 18f)
            lineTo(3.5f, 18f)
            close()
        }
        // خط أفقي داخلي (يمثل المستندات)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        ) {
            moveTo(7f, 12f)
            lineTo(17f, 12f)
            moveTo(7f, 15f)
            lineTo(14f, 15f)
        }
    }.build()
}


/**
 * SaveIcon — سهم تنزيل داخل صندوق (رمز الحفظ/التنزيل).
 */
val SaveIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SaveIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // صندوق مفتوح من الأعلى (U-shape)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        ) {
            // الجهة اليسرى نازلة
            moveTo(5f, 11f)
            lineTo(5f, 19f)
            lineTo(19f, 19f)
            lineTo(19f, 11f)
        }
        // سهم نازل (عمودي)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        ) {
            moveTo(12f, 4f)
            lineTo(12f, 14f)
        }
        // رأس السهم
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
        ) {
            moveTo(8f, 10.5f)
            lineTo(12f, 14.5f)
            lineTo(16f, 10.5f)
        }
    }.build()
}
