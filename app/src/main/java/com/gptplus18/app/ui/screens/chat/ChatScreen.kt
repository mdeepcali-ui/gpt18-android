package com.gptplus18.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gptplus18.app.data.models.Message
import com.gptplus18.app.data.models.Session
import com.gptplus18.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: ChatViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.currentSessionId == null) "محادثاتك" else "محادثة",
                        color = TextPrimary, fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    if (state.currentSessionId != null) {
                        IconButton(onClick = { vm.backToList() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = Accent)
                        }
                    }
                },
                actions = {
                    if (state.currentSessionId == null) {
                        IconButton(onClick = { vm.newChat() }) {
                            Icon(Icons.Default.Add, "جديدة", tint = Accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.currentSessionId == null) {
                SessionsList(
                    sessions = state.sessions,
                    onOpen = { vm.openSession(it.id) },
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        items(state.messages, key = { it.id.toString() + it.ts }) { msg ->
                            MessageBubble(msg)
                        }
                        if (state.isSending) {
                            item {
                                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Accent,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("يفكر...", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Composer(
                        value = input,
                        onValueChange = { input = it },
                        enabled = !state.isSending,
                        onSend = {
                            if (input.isNotBlank()) {
                                vm.send(input)
                                input = ""
                            }
                        },
                    )
                }
            }
        }
    }

    state.error?.let { err ->
        LaunchedEffect(err) {
            kotlinx.coroutines.delay(3000)
            vm.clearError()
        }
    }
}

@Composable
private fun SessionsList(sessions: List<Session>, onOpen: (Session) -> Unit) {
    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ما في محادثات بعد", color = TextSecondary, fontSize = 15.sp)
                Text("اضغط + لبدء محادثة", color = TextTertiary, fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp))
            }
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(sessions, key = { it.id }) { s ->
            Card(
                onClick = { onOpen(s) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(s.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    s.lastMsg?.let {
                        Text(
                            it.take(60),
                            color = TextSecondary, fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: Message) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isUser) Accent else BgSecondary,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp,
            ),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Text(
                text = msg.content,
                color = if (isUser) BgPrimary else TextPrimary,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit,
) {
    Surface(color = BgSecondary) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("اكتب رسالة...", color = TextTertiary) },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                maxLines = 5,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Accent,
                    focusedContainerColor = BgPrimary,
                    unfocusedContainerColor = BgPrimary,
                ),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                modifier = Modifier
                    .size(46.dp)
                    .background(if (value.isNotBlank()) Accent else BgTertiary, RoundedCornerShape(50)),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    "إرسال",
                    tint = if (value.isNotBlank()) BgPrimary else TextTertiary,
                )
            }
        }
    }
}
