package com.gptplus18.app.ui.screens.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import com.gptplus18.app.data.models.BillingPlan
import com.gptplus18.app.data.models.BillingTransaction
import com.gptplus18.app.data.models.BillingMeResponse

private val BgMain = Color(0xFF0E0E10)
private val BgCard = Color(0xFF1C1C1E)
private val BgCardElevated = Color(0xFF252528)
private val BgInput = Color(0xFF2A2A2E)
private val TextMain = Color(0xFFF5F5F7)
private val TextSub = Color(0xFF8E8E93)
private val TextDim = Color(0xFF636366)
private val AccentBlue = Color(0xFF0A84FF)
private val AccentGold = Color(0xFFD4AF37)
private val DividerSoft = Color(0xFF2C2C2E)
private val SuccessGreen = Color(0xFF32D74B)
private val DangerRed = Color(0xFFFF453A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onBack: () -> Unit = {},
    vm: BillingViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scroll = rememberScrollState()

    Scaffold(
        containerColor = BgMain,
        topBar = {
            TopAppBar(
                title = {
                    Text("الاشتراك", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "رجوع", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgMain),
            )
        },
    ) { pad ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue, strokeWidth = 2.dp)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            state.me?.let { StatusSection(it) }
            Spacer(Modifier.height(28.dp))
            SectionTitle("اختر الباقة")
            Spacer(Modifier.height(14.dp))

            state.plans.filter { !it.isFree }.forEach { plan ->
                PlanCard(plan = plan, selected = state.selectedPlan == plan.key, onSelect = { vm.selectPlan(plan.key) })
                Spacer(Modifier.height(10.dp))
            }

            AnimatedVisibility(
                visible = state.selectedPlan != null,
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut(),
            ) {
                PaymentSection(
                    state = state,
                    clipboard = clipboard,
                    onNetwork = { vm.selectNetwork(it) },
                    onTxHashChange = { vm.setTxHash(it) },
                    onVerify = { vm.verifyPayment() },
                )
            }

            state.successMessage?.let { msg ->
                Spacer(Modifier.height(16.dp))
                MessageBanner(msg, false, ctx)
            }
            state.errorMessage?.let { msg ->
                Spacer(Modifier.height(16.dp))
                MessageBanner(msg, true, ctx)
            }

            if (state.transactions.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                SectionTitle("سجل الدفعات")
                Spacer(Modifier.height(12.dp))
                state.transactions.take(10).forEach { TransactionRow(it) }
            }
        }
    }
}

@Composable
private fun StatusSection(me: BillingMeResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(20.dp))
            .border(0.5.dp, DividerSoft, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (me.plan.isPaid) AccentGold.copy(alpha = 0.15f) else AccentBlue.copy(alpha = 0.15f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (me.plan.isPaid) Icons.Outlined.WorkspacePremium else Icons.Outlined.Person,
                    contentDescription = null,
                    tint = if (me.plan.isPaid) AccentGold else AccentBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (me.isOwner) "المالك" else me.plan.name,
                    color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                )
                if (me.plan.expiresAt != null && me.plan.expiresAt > 0) {
                    val days = ((me.plan.expiresAt - (System.currentTimeMillis() / 1000.0)) / 86400).toInt()
                    Text("متبقي $days يوم", color = TextSub, fontSize = 12.sp)
                } else if (me.isOwner) {
                    Text("صلاحيات كاملة", color = TextSub, fontSize = 12.sp)
                } else {
                    Text("الباقة المجانية", color = TextSub, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Divider(color = DividerSoft, thickness = 0.5.dp)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            UsageCell(Icons.Outlined.Image, "صور", me.usage.images.used, me.usage.images.limit)
            UsageCell(Icons.Outlined.Movie, "فيديو", me.usage.videos.used, me.usage.videos.limit)
            UsageCell(Icons.Outlined.MusicNote, "أغاني", me.usage.songs.used, me.usage.songs.limit)
            UsageCell(Icons.Outlined.Code, "كود", me.usage.code.used, me.usage.code.limit)
        }
        if (!me.plan.isPaid) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    "توكنات اليوم: ${me.dailyTokens.remaining} / ${me.dailyTokens.limit}",
                    color = TextDim, fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun UsageCell(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, used: Int, limit: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = TextSub, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(if (limit <= 0) "—" else "$used/$limit", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(label, color = TextDim, fontSize = 10.sp)
    }
}

@Composable
private fun PlanCard(plan: BillingPlan, selected: Boolean, onSelect: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "planScale",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(if (selected) BgCardElevated else BgCard, RoundedCornerShape(18.dp))
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) AccentBlue else DividerSoft,
                shape = RoundedCornerShape(18.dp),
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onSelect() },
                )
            }
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(plan.name, color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text("شهرياً", color = TextDim, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${plan.price.toInt()}", color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("دولار", color = TextDim, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Divider(color = DividerSoft, thickness = 0.5.dp)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FeatureMini(Icons.Outlined.Image, "${plan.images}")
            FeatureMini(Icons.Outlined.Movie, "${plan.videos}")
            FeatureMini(Icons.Outlined.MusicNote, "${plan.songs}")
            FeatureMini(Icons.Outlined.Code, if (plan.unlimitedCode) "∞" else "${plan.code}")
        }
        if (selected) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentBlue.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓ مختارة", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FeatureMini(icon: androidx.compose.ui.graphics.vector.ImageVector, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSub, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(count, color = TextSub, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PaymentSection(
    state: BillingUiState,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    onNetwork: (String) -> Unit,
    onTxHashChange: (String) -> Unit,
    onVerify: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .background(BgCard, RoundedCornerShape(18.dp))
            .border(0.5.dp, DividerSoft, RoundedCornerShape(18.dp))
            .padding(18.dp),
    ) {
        SectionTitle("إتمام الدفع")
        Spacer(Modifier.height(14.dp))

        Text("الشبكة", color = TextSub, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NetworkChip("BEP20", state.selectedNetwork == "bep20") { onNetwork("bep20") }
            NetworkChip("TRC20", state.selectedNetwork == "trc20") { onNetwork("trc20") }
        }
        Spacer(Modifier.height(16.dp))

        Text("أرسل USDT إلى:", color = TextSub, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        val walletAddr = if (state.selectedNetwork == "trc20") "TUoytveyRoedjVtRUuc5dNzeMh2kKrVxs8"
                         else "0xDb20493e64c5b3aaAa564C620DDb38d8be913eb6"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgInput, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(walletAddr, color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ContentCopy,
                contentDescription = "نسخ",
                tint = TextSub,
                modifier = Modifier.size(18.dp).clickable { clipboard.setText(AnnotatedString(walletAddr)) },
            )
        }

        // ⭐ QR Code للمحفظة
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val qrBmp = remember(walletAddr) {
                com.gptplus18.app.util.QrGenerator.generate(walletAddr, 500)
            }
            Image(
                bitmap = qrBmp.asImageBitmap(),
                contentDescription = "QR المحفظة",
                modifier = Modifier.size(180.dp),
            )
        }
        Text(
            "امسح الرمز بكاميرا المحفظة",
            color = TextDim,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(Modifier.height(16.dp))

        Text("رقم العملية (tx_hash)", color = TextSub, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgInput, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            if (state.txHash.isEmpty()) {
                Text("0x... أو T...", color = TextDim, fontSize = 13.sp)
            }
            BasicTextField(
                value = state.txHash,
                onValueChange = onTxHashChange,
                enabled = true,
                textStyle = TextStyle(color = TextMain, fontSize = 13.sp),
                cursorBrush = SolidColor(AccentBlue),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(20.dp))

        var pressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.97f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
            label = "verifyBtn",
        )
        val enabled = state.txHash.trim().length >= 20 && !state.isVerifying

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .background(if (enabled) AccentBlue else BgInput, RoundedCornerShape(14.dp))
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures(
                            onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                            onTap = { onVerify() },
                        )
                    }
                }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isVerifying) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    "تحقق وفعّل الاشتراك",
                    color = if (enabled) Color.White else TextDim,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun NetworkChip(label: String, selected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) AccentBlue.copy(alpha = 0.15f) else BgInput,
                RoundedCornerShape(10.dp),
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) AccentBlue else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            color = if (selected) AccentBlue else TextSub,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun MessageBanner(msg: String, isError: Boolean, ctx: android.content.Context) {
    LaunchedEffect(msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                (if (isError) DangerRed else SuccessGreen).copy(alpha = 0.12f),
                RoundedCornerShape(12.dp),
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (isError) DangerRed else SuccessGreen,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(msg, color = if (isError) DangerRed else SuccessGreen, fontSize = 13.sp)
    }
}

@Composable
private fun TransactionRow(tx: BillingTransaction) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(BgCardElevated, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (tx.status == "confirmed") Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                contentDescription = null,
                tint = if (tx.status == "confirmed") SuccessGreen else TextSub,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.planKey ?: "top-up", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(tx.method.uppercase(), color = TextDim, fontSize = 11.sp)
        }
        Text("$${"%.0f".format(tx.amountUsd)}", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
