package com.galyxnovatec.tienda.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isFromUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalyAiChatScreen(alVolver: () -> Unit) {
    var messages by remember { mutableStateOf(listOf(ChatMessage("¡Hola! Soy GALY AI, tu experto en tecnología. Puedo buscar entre nuestros 100 productos para recomendarte lo mejor. ¿Qué necesitas hoy?", false))) }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("GALY Inteligencia Artificial", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val pulse by rememberInfiniteTransition().animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                            )
                            Box(Modifier.size(8.dp).alpha(pulse).background(Color(0xFF00FF41), CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("Sincronizado con Python Pro", fontSize = 10.sp, color = Color(0xFF00FF41), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
                if (isTyping) {
                    item { Text("GALY AI está pensando...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp)) }
                }
            }

            // Input de chat
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF111111),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Escribe aquí...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ColorGalyRed
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val userMsg = inputText
                                messages = messages + ChatMessage(userMsg, true)
                                inputText = ""
                                isTyping = true
                                
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.aiInstance.getAiResponse(userMsg)
                                        messages = messages + ChatMessage(response["reply"] ?: "No entendí eso, ¿puedes repetirlo?", false)
                                    } catch (e: Exception) {
                                        messages = messages + ChatMessage("Error: No pude conectarme con mi servidor Python.", false)
                                    } finally {
                                        isTyping = false
                                    }
                                }
                            }
                        },
                        containerColor = ColorGalyRed,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (message.isFromUser) ColorGalyRed else Color(0xFF1A1A1A),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isFromUser) 20.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 20.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                modifier = Modifier.padding(14.dp),
                fontSize = 15.sp
            )
        }
    }
}
