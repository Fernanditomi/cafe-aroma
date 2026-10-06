package com.example.cafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cafe.ui.theme.PrimaryCoffee

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToPayments: () -> Unit
) {
    val user by userViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Upper Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryCoffee)
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.avatarUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = user?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user?.name ?: "Usuario", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(user?.email ?: "", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Points Badge
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${user?.points ?: 0} Puntos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // List of Options
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .padding(24.dp)
        ) {
            ProfileOption("Mis Pedidos", Icons.Default.ReceiptLong, onNavigateToOrders)
            ProfileOption("Ajustes de Cuenta", Icons.Default.Settings, onNavigateToSettings)
            ProfileOption("Mis Favoritos", Icons.Default.Favorite, onNavigateToFavorites)
            ProfileOption("Mis Recompensas 🎁", Icons.Default.Redeem, onNavigateToRewards)
            ProfileOption("Mis Direcciones", Icons.Default.LocationOn, onNavigateToAddresses)
            ProfileOption("Métodos de Pago", Icons.Default.Payment, onNavigateToPayments)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { userViewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProfileOption(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryCoffee, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}
