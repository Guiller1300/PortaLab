package com.example.portalab.uii

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.portalab.ui.theme.PortaLabTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.portalab.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenStyled(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = Color(0xFFECEFF1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.unan_managua),
                            contentDescription = "Logo UNAN",
                            modifier = Modifier
                                .width(264.dp)
                                .height(120.dp)
                        )

                        Text(
                            text = "SIUDT",
                            color = Color(0xFF005BAC),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                fontFamily = FontFamily.Serif
                            ),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.person_24dp),
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                loading = true
                                auth.signInWithEmailAndPassword(username.trim(), password.trim())
                                    .addOnCompleteListener { task ->
                                        loading = false
                                        if (task.isSuccessful) {
                                            navController.navigate("inventario") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Error: ${task.exception?.localizedMessage ?: "Login fallido"}"
                                                )
                                            }
                                        }
                                    }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF005BAC)
                            ),
                            enabled = username.isNotBlank() && password.isNotBlank() && !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Acceder", color = Color.White)
                            }
                        }

                        Text(
                            text = "¿Olvidó la contraseña?",
                            color = Color(0xFF3B84F1),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


