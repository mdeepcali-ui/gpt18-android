package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {
    private suspend fun bearer(): String? {
        val t = tokenStorage.getToken() ?: return null
        return "Bearer $t"
    }

    /** قائمة الباقات — بدون توكن */
    suspend fun getPlans(): Result<List<BillingPlan>> {
        return try {
            val r = api.billingPlans()
            if (r.isSuccessful) Result.Success(r.body()?.plans ?: emptyList())
            else Result.Error("فشل تحميل الباقات (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    /** حالة اشتراكي الحالية + الاستهلاك */
    suspend fun getMyBilling(): Result<BillingMeResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.billingMe(b)
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل تحميل الاشتراك (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    /** إنشاء طلب اشتراك جديد */
    suspend fun subscribe(planKey: String, txHash: String? = null): Result<BillingSubscribeResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.billingSubscribe(b, BillingSubscribeRequest(planKey, txHash))
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل الاشتراك (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    /** التحقق من الدفع (بعد إرسال USDT) */
    suspend fun verifyPayment(planKey: String, network: String, txHash: String): Result<BillingVerifyResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.billingVerifyPayment(b, BillingVerifyRequest(planKey, network, txHash))
            if (r.isSuccessful) {
                val body = r.body()!!
                if (body.ok) Result.Success(body)
                else Result.Error(body.error ?: "فشل التحقق")
            } else {
                Result.Error("خطأ (${r.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    /** سجل الدفعات */
    suspend fun getTransactions(): Result<List<BillingTransaction>> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.billingTransactions(b)
            if (r.isSuccessful) Result.Success(r.body()?.transactions ?: emptyList())
            else Result.Error("فشل تحميل السجل")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    /** Top-up (شراء إضافي) */
    suspend fun topUp(kind: String, quantity: Int = 1, txHash: String? = null): Result<BillingTopUpResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.billingTopUp(b, BillingTopUpRequest(kind, quantity, txHash))
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل الشراء (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }
}
