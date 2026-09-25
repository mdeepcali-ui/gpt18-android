package com.gptplus18.app.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.R
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary
import com.gptplus18.app.ui.theme.TextTertiary

private val GradientStart = Color(0xFF4A9EFF)
private val GradientEnd   = Color(0xFF2C5FE0)
private val AuthGradient = Brush.linearGradient(listOf(GradientStart, GradientEnd))
private val CardBg = Color(0xFF0E0E12)
private val BorderSubtle = Color(0xFF1F1F26)
private val RedAccent = Color(0xFFE63946)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    var isLoginTab by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onAuthSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Accent.copy(alpha = 0.12f), Color.Transparent)
                    )
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(50.dp))

            // ═══ الشعار الشفاف (بدون خلفية) ═══
            Image(
                painter = painterResource(id = R.drawable.logo_transparent),
                contentDescription = "GPT+18",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(160.dp),
            )

            Spacer(Modifier.height(12.dp))

            // ═══ GPT+18 — مع "+18" بحجم أكبر ولون أحمر ═══
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "GPT",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp,
                )
                Text(
                    "+18",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Black,
                    color = RedAccent,
                    letterSpacing = 0.sp,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.t_009),
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            // ═══ Tabs ═══
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AuthTab(stringResource(R.string.login), isLoginTab, Modifier.weight(1f)) { isLoginTab = true }
                AuthTab(stringResource(R.string.signup), !isLoginTab, Modifier.weight(1f)) { isLoginTab = false }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ بطاقة الحقول ═══
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBg)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!isLoginTab) {
                        AuthField(
                            value = name,
                            onValueChange = { name = it },
                            label = stringResource(R.string.t_038),
                            icon = "\ud83d\udc64",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    AuthField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.t_039),
                        icon = "\ud83d\udce7",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    Spacer(Modifier.height(12.dp))

                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.t_040),
                        icon = "\ud83d\udd12",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        isPassword = true,
                    )

                    if (state.error != null) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2A1518))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\u26a0\ufe0f", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    state.error!!,
                                    color = Color(0xFFFF7A7A),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ═══ زر الدخول الرئيسي ═══
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (state.isLoading) Brush.linearGradient(listOf(Color(0xFF1A1A22), Color(0xFF1A1A22)))
                                else AuthGradient
                            )
                            .clickable(enabled = !state.isLoading) {
                                if (isLoginTab) vm.login(email, password)
                                else vm.signup(name, email, password)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Accent,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isLoginTab) "\ud83d\udd13" else "\u2728", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isLoginTab) stringResource(R.string.login) else stringResource(R.string.signup),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ فاصل "أو" ═══
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(BorderSubtle))
                Text("  \u0623\u0648  ", color = TextTertiary, fontSize = 11.sp)
                Box(Modifier.weight(1f).height(1.dp).background(BorderSubtle))
            }

            Spacer(Modifier.height(16.dp))

            Spacer(Modifier.height(24.dp))

            Text(
                "\u0628\u0627\u0644\u0645\u062a\u0627\u0628\u0639\u0629 \u0623\u0646\u062a \u062a\u0648\u0627\u0641\u0642 \u0639\u0644\u0649 \u0627\u0644\u0634\u0631\u0648\u0637 \u0648\u0627\u0644\u0623\u062d\u0643\u0627\u0645",
                color = TextTertiary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AuthTab(
    text: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) Accent else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (active) Color.White else TextSecondary,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14141A))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(Modifier.width(10.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(label, color = TextTertiary, fontSize = 13.sp)
                },
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                keyboardOptions = keyboardOptions,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
