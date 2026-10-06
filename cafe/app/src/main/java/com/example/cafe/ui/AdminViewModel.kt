package com.example.cafe.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafe.data.AppDatabase
import com.example.cafe.data.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val orderDao = AppDatabase.getDatabase(application).orderDao()
    private val userDao = AppDatabase.getDatabase(application).userDao()

    val orders = orderDao.getAllOrders().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalSales = orders.map { list ->
        list.filter { it.status != "Cancelado" }.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOrders = orders.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers = _totalUsers.asStateFlow()

    init {
        refreshUsersCount()
    }

    fun refreshUsersCount() {
        viewModelScope.launch {
            _totalUsers.value = userDao.getUserCount()
        }
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            orderDao.updateOrder(order.copy(status = newStatus))
        }
    }
}
