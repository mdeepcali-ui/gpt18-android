package com.gptplus18.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 🔬 LatexView — يرسم معادلات LaTeX باستخدام KaTeX (نفس مكتبة ChatGPT/Claude)
 * يدعم كل أنواع LaTeX: كسور، تكاملات، مصفوفات، رموز، إلخ.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LatexView(
    latex: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: Int = 18,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var ready by remember { mutableStateOf(false) }
    var measuredHeight by remember { mutableIntStateOf(60) }

    val colorHex = remember(textColor) {
        val r = (textColor.red * 255).toInt()
        val g = (textColor.green * 255).toInt()
        val b = (textColor.blue * 255).toInt()
        String.format("#%02X%02X%02X", r, g, b)
    }

    // رمز المعادلة كـ JS safe
    val escaped = remember(latex) {
        latex
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F0F12))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(measuredHeight.dp),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        textZoom = 100
                    }
                    isScrollContainer = false
                    overScrollMode = WebView.OVER_SCROLL_NEVER
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(
                                "renderFormula(\"$escaped\", \"$colorHex\", $fontSize);",
                                null,
                            )
                            // نقيس الطول بعد الرسم
                            view?.postDelayed({
                                view?.evaluateJavascript(
                                    "(function(){return document.getElementById('content').offsetHeight;})();"
                                ) { value ->
                                    val h = value?.replace("\"", "")?.toIntOrNull() ?: 60
                                    measuredHeight = h.coerceIn(50, 500)
                                }
                            }, 120)
                        }
                    }
                    loadUrl("file:///android_asset/katex/katex.html")
                    webView = this
                }
            },
            update = { view ->
                if (ready) {
                    view.evaluateJavascript(
                        "renderFormula(\"$escaped\", \"$colorHex\", $fontSize);",
                        null,
                    )
                }
            },
        )

        LaunchedEffect(webView) {
            // نعتبر الجاهزية بعد 400ms من إنشاء الـ WebView
            kotlinx.coroutines.delay(400)
            ready = true
        }
    }
}
