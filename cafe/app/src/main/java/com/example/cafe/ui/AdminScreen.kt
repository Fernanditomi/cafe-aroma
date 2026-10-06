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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cafe.data.Product
import com.example.cafe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MenuViewModel,
    adminName: String,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    // Form fields
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isPopular by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Inventario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    name = ""
                    price = ""
                    description = ""
                    stock = "50"
                    category = "Café"
                    imageUrl = ""
                    isPopular = false
                    showAddDialog = true 
                }, 
                containerColor = PrimaryCoffee
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo", tint = White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(products) { product ->
                    AdminProductRow(
                        product = product,
                        onEdit = {
                            selectedProduct = product
                            name = product.name
                            price = product.price
                            stock = product.stock.toString()
                            category = product.category
                            imageUrl = product.imageUrl
                            isPopular = product.isPopular
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteProduct(product, adminName) }
                    )
                }
            }
        }

        if (showEditDialog && selectedProduct != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Producto") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") })
                        OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") })
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isPopular, onCheckedChange = { isPopular = it })
                            Text("Producto Popular 🔥")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateProduct(selectedProduct!!.copy(
                            name = name, 
                            price = price, 
                            stock = stock.toIntOrNull() ?: selectedProduct!!.stock,
                            isPopular = isPopular
                        ), adminName)
                        showEditDialog = false
                    }) {
                        Text("Actualizar")
                    }
                }
            )
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nuevo Producto") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, prefix = { Text("$") })
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
                        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL Imagen") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addProduct(name, price, description, category, imageUrl, adminName)
                        showAddDialog = false
                    }) {
                        Text("Añadir")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("Stock: ${product.stock}", color = if (product.stock < product.stockMin) Color.Red else Color.Gray, fontSize = 12.sp)
            }
            Text("$${product.price}", fontWeight = FontWeight.ExtraBold, color = PrimaryCoffee)
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = AccentCinnamon)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}
