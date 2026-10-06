package com.example.cafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val stock: Int = 50,
    val stockMin: Int = 10,
    val isPopular: Boolean = false,
    val isFeatured: Boolean = false,
    val isAvailable: Boolean = true,
    val discount: Int = 0,
    val rating: Float = 4.5f,
    val preparationTime: String = "15 min"
)
