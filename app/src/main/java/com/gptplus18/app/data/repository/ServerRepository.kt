package com.gptplus18.app.data.repository

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.local.TokenStorage
import com.gptplus18.app.data.models.AddServerRequest
import com.gptplus18.app.data.models.ExecServerRequest
import com.gptplus18.app.data.models.ExecServerResponse
import com.gptplus18.app.data.models.ServerItem
import com.gptplus18.app.data.models.TestServerResponse
import com.gptplus18.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
) {
    private suspend fun bearer(): String? = tokenStorage.getToken()?.let { "Bearer $it" }

    suspend fun list(): Result<List<ServerItem>> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.listServers(b)
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!.servers)
            } else {
                Result.Error("HTTP ${r.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun add(
        label: String, host: String, port: Int, username: String,
        authType: String, secret: String,
    ): Result<Int> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.addServer(b, AddServerRequest(label, host, port, username, authType, secret))
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!.id)
            } else {
                val err = r.errorBody()?.string() ?: "HTTP ${r.code()}"
                Result.Error(err.take(200))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun delete(id: Int): Result<Unit> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.deleteServer(b, id)
            if (r.isSuccessful) Result.Success(Unit) else Result.Error("HTTP ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun test(id: Int): Result<TestServerResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.testServer(b, id)
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!)
            } else {
                Result.Error("HTTP ${r.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }

    suspend fun exec(id: Int, command: String): Result<ExecServerResponse> {
        val b = bearer() ?: return Result.Error("غير مصرح")
        return try {
            val r = api.execOnServer(b, id, ExecServerRequest(command))
            if (r.isSuccessful && r.body() != null) {
                Result.Success(r.body()!!)
            } else {
                Result.Error("HTTP ${r.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "خطأ")
        }
    }
}
