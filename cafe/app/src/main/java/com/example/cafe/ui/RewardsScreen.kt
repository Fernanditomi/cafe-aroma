package com.example.cafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.ui.theme.AccentCinnamon
import com.example.cafe.ui.theme.PrimaryCoffee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(userViewModel: UserViewModel, onBack: () -> Unit) {
    val user by userViewModel.currentUser.collectAsState()
    val points = user?.points ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Recompensas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryCoffee),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(48.dp))
                    Text("Puntos Acumulados", color = Color.White.copy(alpha = 0.7f))
                    Text("$points", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("RECOMPENSAS DISPONIBLES", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            RewardItem("Café Gratis", "500 puntos", points >= 500)
            RewardItem("20% Descuento", "1,000 puntos", points >= 1000)
            RewardItem("Muffin de Regalo", "800 puntos", points >= 800)
        }
    }
}

@Composable
fun RewardItem(title: String, cost: String, isAvailable: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = AccentCinnamon)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(cost, fontSize = 12.sp, color = Color.Gray)
            }
            Button(
                onClick = { },
                enabled = isAvailable,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee)
            ) {
                Text(if (isAvailable) "Canjear" else "Faltan puntos", fontSize = 10.sp)
            }
        }
    }
}
