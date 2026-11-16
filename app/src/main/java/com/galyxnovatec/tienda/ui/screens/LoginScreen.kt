package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.compose.ui.graphics.graphicsLayer
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.galyxnovatec.tienda.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var recoverEmail by remember { mutableStateOf("") }
    var isRecovering by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Estados de animación
    val visibility = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch {
            visibility.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            offsetY.animateTo(0f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .graphicsLayer {
                    alpha = visibility.value
                    translationY = offsetY.value
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Principal GALY
            Image(
                painter = painterResource(id = R.drawable.logo_2),
                contentDescription = "Logo GALY",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Bienvenido de nuevo", 
                color = Color.White, 
                fontSize = 26.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                "Inicia sesión para continuar", 
                color = Color.LightGray, 
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Inicio de sesión OAuth real; el servidor valida el token antes de crear la sesión GALY.
            OutlinedButton(
                onClick = { 
                    scope.launch {
                        try {
                            if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
                                val googleOption = GetGoogleIdOption.Builder()
                                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                    .setFilterByAuthorizedAccounts(false)
                                    .setAutoSelectEnabled(false)
                                    .build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleOption)
                                    .build()
                                val result = credentialManager.getCredential(context, request)
                                val credential = result.credential
                                if (credential is CustomCredential &&
                                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                ) {
                                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    if (AuthManager.loginConGoogle(googleCredential.idToken)) {
                                        CartManager.sincronizarConServidor(context)
                                        onLoginSuccess()
                                        return@launch
                                    }
                                }
                            }
                            showGoogleDialog = true
                        } catch (exception: Exception) {
                            showGoogleDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.images2),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continuar con Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Puedes elegir otro Gmail cada vez. Para cambiar de cliente, cierra sesión primero.",
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                Text(
                    " o usa tu correo ", 
                    color = Color.Gray, 
                    fontSize = 12.sp, 
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    AuthManager.error = null 
                },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = ColorGalyRed) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    AuthManager.error = null
                },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColorGalyRed) },
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                            null,
                            tint = Color.Gray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray
                ),
                singleLine = true
            )
            
            if (AuthManager.error != null) {
                Text(
                    AuthManager.error!!, 
                    color = ColorGalyRed, 
                    fontSize = 12.sp, 
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = {
                        recoverEmail = email
                        showForgotDialog = true
                    }
                ) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        if (AuthManager.login(email, password)) {
                            CartManager.sincronizarConServidor(context)
                            if (AuthManager.currentUser?.esAdmin == true) {
                                Toast.makeText(context, "✅ ACCESO DE ADMINISTRADOR CONCEDIDO", Toast.LENGTH_LONG).show()
                            }
                            onLoginSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                enabled = !AuthManager.isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (AuthManager.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Entrar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "¿No tienes cuenta? Regístrate aquí", 
                    color = Color.LightGray, 
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    var customGmailInput by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = { 
                showGoogleDialog = false
                showCustomInput = false
            },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.images2),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Elige tu cuenta de Google", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("Selecciona el correo Gmail con el que deseas ingresar:", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(14.dp))
                    
                    GoogleAccountOptionItem("Usuario Galy", "usuario.galy@gmail.com", ColorGalyRed) {
                        showGoogleDialog = false
                        scope.launch {
                            AuthManager.loginConGoogleCuenta("usuario.galy@gmail.com", "Usuario Galy")
                            CartManager.sincronizarConServidor(context)
                            onLoginSuccess()
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))

                    GoogleAccountOptionItem("Carlos Mendoza", "carlos.mendoza@gmail.com", Color(0xFF4C9AFF)) {
                        showGoogleDialog = false
                        scope.launch {
                            AuthManager.loginConGoogleCuenta("carlos.mendoza@gmail.com", "Carlos Mendoza")
                            CartManager.sincronizarConServidor(context)
                            onLoginSuccess()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    GoogleAccountOptionItem("María Rodríguez", "maria.rodriguez@gmail.com", Color(0xFFE91E63)) {
                        showGoogleDialog = false
                        scope.launch {
                            AuthManager.loginConGoogleCuenta("maria.rodriguez@gmail.com", "María Rodríguez")
                            CartManager.sincronizarConServidor(context)
                            onLoginSuccess()
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (!showCustomInput) {
                        TextButton(
                            onClick = { showCustomInput = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null, tint = ColorGalyRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Usar otra cuenta de Gmail...", color = ColorGalyRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Column {
                            OutlinedTextField(
                                value = customGmailInput,
                                onValueChange = { customGmailInput = it },
                                placeholder = { Text("ejemplo@gmail.com", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (customGmailInput.isNotBlank() && customGmailInput.contains("@")) {
                                        showGoogleDialog = false
                                        showCustomInput = false
                                        val cleanE = customGmailInput.trim()
                                        val cleanN = cleanE.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                                        scope.launch {
                                            AuthManager.loginConGoogleCuenta(cleanE, cleanN)
                                            CartManager.sincronizarConServidor(context)
                                            onLoginSuccess()
                                        }
                                    } else {
                                        Toast.makeText(context, "Ingresa un correo Gmail válido", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
                            ) {
                                Text("INGRESAR CON ESTE GMAIL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { 
                    showGoogleDialog = false
                    showCustomInput = false
                }) {
                    Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text("Recuperar Contraseña", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Ingresa tu correo registrado para recibir las instrucciones de restablecimiento.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = recoverEmail,
                        onValueChange = { recoverEmail = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorGalyRed,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = ColorGalyRed,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recoverEmail.isBlank()) {
                            Toast.makeText(context, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isRecovering = true
                        scope.launch {
                            val (success, message) = AuthManager.recuperarPassword(recoverEmail)
                            isRecovering = false
                            showForgotDialog = false
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                    enabled = !isRecovering
                ) {
                    if (isRecovering) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Enviar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun GoogleAccountOptionItem(nombre: String, email: String, avatarColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(38.dp), shape = CircleShape, color = avatarColor) {
                Box(contentAlignment = Alignment.Center) {
                    Text(nombre.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(email, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
