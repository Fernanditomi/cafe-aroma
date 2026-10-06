package com.example.cafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val userId: Int = 0,
    val customerName: String,
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val status: String = "NUEVO", // NUEVO, CONFIRMADO, PREPARANDO, LISTO, ENTREGADO, CANCELADO
    val itemsSummary: String,
    val paymentMethod: String = "Efectivo",
    val deliveryMethod: String = "Local", // Local, Delivery
    val deliveryAddress: String = "",
    val estimatedTime: String = "20 min"
)
