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

// ═══════════════════════════════════════════
// Notifications (FCM)
// ═══════════════════════════════════════════

data class AdminSendNotificationRequest(
    @SerializedName("uid") val uid: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("deep_link") val deepLink: String? = null,
)

data class AdminSendManyRequest(
    @SerializedName("uids") val uids: List<Int>,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
)

data class AdminBroadcastRequest(
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
)

data class AdminNotificationResult(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("users_count") val usersCount: Int = 0,
    @SerializedName("success_count") val successCount: Int = 0,
    @SerializedName("failure_count") val failureCount: Int = 0,
)

data class AdminNotificationStats(
    @SerializedName("tokens_count") val tokensCount: Int = 0,
    @SerializedName("users_count") val usersCount: Int = 0,
    @SerializedName("sent_count") val sentCount: Int = 0,
    @SerializedName("recent") val recent: List<AdminNotifLog> = emptyList(),
    @SerializedName("by_event") val byEvent: List<AdminNotifByEvent> = emptyList(),
)

data class AdminNotifLog(
    @SerializedName("uid") val uid: Int,
    @SerializedName("event") val event: String,
    @SerializedName("title") val title: String?,
    @SerializedName("sent_at") val sentAt: Double = 0.0,
)

data class AdminNotifByEvent(
    @SerializedName("event") val event: String,
    @SerializedName("c") val count: Int = 0,
)
