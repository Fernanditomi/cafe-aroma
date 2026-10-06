package com.example.cafe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    totalAmount: Double,
    onPaymentConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Tarjeta") }
    var deliveryMethod by remember { mutableStateOf("Local") }
    var address by remember { mutableStateOf("") }
    
    val deliveryFee = if (deliveryMethod == "Delivery") 1.50 else 0.0
    val finalTotal = totalAmount + deliveryFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Pedido") },
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
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("MÉTODO DE ENTREGA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                DeliveryChip("En Local", Icons.Default.Store, deliveryMethod == "Local") { deliveryMethod = "Local" }
                Spacer(modifier = Modifier.width(12.dp))
                DeliveryChip("A Domicilio", Icons.Default.DeliveryDining, deliveryMethod == "Delivery") { deliveryMethod = "Delivery" }
            }

            if (deliveryMethod == "Delivery") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección de entrega") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("MÉTODO DE PAGO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(12.dp))

            PaymentMethodOption(
                title = "Tarjeta de Crédito / Débito",
                icon = Icons.Default.CreditCard,
                isSelected = selectedMethod == "Tarjeta",
                onClick = { selectedMethod = "Tarjeta" }
            )
            Spacer(modifier = Modifier.height(12.dp))
            PaymentMethodOption(
                title = "Efectivo",
                icon = Icons.Default.Payments,
                isSelected = selectedMethod == "Efectivo",
                onClick = { selectedMethod = "Efectivo" }
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // Total Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = Color.Gray)
                        Text("$${String.format("%.2f", totalAmount)}")
                    }
                    if (deliveryFee > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery", color = Color.Gray)
                            Text("$${String.format("%.2f", deliveryFee)}")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", finalTotal)}", fontWeight = FontWeight.Bold, color = PrimaryCoffee, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onPaymentConfirmed(selectedMethod) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                shape = RoundedCornerShape(16.dp),
                enabled = if (deliveryMethod == "Delivery") address.isNotBlank() else true
            ) {
                Text("Confirmar Pedido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeliveryChip(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) PrimaryCoffee else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) White else Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = if (isSelected) White else Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun PaymentMethodOption(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, PrimaryCoffee) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) PrimaryCoffee else Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, modifier = Modifier.weight(1f), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = PrimaryCoffee))
        }
    }
}
