package com.example.portalab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.portalab.ui.theme.PortaLabTheme
import com.example.portalab.uii.*
import com.example.portalab.ui.InventarioScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 Inicializa Firebase
        com.google.firebase.FirebaseApp.initializeApp(this)

        setContent {
            PortaLabTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(navController)
                    }
                    composable("login") {
                        LoginScreenStyled(navController)
                    }
                    composable("inventario") {
                        PantallaConMenu(navController = navController) { drawerState, scope ->
                            InventarioScreen(drawerState, scope)
                        }
                    }
                    composable("laboratorios") {
                        PantallaConMenu(navController = navController) { drawerState, scope ->
                            LaboratorioScreen(drawerState, scope)
                        }
                    }
                }
            }
        }
    }
}
