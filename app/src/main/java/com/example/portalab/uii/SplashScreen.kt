package com.example.portalab.uii

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.portalab.R

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.unan_managua),
            contentDescription = "Logo UNAN",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("SIUDT", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
    }
}