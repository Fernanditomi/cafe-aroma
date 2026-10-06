package com.example.cafe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesForUser(userId: Int): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Delete
    suspend fun removeFavorite(favorite: Favorite)
    
    @Query("SELECT * FROM favorites WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getFavorite(userId: Int, productId: Int): Favorite?
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY date DESC")
    fun getNotifications(userId: Int): Flow<List<Notification>>

    @Insert
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCoupon(code: String): Coupon?

    @Insert
    suspend fun insertCoupon(coupon: Coupon)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY date DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<ActivityLog>>

    @Insert
    suspend fun insertLog(log: ActivityLog)
}
