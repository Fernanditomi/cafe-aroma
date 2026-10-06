package com.example.cafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.data.Product
import com.example.cafe.ui.theme.PrimaryCoffee
import com.example.cafe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(
    viewModel: MenuViewModel,
    adminName: String,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var discount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promociones y Descuentos") },
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
                .padding(16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(products) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = PrimaryCoffee)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold)
                                if (product.discount > 0) {
                                    Text("${product.discount}% OFF", color = Color.Red, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Sin descuento", color = Color.Gray)
                                }
                            }
                            Button(
                                onClick = {
                                    selectedProduct = product
                                    discount = product.discount.toString()
                                    showDialog = true
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ajustar")
                            }
                        }
                    }
                }
            }
        }

        if (showDialog && selectedProduct != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Aplicar Descuento") },
                text = {
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Porcentaje de Descuento (%)") },
                        prefix = { Text("%") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val dValue = discount.trim().toIntOrNull() ?: 0
                        // Asegurar que el descuento esté entre 0 y 100
                        val clampedDiscount = dValue.coerceIn(0, 100)
                        viewModel.updateProduct(selectedProduct!!.copy(discount = clampedDiscount), adminName)
                        showDialog = false
                    }) {
                        Text("Aplicar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
