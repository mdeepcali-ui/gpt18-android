package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════
// Auth + User
// ═══════════════════════════════════════════════════════════

data class User(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("sub_expires") val subExpires: Double? = null,
    @SerializedName("trial_expires") val trialExpires: Double? = null,
    @SerializedName("trial_used") val trialUsed: Int? = null,
    @SerializedName("created_at") val createdAt: Double? = null,
) {
    val hasSubscription: Boolean
        get() = (subExpires ?: 0.0) > System.currentTimeMillis() / 1000.0
    val hasActiveTrial: Boolean
        get() = (trialExpires ?: 0.0) > System.currentTimeMillis() / 1000.0
}

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class SignupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: User,
)

data class MeResponse(
    @SerializedName("user") val user: User,
)


data class UpdateProfileRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
)

// ─── FCM Notifications ───
data class RegisterTokenRequest(
    @SerializedName("token") val token: String,
)
