package com.gptplus18.app.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gptplus18.app.R
import com.gptplus18.app.ui.theme.Accent
import com.gptplus18.app.ui.theme.BgPrimary
import com.gptplus18.app.ui.theme.TextPrimary
import com.gptplus18.app.ui.theme.TextSecondary

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

    Box(
        modifier = Modifier.fillMaxSize().background(BgPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(40.dp))

            // ═══ الشعار ═══
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "GPT+18",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 12.dp),
            )

            // ═══ العنوان ═══
            Text(
                "GPT+18",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Accent,
            )
            Text(
                "ذكاء اصطناعي بلا قيود",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
            )

            // ═══ Tabs ═══
            Row(horizontalArrangement = Arrangement.Center) {
                TabButton("تسجيل دخول", isLoginTab) { isLoginTab = true }
                Spacer(Modifier.width(8.dp))
                TabButton("حساب جديد", !isLoginTab) { isLoginTab = false }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ حقل الاسم (Signup فقط) ═══
            if (!isLoginTab) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("الاسم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = darkFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(Modifier.height(12.dp))
            }

            // ═══ الإيميل ═══
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("الإيميل") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = darkFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(Modifier.height(12.dp))

            // ═══ كلمة السر ═══
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("كلمة السر") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = darkFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            // ═══ الخطأ ═══
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    state.error!!,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ═══ زر الدخول/التسجيل ═══
            val loginInteraction = remember { MutableInteractionSource() }
            val loginPressed by loginInteraction.collectIsPressedAsState()
            val loginScale by animateFloatAsState(
                targetValue = if (loginPressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
                label = "loginScale",
            )

            Button(
                interactionSource = loginInteraction,
                onClick = {
                    if (isLoginTab) vm.login(email, password)
                    else vm.signup(name, email, password)
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(loginScale),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = BgPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        if (isLoginTab) "دخول" else "إنشاء حساب",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BgPrimary,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ فاصل "أو" ═══
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HorizontalDivider(Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.2f))
                Text("  أو  ", color = TextSecondary, fontSize = 13.sp)
                HorizontalDivider(Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.2f))
            }

            Spacer(Modifier.height(16.dp))

            // ═══ زر Google ═══
            val ctx = LocalContext.current
            val googleInteraction = remember { MutableInteractionSource() }
            val googlePressed by googleInteraction.collectIsPressedAsState()
            val googleScale by animateFloatAsState(
                targetValue = if (googlePressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
                label = "googleScale",
            )

            OutlinedButton(
                interactionSource = googleInteraction,
                onClick = {
                    com.gptplus18.app.util.GoogleAuthLauncher.launch(ctx)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(loginScale),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    "الدخول بحساب Google",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TabButton(text: String, active: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text,
            color = if (active) Accent else TextSecondary,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = Accent,
    unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
    focusedLabelColor = Accent,
    unfocusedLabelColor = TextSecondary,
    cursorColor = Accent,
)
