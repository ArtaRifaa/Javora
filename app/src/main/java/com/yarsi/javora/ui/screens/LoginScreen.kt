package com.yarsi.javora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.data.remote.AppwriteService
import com.yarsi.javora.ui.components.JavoraLogo
import com.yarsi.javora.ui.theme.*
import kotlinx.coroutines.launch
// wayau wayau wayau
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current
    val appwriteService = remember { if (isPreview) null else AppwriteService(context) }

    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), JavoraDarkBg)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo inside white box
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    JavoraLogo(size = 80.dp, showText = false)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Javora",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = JavoraGradient
                )
            )

            Text(
                text = if (isLoginMode) "Siap Jadi Master Java?" else "Mulai Petualangan Java-mu!",
                color = Color.LightGray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = JavoraCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (!isLoginMode) {
                        Text(
                            text = "NAMA LENGKAP",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Nama Anda", color = Color.DarkGray) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JavoraOrange,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = "EMAIL",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("nama@email.com", color = Color.DarkGray) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JavoraOrange,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "KATA SANDI",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("........", color = Color.DarkGray) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JavoraOrange,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (isLoginMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Lupa Kata Sandi?", color = Color(0xFFCC8B3C))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { 
                            if (!isLoading) {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = if (isLoginMode) {
                                        appwriteService?.login(email, password)
                                    } else {
                                        appwriteService?.signUp(email, password, name)
                                    }
                                    
                                    if (result?.isSuccess == true) {
                                        if (!isLoginMode) {
                                            // Auto login after signup
                                            appwriteService?.login(email, password)
                                        }
                                        onLoginSuccess()
                                    } else {
                                        val error = result?.exceptionOrNull()
                                        errorMessage = error?.message ?: "Terjadi kesalahan sistem."
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                brush = JavoraGradient,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isLoginMode) "Masuk" else "Daftar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(if (isLoginMode) "Belum punya akun? " else "Sudah punya akun? ", color = Color.Gray)
                        Text(
                            text = if (isLoginMode) "Daftar Sekarang" else "Masuk Sekarang",
                            color = Color(0xFFCC8B3C),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { 
                                isLoginMode = !isLoginMode
                                errorMessage = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aman", color = Color.Gray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Akses Cepat", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    JavoraTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
