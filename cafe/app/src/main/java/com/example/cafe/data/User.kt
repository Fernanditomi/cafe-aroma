package com.example.cafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val points: Int = 0,
    val registrationDate: Long = System.currentTimeMillis(),
    val highScore: Int = 0,
    val isAdmin: Boolean = false,
    val role: String = "CLIENTE" // ADMIN, KITCHEN, CASHIER, INVENTORY, CLIENTE
)
