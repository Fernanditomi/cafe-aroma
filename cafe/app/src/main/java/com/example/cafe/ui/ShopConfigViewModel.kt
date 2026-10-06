package com.example.cafe.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafe.data.AppDatabase
import com.example.cafe.data.ShopConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val shopConfigDao = AppDatabase.getDatabase(application).shopConfigDao()

    val config = shopConfigDao.getConfig().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShopConfig(name = "Café Aroma", address = "Av. Esencia #123", phone = "+1 234 567 890")
    )

    fun saveConfig(name: String, address: String, phone: String) {
        viewModelScope.launch {
            shopConfigDao.saveConfig(ShopConfig(name = name, address = address, phone = phone))
        }
    }
}
