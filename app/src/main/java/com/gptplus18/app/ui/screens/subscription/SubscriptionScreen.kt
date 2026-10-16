package com.gptplus18.app.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit = {},
    vm: SubscriptionViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current
    val isArabic = Locale.getDefault().language == "ar"

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isArabic) "الاشتراك والرصيد" else "Subscription & Balance",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isArabic) "رجوع" else "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = TextPrimary,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Accent) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            BalanceCard(state, isArabic)
            Spacer(Modifier.height(20.dp))
            Text(
                if (isArabic) "اختر الباقة المناسبة" else "Choose Your Plan",
                color = TextPrimary,
                fontSize = 13.6.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (isArabic) "دفعة واحدة، تحكم كامل باستهلاكك" else "One-time payment, full control",
                color = colors.textTertiary,
                fontSize = 9.2.sp,
            )
            Spacer(Modifier.height(12.dp))
            state.plans.forEach { (key, plan) ->
                if (plan.type == "topup") return@forEach
                PlanCard(
                    planKey = key,
                    plan = plan,
                    isArabic = isArabic,
                    colors = colors,
                    onSubscribe = { vm.selectPlan(key) },
                )
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(20.dp))
            TopupCard(
                state = state,
                isArabic = isArabic,
                colors = colors,
                onTopup = { amount -> vm.selectPlan("topup"); vm.setTopupAmount(amount.toDouble()) },
            )
            Spacer(Modifier.height(24.dp))
            Text(
                if (isArabic) "💡 كل المدفوعات عبر USDT آمنة ومشفرة" else "💡 All payments via USDT are secure",
                color = colors.textTertiary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }

        if (state.selectedPlan != null) {
            PaymentDialog(
                state = state,
                vm = vm,
                isArabic = isArabic,
                colors = colors,
                onDismiss = { vm.closePayment() },
            )
        }
    }
}

@Composable
private fun BalanceCard(state: SubscriptionState, isArabic: Boolean) {
    val colors = LocalAppColors.current
    val my = state.mySubscription

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Accent.copy(alpha = 0.18f), Accent.copy(alpha = 0.05f))
                )
            )
            .border(1.dp, Accent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (isArabic) "رصيدك الحالي" else "Your Balance", color = TextSecondary, fontSize = 9.6.sp)
                    Text(
                        "$${String.format(Locale.US, "%.2f", my?.usdValue ?: 0.0)}",
                        color = TextPrimary, fontSize = 19.2.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Divider(color = colors.surfaceVariant, thickness = 1.dp)
            Spacer(Modifier.height(14.dp))
            val du = my?.dailyUsage
            if (du != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem(Icons.Default.Forum, if (isArabic) "دردشة" else "Chat", "${du.chat} / ${if (my.subscription != null) du.chatLimitSub else du.chatLimitFree}")
                    StatItem(Icons.Default.Code, if (isArabic) "برمجة" else "Code", "${du.code} / ${du.codeLimit}")
                    StatItem(Icons.Default.WorkspacePremium, if (isArabic) "اشتراك" else "Plan", when (my.subscription?.plan) {
                        "chat" -> if (isArabic) "دردشة" else "Chat"
                        "code" -> if (isArabic) "برمجة" else "Code"
                        "full" -> if (isArabic) "كامل" else "Full"
                        else -> if (isArabic) "مجاني" else "Free"
                    })
                }
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSecondary, fontSize = 9.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = TextPrimary, fontSize = 9.2.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlanCard(
    planKey: String,
    plan: com.gptplus18.app.data.models.Plan,
    isArabic: Boolean,
    colors: AppColors,
    onSubscribe: () -> Unit,
) {
    val isFull = planKey == "full"
    val isCode = planKey == "code"
    val isChat = planKey == "chat"
    val accent = when {
        isFull -> Color(0xFFFFB800)
        isCode -> Color(0xFF4ADE80)
        isChat -> Accent
        else -> Accent
    }
    val features = when (planKey) {
        "chat" -> if (isArabic) listOf(
            "دردشة غير محدودة", "وضع 18+ كامل", "ذاكرة محادثة كاملة", "بدون حد يومي",
        ) else listOf("Unlimited chat", "Full 18+ mode", "Complete memory", "No daily limit")
        "code" -> if (isArabic) listOf(
            "برمجة غير محدودة", "بيئة معزولة (Sandbox)", "كتابة وتشغيل ملفات", "نماذج متقدمة",
        ) else listOf("Unlimited code", "Isolated sandbox", "File write & run", "Advanced models")
        "full" -> if (isArabic) listOf(
            "دردشة + برمجة معاً", "كل مميزات الباقتين", "توفير \$5 شهرياً", "القيمة الأفضل",
        ) else listOf("Chat + Code", "All features", "Save \$5/mo", "Best value")
        else -> emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(
                width = if (isFull) 2.dp else 1.dp,
                color = if (isFull) accent.copy(alpha = 0.6f) else colors.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable { onSubscribe() }
            .padding(16.dp),
    ) {
        if (isFull) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.15f))
                    .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    if (isArabic) "الأكثر قيمة" else "Best value",
                    color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                )
            }
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when {
                            isFull -> Icons.Default.WorkspacePremium
                            isCode -> Icons.Default.Code
                            else -> Icons.Default.Forum
                        },
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(plan.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (isArabic) "شهرياً · يُلغى في أي وقت" else "Monthly · Cancel anytime",
                        color = colors.textTertiary, fontSize = 9.sp,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${plan.price.toInt()}", color = accent, fontSize = 17.6.sp, fontWeight = FontWeight.Bold)
                    Text(if (isArabic) "/شهر" else "/mo", color = colors.textTertiary, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(feat, color = TextSecondary, fontSize = 9.6.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color(0xFF0F0F0F),
                ),
            ) {
                Text(if (isArabic) "اشترك الآن" else "Subscribe Now", fontWeight = FontWeight.Bold, fontSize = 10.4.sp)
            }
        }
    }
}

@Composable
private fun TopupCard(
    state: SubscriptionState,
    isArabic: Boolean,
    colors: AppColors,
    onTopup: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.surfaceVariant, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF4ADE80).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (isArabic) "شحن رصيد" else "Top-up Balance", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (isArabic) "دون تاريخ انتهاء — استخدمه وقت ما بدك" else "No expiry — use anytime",
                        color = colors.textTertiary, fontSize = 9.sp,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (isArabic) "اختر مبلغ الشحن (الحد الأدنى \$5)" else "Choose amount (min \$5)",
                color = TextSecondary, fontSize = 9.2.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 20, 50).forEach { amt ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Accent.copy(alpha = 0.08f))
                            .border(1.dp, Accent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .clickable { onTopup(amt) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$$amt", color = Accent, fontSize = 11.2.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentDialog(
    state: SubscriptionState,
    vm: SubscriptionViewModel,
    isArabic: Boolean,
    colors: AppColors,
    onDismiss: () -> Unit,
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // ⭐ v2.2: اختيار الشبكة
    var selectedNetwork by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("trc20") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.surfaceVariant, RoundedCornerShape(20.dp)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // ═══ Header ═══
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isArabic) "إتمام الدفع" else "Complete Payment",
                        color = TextPrimary,
                        fontSize = 12.8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    androidx.compose.material3.IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Divider(color = colors.surfaceVariant, thickness = 1.dp)

                // ═══ المحتوى (Scrollable) ═══
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    // ═══ 1. اختيار الشبكة ═══
                    Text(
                        if (isArabic) "اختر الشبكة:" else "Choose network:",
                        color = TextSecondary,
                        fontSize = 9.6.sp,
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        NetworkChip(
                            label = "TRC20",
                            subtitle = if (isArabic) "Tron" else "Tron",
                            selected = selectedNetwork == "trc20",
                            onClick = { selectedNetwork = "trc20" },
                            colors = colors,
                        )
                        NetworkChip(
                            label = "BEP20",
                            subtitle = if (isArabic) "BSC (رسوم أقل)" else "BSC (lower fees)",
                            selected = selectedNetwork == "bep20",
                            onClick = { selectedNetwork = "bep20" },
                            colors = colors,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ═══ 2. العنوان ═══
                    val wallet = state.wallets[selectedNetwork] ?: ""

                    Text(
                        if (isArabic) "حوّل المبلغ على:" else "Send to:",
                        color = TextSecondary,
                        fontSize = 9.6.sp,
                    )
                    Spacer(Modifier.height(8.dp))

                    // ═══ 3. محفظة + زر نسخ ═══
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgPrimary)
                            .border(1.dp, colors.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            wallet,
                            color = Accent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(6.dp))
                        androidx.compose.material3.IconButton(
                            onClick = {
                                val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("wallet", wallet))
                                android.widget.Toast.makeText(
                                    ctx,
                                    if (isArabic) "تم نسخ العنوان ✅" else "Address copied ✅",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Default.ContentCopy,
                                contentDescription = if (isArabic) "نسخ" else "Copy",
                                tint = Accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // ═══ 4. QR Code ═══
                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val qrBitmap = androidx.compose.runtime.remember(wallet, selectedNetwork) {
                                try {
                                    val uri = when (selectedNetwork) {
                                        "bep20" -> "ethereum:$wallet@56"
                                        "trc20" -> "tron:$wallet"
                                        else -> wallet
                                    }
                                    com.gptplus18.app.util.QrGenerator.generate(uri, size = 500)
                                } catch (_: Exception) { null }
                            }
                            if (qrBitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Wallet QR",
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        if (isArabic) "امسح الرمز للتحويل السريع" else "Scan for quick transfer",
                        color = colors.textTertiary,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // ═══ 5. TX Hash ═══
                    Spacer(Modifier.height(16.dp))

                    Text(
                        if (isArabic) "ثم الصق رقم العملية (TX Hash):" else "Then paste TX Hash:",
                        color = TextSecondary,
                        fontSize = 9.6.sp,
                    )
                    Spacer(Modifier.height(8.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = state.txHash,
                        onValueChange = { vm.setTxHash(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = ScheherazadeFontFamily, fontSize = 9.6.sp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = colors.surfaceVariant,
                            cursorColor = Accent,
                        ),
                    )

                    Spacer(Modifier.height(8.dp))
                }

                Divider(color = colors.surfaceVariant, thickness = 1.dp)

                // ═══ Actions (ثابتة أسفل) ═══
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary,
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceVariant),
                    ) {
                        Text(
                            if (isArabic) "إلغاء" else "Cancel",
                            fontSize = 10.4.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    androidx.compose.material3.Button(
                        onClick = { vm.verify() },
                        enabled = !state.isVerifying && state.txHash.length >= 20,
                        modifier = Modifier.weight(1.3f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White,
                            disabledContainerColor = colors.surfaceVariant,
                            disabledContentColor = colors.textTertiary,
                        ),
                    ) {
                        if (state.isVerifying) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                if (isArabic) "تحقق" else "Verify",
                                fontSize = 10.4.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun androidx.compose.foundation.layout.RowScope.NetworkChip(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
) {
    val blue = Color(0xFF3B82F6)
    val bg = if (selected) blue.copy(alpha = 0.12f) else colors.surface
    val border = if (selected) blue else colors.surfaceVariant
    val titleColor = if (selected) blue else TextPrimary

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = border,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Column {
            Text(
                label,
                color = titleColor,
                fontSize = 10.4.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = colors.textTertiary,
                fontSize = 9.sp,
            )
        }
    }
}
