package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {
    suspend fun getPublic(): Result<SubscriptionPublic> {
        return try {
            val r = api.subscriptionPublic()
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل التحميل (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun getMine(): Result<MySubscriptionResponse> {
        val t = tokenStorage.getToken() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.mySubscription("Bearer $t")
            if (r.isSuccessful) Result.Success(r.body()!!)
            else Result.Error("فشل التحميل (${r.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun verifyPayment(plan: String, network: String, txHash: String): Result<VerifyPaymentResponse> {
        val t = tokenStorage.getToken() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.verifyPayment("Bearer $t", VerifyPaymentRequest(plan, network, txHash))
            if (r.isSuccessful) {
                val body = r.body()!!
                if (body.ok) Result.Success(body)
                else Result.Error(body.message ?: "فشل التحقق")
            } else {
                Result.Error("خطأ (${r.code()})")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ شبكة")
        }
    }

    suspend fun startTrial(): Result<Unit> {
        val t = tokenStorage.getToken() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.startTrial("Bearer $t")
            if (r.isSuccessful) Result.Success(Unit)
            else Result.Error("فشل بدء التجربة")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }
}
