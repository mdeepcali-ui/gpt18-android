package com.gptplus18.app

import ru.noties.jlatexmath.JLatexMath
import android.content.Context
import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GptPlus18App : Application(), ImageLoaderFactory {

    companion object {
        private const val TAG = "GptPlus18App"
    }

    override fun onCreate() {
        super.onCreate()
        // 🔬 تهيئة JLaTeXMath
        try {
            JLatexMath.init(this)
        } catch (_: Throwable) {}

        // ═══ Firebase Init ═══
        try {
            FirebaseApp.initializeApp(this)

            // Crashlytics — تفعيل تتبع الأخطاء
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            }

            // Analytics — تتبع استخدام التطبيق
            FirebaseAnalytics.getInstance(this).apply {
                setAnalyticsCollectionEnabled(true)
            }

            Log.d(TAG, "✅ Firebase جاهز (Crashlytics + Analytics)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل تهيئة Firebase: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // دعم GIF على Android P+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            // ❌ لا crossfade — يكسر تشغيل GIF
            .crossfade(false)
            // ⚠️ مهم للـ GIF: Bitmap عادي بدل Hardware
            .allowHardware(false)
            .build()
    }
}
