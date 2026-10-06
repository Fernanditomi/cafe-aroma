package com.example.cafe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopConfigDao {
    @Query("SELECT * FROM shop_config WHERE id = 1")
    fun getConfig(): Flow<ShopConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ShopConfig)
}
