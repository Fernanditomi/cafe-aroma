package com.example.cafe.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafe.data.AppDatabase
import com.example.cafe.data.Order
import com.example.cafe.data.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartItem(
    val menuItem: Product,
    val quantity: Int = 1
)

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val orderDao = AppDatabase.getDatabase(application).orderDao()
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    val totalItems = MutableStateFlow(0)
    val totalAmount = MutableStateFlow(0.0)

    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders = _userOrders.asStateFlow()

    fun fetchUserOrders(name: String) {
        viewModelScope.launch {
            orderDao.getOrdersByCustomer(name).collect { orders ->
                _userOrders.value = orders
            }
        }
    }

    fun addToCart(item: Product) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.menuItem.id == item.id }
            if (existingItem != null) {
                currentItems.map {
                    if (it.menuItem.id == item.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                currentItems + CartItem(item)
            }
        }
        updateTotals()
    }

    fun removeFromCart(item: Product) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.menuItem.id == item.id }
            if (existingItem != null) {
                if (existingItem.quantity > 1) {
                    currentItems.map {
                        if (it.menuItem.id == item.id) it.copy(quantity = it.quantity - 1) else it
                    }
                } else {
                    currentItems.filter { it.menuItem.id != item.id }
                }
            } else {
                currentItems
            }
        }
        updateTotals()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        updateTotals()
    }

    fun completePurchase(customerName: String, userViewModel: UserViewModel, onComplete: (Long) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val total = totalAmount.value
        val summary = items.joinToString { "${it.quantity}x ${it.menuItem.name}" }
        val order = Order(
            customerName = customerName,
            totalAmount = total,
            itemsSummary = summary
        )

        viewModelScope.launch {
            val id = orderDao.insertOrder(order)
            // Cada $1 genera 10 puntos
            userViewModel.addPoints((total * 10).toInt())
            clearCart()
            onComplete(id)
        }
    }

    private fun updateTotals() {
        val items = _cartItems.value
        totalItems.value = items.sumOf { it.quantity }
        totalAmount.value = items.sumOf { item ->
            val basePrice = item.menuItem.price.replace("$", "").toDoubleOrNull() ?: 0.0
            val finalPrice = if (item.menuItem.discount > 0) {
                basePrice * (1 - item.menuItem.discount / 100.0)
            } else {
                basePrice
            }
            finalPrice * item.quantity
        }
    }
}
