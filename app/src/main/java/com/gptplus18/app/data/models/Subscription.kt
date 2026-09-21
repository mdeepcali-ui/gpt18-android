package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

data class SubscriptionPublic(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("plans") val plans: Map<String, Plan> = emptyMap(),
    @SerializedName("wallets") val wallets: Map<String, String> = emptyMap(),
    @SerializedName("trial_seconds") val trialSeconds: Int = 3600,
    @SerializedName("status") val status: UserStatus? = null,
)

data class Plan(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("days") val days: Int,
    @SerializedName("emoji") val emoji: String? = null,
)

data class UserStatus(
    @SerializedName("has_sub") val hasSub: Boolean = false,
    @SerializedName("sub_expires") val subExpires: Double = 0.0,
    @SerializedName("has_trial") val hasTrial: Boolean = false,
    @SerializedName("trial_used") val trialUsed: Boolean = false,
    @SerializedName("trial_expires") val trialExpires: Double = 0.0,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("now") val now: Double = 0.0,
)

data class MySubscriptionResponse(
    @SerializedName("status") val status: UserStatus,
    @SerializedName("plans") val plans: Map<String, Plan> = emptyMap(),
    @SerializedName("wallets") val wallets: Map<String, String> = emptyMap(),
    @SerializedName("trial_seconds") val trialSeconds: Int = 3600,
)

data class VerifyPaymentRequest(
    @SerializedName("plan") val plan: String,
    @SerializedName("network") val network: String,
    @SerializedName("tx_hash") val txHash: String,
)

data class VerifyPaymentResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("expires_at") val expiresAt: Double? = null,
    @SerializedName("amount") val amount: Double? = null,
)
