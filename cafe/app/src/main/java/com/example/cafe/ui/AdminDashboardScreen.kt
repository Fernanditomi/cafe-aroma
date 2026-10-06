package com.example.cafe.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.ui.theme.AccentCinnamon
import com.example.cafe.ui.theme.PrimaryCoffee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    userName: String,
    onNavigateToProducts: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToPromotions: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val totalSales by viewModel.totalSales.collectAsState()
    val totalOrders by viewModel.totalOrders.collectAsState()
    val totalUsers by viewModel.totalUsers.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            HeaderSection(
                userName = userName, 
                avatarUrl = "",
                onAvatarClick = onNavigateToSettings,
                onSettingsClick = onNavigateToSettings
            ) // Professional common header

            Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "DASHBOARD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Grid
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AdminStatCard(
                        title = "Ventas Hoy",
                        value = "$${String.format("%.2f", totalSales)}",
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Pedidos",
                        value = totalOrders.toString(),
                        icon = Icons.Default.ShoppingBasket,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    AdminStatCard(
                        title = "Clientes",
                        value = totalUsers.toString(),
                        icon = Icons.Default.People,
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Inventario",
                        value = "92%",
                        icon = Icons.Default.Inventory,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "GESTIÓN OPERATIVA",
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.tertiary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            AdminMenuOptionLarge("Control de Pedidos", "Gestiona órdenes y estados en vivo", Icons.Default.ListAlt, onNavigateToOrders)
            AdminMenuOptionLarge("Catálogo de Productos", "Añade, edita y ajusta stock", Icons.Default.RestaurantMenu, onNavigateToProducts)
            AdminMenuOptionLarge("Marketing & Descuentos", "Cupones y promociones activas", Icons.Default.LocalOffer, onNavigateToPromotions)
            AdminMenuOptionLarge("Reportes Detallados", "Analiza ventas e ingresos", Icons.Default.BarChart, onNavigateToReports)
            AdminMenuOptionLarge("Gestión de Clientes", "Ver perfiles e historial de compras", Icons.Default.People, onNavigateToUsers)
            AdminMenuOptionLarge("Actividad Reciente", "Auditoría de acciones realizadas", Icons.Default.History, onNavigateToActivity)
            AdminMenuOptionLarge("Ajustes de Tienda", "Horarios, dirección y delivery", Icons.Default.Storefront, onNavigateToConfig)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdminMenuOptionLarge(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(PrimaryCoffee.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryCoffee)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
