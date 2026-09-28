package com.gptplus18.app.data.models

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════
// 💳 Billing Models — نظام الاشتراكات الجديد
// ═══════════════════════════════════════════════════════════

data class BillingPlan(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("images") val images: Int,
    @SerializedName("videos") val videos: Int,
    @SerializedName("songs") val songs: Int,
    @SerializedName("code") val code: Int,
    @SerializedName("unlimited_code") val unlimitedCode: Boolean = false,
    @SerializedName("chat_daily") val chatDaily: Int = 200,
    @SerializedName("chat_monthly") val chatMonthly: Int = 4000,
    @SerializedName("is_free") val isFree: Boolean = false,
)

data class BillingPlansResponse(
    @SerializedName("plans") val plans: List<BillingPlan>,
)

data class BillingUsageItem(
    @SerializedName("used") val used: Int,
    @SerializedName("limit") val limit: Int,
)

data class BillingUsage(
    @SerializedName("images") val images: BillingUsageItem,
    @SerializedName("videos") val videos: BillingUsageItem,
    @SerializedName("songs") val songs: BillingUsageItem,
    @SerializedName("code") val code: BillingUsageItem,
    @SerializedName("chat") val chat: BillingUsageItem? = null,
)

data class BillingDailyTokens(
    @SerializedName("used") val used: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("remaining") val remaining: Int,
)

data class BillingPlanInfo(
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("is_paid") val isPaid: Boolean,
    @SerializedName("expires_at") val expiresAt: Double? = null,
)

data class BillingMeResponse(
    @SerializedName("uid") val uid: Int,
    @SerializedName("is_owner") val isOwner: Boolean = false,
    @SerializedName("plan") val plan: BillingPlanInfo,
    @SerializedName("usage") val usage: BillingUsage,
    @SerializedName("daily_tokens") val dailyTokens: BillingDailyTokens,
    @SerializedName("period") val period: String,
)

data class BillingSubscribeRequest(
    @SerializedName("plan_key") val planKey: String,
    @SerializedName("tx_hash") val txHash: String? = null,
)

data class BillingSubscribeResponse(
    @SerializedName("status") val status: String,
    @SerializedName("tx_id") val txId: Int? = null,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("amount_usd") val amountUsd: Double? = null,
    @SerializedName("wallet") val wallet: String? = null,
    @SerializedName("message") val message: String? = null,
)

data class BillingVerifyRequest(
    @SerializedName("plan_key") val planKey: String,
    @SerializedName("network") val network: String,
    @SerializedName("tx_hash") val txHash: String,
)

data class BillingVerifyResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("error") val error: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("expires_at") val expiresAt: Double? = null,
)

data class BillingTransaction(
    @SerializedName("id") val id: Int,
    @SerializedName("plan_key") val planKey: String?,
    @SerializedName("amount_usd") val amountUsd: Double,
    @SerializedName("currency") val currency: String = "USDT",
    @SerializedName("method") val method: String,
    @SerializedName("tx_hash") val txHash: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: Double,
    @SerializedName("confirmed_at") val confirmedAt: Double? = null,
)

data class BillingTransactionsResponse(
    @SerializedName("transactions") val transactions: List<BillingTransaction>,
)

data class BillingTopUpRequest(
    @SerializedName("kind") val kind: String,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("tx_hash") val txHash: String? = null,
)

data class BillingTopUpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("tx_id") val txId: Int? = null,
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("amount_usd") val amountUsd: Double? = null,
    @SerializedName("wallet") val wallet: String? = null,
)
