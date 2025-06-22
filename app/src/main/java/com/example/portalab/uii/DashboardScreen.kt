import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    var totalLaboratorios by remember { mutableStateOf(0) }
    var totalEquipos by remember { mutableStateOf(0) }
    var totalSoftwares by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        db.collection("laboratorios").get().addOnSuccessListener { totalLaboratorios = it.size() }
        db.collection("equipos").get().addOnSuccessListener { totalEquipos = it.size() }
        db.collection("software").get().addOnSuccessListener { totalSoftwares = it.size() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Principal") },
                navigationIcon = {
                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF263238),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Resumen General", style = MaterialTheme.typography.headlineSmall, color = Color.White)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardItem("Laboratorios", totalLaboratorios, Icons.Default.Lan, Color(0xFF26C6DA), Modifier.weight(1f)) {
                    navController.navigate("laboratorios")
                }
                DashboardItem("Equipos", totalEquipos, Icons.Default.Inventory, Color(0xFFAB47BC), Modifier.weight(1f)) {
                    navController.navigate("inventario")
                }
                DashboardItem("Softwares", totalSoftwares, Icons.Default.Star, Color(0xFFFFC107), Modifier.weight(1f)) {
                    navController.navigate("software")
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Visualización Gráfica", style = MaterialTheme.typography.titleMedium, color = Color.White)
            AsyncImage(
                model = "file:///mnt/data/resumen_general_dashboard.png",
                contentDescription = "Gráfico de resumen",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun DashboardItem(
    title: String,
    value: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B))
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Text("$value", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text(title, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}
