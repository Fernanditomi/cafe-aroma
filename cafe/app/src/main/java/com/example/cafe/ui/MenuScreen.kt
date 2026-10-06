package com.example.cafe.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cafe.data.Product
import com.example.cafe.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(
    userId: Int,
    cartViewModel: CartViewModel,
    menuViewModel: MenuViewModel,
    onNavigateToCart: () -> Unit
) {
    val products by menuViewModel.products.collectAsState()
    val cartItemsCount by cartViewModel.totalItems.collectAsState()
    val favorites by menuViewModel.getFavorites(userId).collectAsState(initial = emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var sortBy by remember { mutableStateOf("Popular") }

    val categories = listOf("Todos", "Café", "Panadería", "Bebidas", "Postres")

    val filteredProducts = products.filter { product ->
        (selectedCategory == "Todos" || product.category == selectedCategory) &&
        (product.name.contains(searchQuery, ignoreCase = true) || product.description.contains(searchQuery, ignoreCase = true))
    }.sortedBy { 
        when (sortBy) {
            "Precio: Menor" -> it.price.replace("$", "").toDoubleOrNull() ?: 0.0
            "Precio: Mayor" -> -(it.price.replace("$", "").toDoubleOrNull() ?: 0.0)
            else -> if (it.isPopular) -1.0 else 0.0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header con Búsqueda
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menú", 
                    fontSize = 36.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = PrimaryCoffee
                )
                
                IconButton(
                    onClick = onNavigateToCart,
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryCoffee, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart, 
                            contentDescription = null, 
                            tint = Color.White, 
                            modifier = Modifier.size(24.dp)
                        )
                        if (cartItemsCount > 0) {
                            Surface(
                                color = Color.Red,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .offset(x = 4.dp, y = (-4).dp)
                            ) {
                                Text(
                                    text = cartItemsCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.wrapContentSize(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
            
            SearchBarStyled(query = searchQuery, onQueryChange = { searchQuery = it })
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        color = if (isSelected) PrimaryCoffee else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            color = if (isSelected) Color.White else Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Listado de Productos
        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No encontramos lo que buscas ☕", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProducts) { product ->
                    val cartItems by cartViewModel.cartItems.collectAsState()
                    val quantity = cartItems.find { it.menuItem.id == product.id }?.quantity ?: 0
                    val isFav = favorites.any { it.productId == product.id }

                    ProfessionalMenuCard(
                        userId = userId,
                        product = product,
                        quantity = quantity,
                        isFavorite = isFav,
                        onFavoriteToggle = { menuViewModel.toggleFavorite(userId, product.id) },
                        onAdd = { cartViewModel.addToCart(product) },
                        onRemove = { cartViewModel.removeFromCart(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalMenuCard(
    userId: Int,
    product: Product,
    quantity: Int,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    var isAnimated by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimated) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del Producto con Badges y Favorito
            Box(modifier = Modifier.size(110.dp)) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Botón de Favorito sobre la imagen (Libera espacio para el nombre)
                Surface(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clickable { onFavoriteToggle() }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.DarkGray,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                if (product.discount > 0) {
                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(bottomEnd = 12.dp, topStart = 16.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-${product.discount}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                if (product.isPopular) {
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.9f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = PrimaryCoffee,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información Detallada
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 17.sp, 
                    color = PrimaryCoffee,
                    maxLines = 2, // Permite hasta 2 líneas para nombres muy largos
                    lineHeight = 20.sp
                )

                // Rating Alineado
                if (product.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.rating.toString(), 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = product.description, 
                    fontSize = 12.sp, 
                    color = Color.Gray, 
                    maxLines = 2,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Precio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val priceVal = product.price.replace("$", "").toDoubleOrNull() ?: 0.0
                    if (product.discount > 0) {
                        val finalPrice = priceVal * (1 - product.discount / 100.0)
                        Text("$${String.format("%.2f", finalPrice)}", fontWeight = FontWeight.ExtraBold, color = Color.Red, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$${product.price}", style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough), color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Text("$${product.price}", fontWeight = FontWeight.ExtraBold, color = PrimaryCoffee, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Control de Cantidad o Agregar
            if (quantity > 0) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.background(PrimaryCoffee.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(4.dp)
                ) {
                    IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryCoffee)
                    }
                    Text(
                        text = quantity.toString(), 
                        fontWeight = FontWeight.ExtraBold, 
                        color = PrimaryCoffee,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Gray)
                    }
                }
            } else {
                IconButton(
                    onClick = {
                        onAdd()
                        scope.launch { isAnimated = true; delay(150); isAnimated = false }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryCoffee),
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}
