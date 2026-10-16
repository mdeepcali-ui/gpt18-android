package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

data class ServerItem(
    @SerializedName("id") val id: Int,
    @SerializedName("label") val label: String,
    @SerializedName("host") val host: String,
    @SerializedName("port") val port: Int = 22,
    @SerializedName("username") val username: String,
    @SerializedName("auth_type") val authType: String,
    @SerializedName("created_at") val createdAt: Double = 0.0,
    @SerializedName("last_used") val lastUsed: Double = 0.0,
    @SerializedName("last_status") val lastStatus: String = "",
)

data class ServersListResponse(
    @SerializedName("servers") val servers: List<ServerItem> = emptyList(),
    @SerializedName("count") val count: Int = 0,
)

data class AddServerRequest(
    @SerializedName("label") val label: String,
    @SerializedName("host") val host: String,
    @SerializedName("port") val port: Int = 22,
    @SerializedName("username") val username: String,
    @SerializedName("auth_type") val authType: String = "password",
    @SerializedName("secret") val secret: String,
)

data class AddServerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("id") val id: Int = 0,
    @SerializedName("label") val label: String = "",
    @SerializedName("detail") val detail: String = "",
)

data class TestServerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("output") val output: String = "",
    @SerializedName("error") val error: String = "",
)

data class ExecServerRequest(
    @SerializedName("command") val command: String,
)

data class ExecServerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("output") val output: String = "",
    @SerializedName("error") val error: String = "",
    @SerializedName("exit_code") val exitCode: Int = -1,
    @SerializedName("duration_ms") val durationMs: Double = 0.0,
)
