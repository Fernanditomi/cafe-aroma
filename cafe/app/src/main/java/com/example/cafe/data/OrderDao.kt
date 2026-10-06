package com.example.cafe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerName = :name ORDER BY date DESC")
    fun getOrdersByCustomer(name: String): Flow<List<Order>>

    @Insert
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT SUM(totalAmount) FROM orders WHERE status != 'Cancelado'")
    suspend fun getTotalSales(): Double?

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int
}
