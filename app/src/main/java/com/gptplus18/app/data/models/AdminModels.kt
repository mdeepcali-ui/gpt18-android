package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

data class AdminStatus(
    @SerializedName("is_owner") val isOwner: Boolean = false,
)

data class AdminUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("trial_used") val trialUsed: Int = 0,
    @SerializedName("trial_expires") val trialExpires: Double = 0.0,
    @SerializedName("sub_expires") val subExpires: Double = 0.0,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("created_at") val createdAt: Double = 0.0,
    @SerializedName("last_login") val lastLogin: Double? = null,
    @SerializedName("has_sub") val hasSub: Boolean = false,
    @SerializedName("has_trial") val hasTrial: Boolean = false,
    @SerializedName("is_owner") val isOwner: Boolean = false,
)

data class AdminUsersList(
    @SerializedName("items") val items: List<AdminUser> = emptyList(),
    @SerializedName("count") val count: Int = 0,
)

data class AdminGrantRequest(
    @SerializedName("email") val email: String,
    @SerializedName("plan") val plan: String = "month",
)

data class AdminRevokeRequest(
    @SerializedName("email") val email: String,
)

data class AdminResetTrialRequest(
    @SerializedName("email") val email: String,
)

data class AdminActionResult(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("message") val message: String? = null,
)
