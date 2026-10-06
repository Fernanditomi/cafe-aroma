package com.example.cafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
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

@Composable
fun FavoritesScreen(
    userId: Int,
    viewModel: MenuViewModel,
    cartViewModel: CartViewModel
) {
    val allProducts by viewModel.products.collectAsState()
    val favorites by viewModel.getFavorites(userId).collectAsState(initial = emptyList())
    
    val favoriteProducts = allProducts.filter { prod -> 
        favorites.any { it.productId == prod.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        Text(
            text = "Mis Favoritos ❤️",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryCoffee,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (favoriteProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aún no tienes favoritos", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(favoriteProducts) { product ->
                    FavoriteProductRow(
                        product = product,
                        onRemove = { viewModel.toggleFavorite(userId, product.id) },
                        onAddToCart = { cartViewModel.addToCart(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteProductRow(product: Product, onRemove: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = PrimaryCoffee)
                Text("$${product.price}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
            }
            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir", fontSize = 11.sp)
            }
        }
    }
}
