package com.example.cafe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cafe.ui.theme.*

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onCheckout: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalAmount by cartViewModel.totalAmount.collectAsState()
    
    val deliveryFee = if (totalAmount > 0) 1.50 else 0.0
    val discount = 0.0 // Could be from coupon
    val finalTotal = totalAmount + deliveryFee - discount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tu Carrito", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryCoffee)
            if (cartItems.isNotEmpty()) {
                TextButton(onClick = { cartViewModel.clearCart() }) {
                    Text("Vaciar", color = Color.Red)
                }
            }
        }

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡El carrito está sediento!", color = Color.Gray)
                    Text("Añade un café para empezar", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    ProfessionalCartItem(
                        item = item,
                        onAdd = { cartViewModel.addToCart(item.menuItem) },
                        onRemove = { cartViewModel.removeFromCart(item.menuItem) }
                    )
                }
                
                item {
                    PromoCodeSection()
                }
            }

            // Resumen de Pago
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    PaymentRow("Subtotal", "$${String.format("%.2f", totalAmount)}")
                    PaymentRow("Costo de Delivery", "$${String.format("%.2f", deliveryFee)}")
                    if (discount > 0) PaymentRow("Descuento", "-$${String.format("%.2f", discount)}", isDiscount = true)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PrimaryCoffee)
                        Text("$${String.format("%.2f", finalTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = AccentCinnamon)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Continuar al pago →", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalCartItem(item: CartItem, onAdd: () -> Unit, onRemove: () -> Unit) {
    val basePrice = item.menuItem.price.replace("$", "").toDoubleOrNull() ?: 0.0
    val unitPrice = if (item.menuItem.discount > 0) basePrice * (1 - item.menuItem.discount / 100.0) else basePrice

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.menuItem.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.menuItem.name, fontWeight = FontWeight.Bold, color = PrimaryCoffee)
                Text("$${String.format("%.2f", unitPrice)}", fontSize = 14.sp, color = AccentCinnamon)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Gray)
                }
                Text(item.quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = PrimaryCoffee)
                }
            }
        }
    }
}

@Composable
fun PaymentRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = if (isDiscount) Color.Red else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PromoCodeSection() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        placeholder = { Text("Código promocional (CAFE20)", fontSize = 14.sp) },
        trailingIcon = { 
            Text("Aplicar", color = PrimaryCoffee, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp).clickable { }) 
        },
        shape = RoundedCornerShape(12.dp)
    )
}
