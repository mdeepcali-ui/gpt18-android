package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.*
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {
    private suspend fun bearer(): String? {
        val t = tokenStorage.getToken() ?: return null
        return "Bearer $t"
    }

    suspend fun isOwner(): Boolean {
        val b = bearer() ?: return false
        return try {
            val r = api.adminStatus(b)
            r.isSuccessful && (r.body()?.isOwner == true)
        } catch (_: Exception) { false }
    }

    suspend fun listUsers(): Result<List<AdminUser>> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.adminUsers(b)
            if (r.isSuccessful) Result.Success(r.body()!!.items)
            else Result.Error("فشل التحميل")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun grant(email: String, plan: String): Result<String> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.adminGrant(b, AdminGrantRequest(email, plan))
            if (r.isSuccessful) Result.Success(r.body()?.message ?: "تم")
            else Result.Error("فشل المنح")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun revoke(email: String): Result<String> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.adminRevoke(b, AdminRevokeRequest(email))
            if (r.isSuccessful) Result.Success(r.body()?.message ?: "تم")
            else Result.Error("فشل الإلغاء")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun resetTrial(email: String): Result<String> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.adminResetTrial(b, AdminResetTrialRequest(email))
            if (r.isSuccessful) Result.Success(r.body()?.message ?: "تم")
            else Result.Error("فشل الضبط")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }
}
