package com.example.cafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_config")
data class ShopConfig(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val address: String,
    val phone: String
)
