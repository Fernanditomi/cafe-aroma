package com.example.cafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val message: String,
    val date: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String // INFO, ORDER, PROMO
)

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val discountPercent: Int,
    val minPurchase: Double = 0.0,
    val expiryDate: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val action: String,
    val date: Long = System.currentTimeMillis(),
    val details: String
)
