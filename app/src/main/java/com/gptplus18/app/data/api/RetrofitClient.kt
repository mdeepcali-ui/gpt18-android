package com.gptplus18.app.data.api

import com.gptplus18.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BASIC
        else
            HttpLoggingInterceptor.Level.NONE
    }

    // ═══ Retry Interceptor ═══
    // ⚠️ مهم: لا نعيد المحاولة لطلبات رفع الملفات (MultipartBody)
    // لأن الـ body يُستهلك مرة واحدة فقط، وإعادة المحاولة تفشل فوراً.
    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()

        // تخطي retry لطلبات الرفع (multipart)
        val isUpload = request.body is MultipartBody
        if (isUpload) {
            return@Interceptor chain.proceed(request)
        }

        var response: Response? = null
        var lastError: IOException? = null

        for (attempt in 1..3) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful || response.code < 500) {
                    return@Interceptor response
                }
                response.close()
            } catch (e: IOException) {
                lastError = e
            }

            if (attempt < 3) {
                Thread.sleep(500L * attempt)
            }
        }

        response ?: throw (lastError ?: IOException("فشل الاتصال"))
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(retryInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)       // كان 120 — رفعناه لدعم الردود الطويلة
        .writeTimeout(300, TimeUnit.SECONDS)      // كان 60 — رفعناه لدعم رفع الصور على 4G
        .callTimeout(600, TimeUnit.SECONDS)       // جديد — 10 دقائق إجمالية لكل طلب
        .retryOnConnectionFailure(true)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
