package com.gptplus18.app.data.repository

import com.gptplus18.app.util.DeviceIdProvider
import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.LoginRequest
import com.gptplus18.app.data.models.SignupRequest
import com.gptplus18.app.data.models.User
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    private val deviceIdProvider: DeviceIdProvider,
) {
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val r = api.login(LoginRequest(email.trim().lowercase(), password, deviceIdProvider.get()))
            if (r.isSuccessful) {
                val body = r.body()!!
                tokenStorage.save(body.token, body.user.name, body.user.email, body.user.id)
                Result.Success(body.user)
            } else {
                Result.Error(parseError(r.errorBody()?.string(), r.code()))
            }
        } catch (e: Exception) {
            Result.Error("تعذر الاتصال: ${e.message ?: "شبكة"}")
        }
    }

    suspend fun signup(name: String, email: String, password: String): Result<User> {
        return try {
            val r = api.signup(SignupRequest(name.trim(), email.trim().lowercase(), password, deviceIdProvider.get()))
            if (r.isSuccessful) {
                val body = r.body()!!
                tokenStorage.save(body.token, body.user.name, body.user.email, body.user.id)
                Result.Success(body.user)
            } else {
                Result.Error(parseError(r.errorBody()?.string(), r.code()))
            }
        } catch (e: Exception) {
            Result.Error("تعذر الاتصال: ${e.message ?: "شبكة"}")
        }
    }

    suspend fun me(): Result<User> {
        return try {
            val token = tokenStorage.getToken() ?: return Result.Error("غير مصرح")
            val r = api.me("Bearer $token")
            if (r.isSuccessful) Result.Success(r.body()!!.user)
            else Result.Error("جلسة منتهية")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun logout() = tokenStorage.clear()

    suspend fun hasToken(): Boolean = !tokenStorage.getToken().isNullOrBlank()

    private fun parseError(body: String?, code: Int): String {
        if (code == 429) return "محاولات كثيرة — جرب بعد 5 دقائق"
        if (code == 401) return "الإيميل أو كلمة السر غلط"
        if (code == 409) return "الإيميل مسجّل مسبقاً"
        if (body.isNullOrBlank()) return "خطأ ($code)"
        return try {
            Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
                ?: "خطأ ($code)"
        } catch (_: Exception) {
            "خطأ ($code)"
        }
    }
}
