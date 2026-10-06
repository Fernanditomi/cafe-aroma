package com.example.cafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.data.Order
import com.example.cafe.ui.theme.PrimaryCoffee

@Composable
fun UserOrdersScreen(
    cartViewModel: CartViewModel
) {
    val orders by cartViewModel.userOrders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        Text(
            text = "Mis Pedidos",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryCoffee,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (orders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aún no tienes pedidos", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(orders) { order ->
                    ProfessionalUserOrderCard(order)
                }
            }
        }
    }
}

@Composable
fun ProfessionalUserOrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Orden #${order.orderId}", fontWeight = FontWeight.Bold, color = PrimaryCoffee)
                    Text(text = "22 Julio, 14:30", fontSize = 12.sp, color = Color.Gray)
                }
                StatusBadge(order.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Items summary
            Text(text = order.itemsSummary, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar simulation
            OrderProgressBar(order.status)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total: $${String.format("%.2f", order.totalAmount)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryCoffee)
                Text(text = "Ver detalles →", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OrderProgressBar(status: String) {
    val progress = when (status) {
        "NUEVO" -> 0.2f
        "CONFIRMADO" -> 0.4f
        "PREPARANDO" -> 0.6f
        "LISTO" -> 0.8f
        "ENTREGADO" -> 1.0f
        else -> 0.0f
    }
    
    val statusText = when (status) {
        "PREPARANDO" -> "Estamos moliendo tus granos... ☕"
        "LISTO" -> "¡Tu café te espera en barra! 🏃"
        "ENTREGADO" -> "¡Disfruta tu pedido! ✨"
        else -> "Procesando tu orden..."
    }

    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = PrimaryCoffee,
            trackColor = PrimaryCoffee.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = statusText, fontSize = 12.sp, color = PrimaryCoffee, fontWeight = FontWeight.Medium)
    }
}
