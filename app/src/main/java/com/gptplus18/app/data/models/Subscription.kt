package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════════
// ⭐ v2.0: نظام Credits + الاشتراكات + الشحن
// ═══════════════════════════════════════════════════════════════

data class SubscriptionPublic(
    @SerializedName("ok") val ok: Boolean = true,
    @SerializedName("plans") val plans: Map<String, Plan> = emptyMap(),
    @SerializedName("topup") val topup: TopupInfo? = null,
    @SerializedName("actions_pricing") val actionsPricing: List<ActionPricing> = emptyList(),
    @SerializedName("wallets") val wallets: Map<String, String> = emptyMap(),
    @SerializedName("trial_seconds") val trialSeconds: Int = 3600,
    @SerializedName("status") val status: UserStatus? = null,
    @SerializedName("free_tier") val freeTier: FreeTier? = null,
    @SerializedName("subscription_limits") val subscriptionLimits: SubscriptionLimits? = null,
)

data class Plan(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("days") val days: Int,
    @SerializedName("emoji") val emoji: String? = null,
    @SerializedName("type") val type: String = "subscription",  // ⭐ subscription | topup
    @SerializedName("min_amount") val minAmount: Double? = null,  // للشحن فقط
)

data class TopupInfo(
    @SerializedName("min") val min: Double = 5.0,
    @SerializedName("credits_per_usd") val creditsPerUsd: Int = 10000,
    @SerializedName("quick_amounts") val quickAmounts: List<Int> = listOf(5, 10, 20, 50),
)

data class ActionPricing(
    @SerializedName("action") val action: String,
    @SerializedName("credits") val credits: Int,
    @SerializedName("usd") val usd: Double,
)

data class FreeTier(
    @SerializedName("daily_messages") val dailyMessages: Int = 10,
    @SerializedName("max_input_tokens") val maxInputTokens: Int = 500,
    @SerializedName("max_output_tokens") val maxOutputTokens: Int = 800,
)

data class SubscriptionLimits(
    @SerializedName("chat_daily") val chatDaily: Int = 200,
    @SerializedName("code_daily") val codeDaily: Int = 100,
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
    @SerializedName("topup") val topup: TopupInfo? = null,
    @SerializedName("wallets") val wallets: Map<String, String> = emptyMap(),
    @SerializedName("trial_seconds") val trialSeconds: Int = 3600,
    // ⭐ v2.0: معلومات الرصيد
    @SerializedName("credits") val credits: Int = 0,
    @SerializedName("usd_value") val usdValue: Double = 0.0,
    @SerializedName("subscription") val subscription: ActiveSubscription? = null,
    @SerializedName("daily_usage") val dailyUsage: DailyUsage? = null,
)

data class ActiveSubscription(
    @SerializedName("plan") val plan: String,
    @SerializedName("expires_at") val expiresAt: Double = 0.0,
    @SerializedName("days_left") val daysLeft: Int = 0,
)

data class DailyUsage(
    @SerializedName("chat") val chat: Int = 0,
    @SerializedName("chat_limit_free") val chatLimitFree: Int = 10,
    @SerializedName("chat_limit_sub") val chatLimitSub: Int = 200,
    @SerializedName("code") val code: Int = 0,
    @SerializedName("code_limit") val codeLimit: Int = 100,
)

data class VerifyPaymentRequest(
    @SerializedName("plan") val plan: String,
    @SerializedName("network") val network: String,
    @SerializedName("tx_hash") val txHash: String,
    @SerializedName("topup_amount") val topupAmount: Double? = null,  // ⭐ للشحن
)

data class VerifyPaymentResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("type") val type: String? = null,  // ⭐ subscription | topup
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("expires_at") val expiresAt: Double? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("credits_added") val creditsAdded: Int? = null,  // ⭐ للشحن
    @SerializedName("new_balance") val newBalance: Int? = null,     // ⭐ للشحن
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("plan_name") val planName: String? = null,
    @SerializedName("days") val days: Int? = null,
)

// ═══════════════════════════════════════════════════════════════
// ⭐ v2.0: Credits API Responses
// ═══════════════════════════════════════════════════════════════

data class CreditsBalanceResponse(
    @SerializedName("uid") val uid: Int = 0,
    @SerializedName("credits") val credits: Int = 0,
    @SerializedName("usd_value") val usdValue: Double = 0.0,
    @SerializedName("subscription") val subscription: ActiveSubscription? = null,
    @SerializedName("daily_usage") val dailyUsage: DailyUsage? = null,
)

data class CreditsTransactionsResponse(
    @SerializedName("uid") val uid: Int = 0,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("transactions") val transactions: List<CreditTransaction> = emptyList(),
)

data class CreditTransaction(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("ts") val ts: Double = 0.0,
    @SerializedName("type") val type: String = "",
    @SerializedName("credits_delta") val creditsDelta: Int = 0,
    @SerializedName("usd_amount") val usdAmount: Double = 0.0,
    @SerializedName("balance_after") val balanceAfter: Int = 0,
    @SerializedName("action") val action: String = "",
    @SerializedName("model") val model: String = "",
    @SerializedName("description") val description: String = "",
)
