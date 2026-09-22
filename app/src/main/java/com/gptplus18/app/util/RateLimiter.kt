package com.gptplus18.app.util

/**
 * Rate Limiter بسيط — يمنع الطلبات السريعة جداً
 */
object RateLimiter {

    private val timestamps = mutableMapOf<String, MutableList<Long>>()

    /**
     * @param key مفتاح لتحديد نوع العملية (مثل: "chat_send", "code_generate")
     * @param maxAttempts عدد المحاولات المسموحة
     * @param windowMs النافذة الزمنية بالميلي ثانية
     * @return null إذا كان مسموحاً، أو نص الخطأ إذا مرفوض
     */
    fun check(key: String, maxAttempts: Int, windowMs: Long): String? {
        val now = System.currentTimeMillis()
        val list = timestamps.getOrPut(key) { mutableListOf() }

        // نحذف القديم
        list.removeAll { now - it > windowMs }

        if (list.size >= maxAttempts) {
            val oldest = list.minOrNull() ?: now
            val waitSec = ((windowMs - (now - oldest)) / 1000).coerceAtLeast(1)
            return "الرجاء الانتظار ${waitSec} ثانية قبل المحاولة مرة أخرى"
        }

        list.add(now)
        return null
    }

    /**
     * للـ Chat: 10 رسائل كل دقيقة
     */
    fun checkChat(): String? = check("chat_send", 10, 60_000)

    /**
     * للـ Code: 3 طلبات كل دقيقتين
     */
    fun checkCode(): String? = check("code_generate", 3, 120_000)

    /**
     * للـ Media: 5 طلبات كل دقيقتين
     */
    fun checkMedia(): String? = check("media_generate", 5, 120_000)
}
