package com.example.cafe.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafe.data.ActivityLog
import com.example.cafe.data.AppDatabase
import com.example.cafe.data.Favorite
import com.example.cafe.data.Product
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val productDao = database.productDao()
    private val favoriteDao = database.favoriteDao()
    private val activityLogDao = database.activityLogDao()

    val products = productDao.getAllProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            if (productDao.getProductCount() == 0) {
                seedDatabase()
            }
        }
    }

    private suspend fun seedDatabase() {
        val initialProducts = listOf(
            Product(name = "Espresso", price = "2.50", description = "Intenso y aromático", category = "Café", 
                imageUrl = "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&q=80&w=800", isPopular = true),
            Product(name = "Cappuccino", price = "3.50", description = "Espuma cremosa y canela", category = "Café", 
                imageUrl = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&q=80&w=800", isPopular = true),
            Product(name = "Latte Macchiato", price = "4.00", description = "Leche manchada con espresso", category = "Café", 
                imageUrl = "https://images.unsplash.com/photo-1599398054066-846f28917f38?auto=format&fit=crop&q=80&w=800"),
            Product(name = "Americano", price = "3.00", description = "Espresso suave con agua", category = "Café", 
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&q=80&w=800"),
            Product(name = "Muffin de Chocolate", price = "2.00", description = "Recién horneado", category = "Panadería", 
                imageUrl = "https://images.unsplash.com/photo-1582298538104-fe2e74c27f59?auto=format&fit=crop&q=80&w=800"),
            Product(name = "Croissant", price = "2.50", description = "Hojaldre artesanal con mantequilla", category = "Panadería", 
                imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&q=80&w=800")
        )
        initialProducts.forEach { productDao.insertProduct(it) }
    }

    fun getFavorites(userId: Int) = favoriteDao.getFavoritesForUser(userId)

    fun toggleFavorite(userId: Int, productId: Int) {
        viewModelScope.launch {
            val existing = favoriteDao.getFavorite(userId, productId)
            if (existing != null) {
                favoriteDao.removeFavorite(existing)
            } else {
                favoriteDao.addFavorite(Favorite(userId = userId, productId = productId))
            }
        }
    }

    private fun logActivity(userName: String, action: String, details: String) {
        viewModelScope.launch {
            activityLogDao.insertLog(ActivityLog(userName = userName, action = action, details = details))
        }
    }

    fun updateProduct(product: Product, adminName: String) {
        viewModelScope.launch {
            productDao.updateProduct(product)
            logActivity(adminName, "ACTUALIZAR", "Se modificó el producto: ${product.name}")
        }
    }

    fun addProduct(name: String, price: String, description: String, category: String, imageUrl: String, adminName: String) {
        viewModelScope.launch {
            productDao.insertProduct(Product(name = name, price = price, description = description, category = category, imageUrl = imageUrl))
            logActivity(adminName, "CREAR", "Se añadió el producto: $name")
        }
    }
    
    fun deleteProduct(product: Product, adminName: String) {
        viewModelScope.launch {
            productDao.deleteProduct(product)
            logActivity(adminName, "ELIMINAR", "Se eliminó el producto: ${product.name}")
        }
    }
}
