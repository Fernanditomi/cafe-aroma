package com.example.cafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.data.Order
import com.example.cafe.ui.theme.PrimaryCoffee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("TODOS", "NUEVOS", "PREPARANDO", "LISTOS", "ENTREGADOS")

    val filteredOrders = orders.filter { order ->
        when (selectedTab) {
            1 -> order.status == "NUEVO"
            2 -> order.status == "PREPARANDO"
            3 -> order.status == "LISTO"
            4 -> order.status == "ENTREGADO"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryCoffee,
                edgePadding = 16.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay pedidos en esta categoría", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        AdminOrderCardProfessional(
                            order = order,
                            onStatusChange = { newStatus -> viewModel.updateOrderStatus(order, newStatus) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCardProfessional(order: Order, onStatusChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Orden #${order.orderId}", fontWeight = FontWeight.Bold, color = PrimaryCoffee)
                    Text(text = order.customerName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                StatusBadge(order.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = order.itemsSummary, color = Color.Gray, fontSize = 14.sp)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$${String.format("%.2f", order.totalAmount)}", fontWeight = FontWeight.ExtraBold, color = PrimaryCoffee, fontSize = 18.sp)
                
                // Action Buttons based on status
                Row {
                    if (order.status == "NUEVO") {
                        Button(
                            onClick = { onStatusChange("CONFIRMADO") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Confirmar", fontSize = 11.sp)
                        }
                    }
                    if (order.status == "CONFIRMADO") {
                        Button(
                            onClick = { onStatusChange("PREPARANDO") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Preparar", fontSize = 11.sp)
                        }
                    }
                    if (order.status == "PREPARANDO") {
                        Button(
                            onClick = { onStatusChange("LISTO") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Listo", fontSize = 11.sp)
                        }
                    }
                    if (order.status == "LISTO") {
                        Button(
                            onClick = { onStatusChange("ENTREGADO") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Entregar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
