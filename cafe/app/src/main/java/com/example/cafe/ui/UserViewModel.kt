package com.example.cafe.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafe.data.AppDatabase
import com.example.cafe.data.AuthRepository
import com.example.cafe.data.AuthResult
import com.example.cafe.data.ServerConfig
import com.example.cafe.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val serverConfig = ServerConfig(application)

    private val _serverUrl = MutableStateFlow(serverConfig.baseUrl)
    val serverUrl = _serverUrl.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /** Indica que la sesion guardada ya fue restaurada (o descartada). */
    private val _sessionReady = MutableStateFlow(false)
    val sessionReady = _sessionReady.asStateFlow()

    /** Aviso no bloqueante (modo sin conexion, sincronizacion, etc.). */
    private val _notice = MutableStateFlow<String?>(null)
    val notice = _notice.asStateFlow()

    /** true = API en linea, false = sin conexion, null = aun sin comprobar. */
    private val _serverReachable = MutableStateFlow<Boolean?>(null)
    val serverReachable = _serverReachable.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                val restored = repository.restoreSession()
                _currentUser.value = restored?.user
                _serverReachable.value = restored?.let { !it.offline }
                _notice.value = restored?.takeIf { it.offline }?.let {
                    "Sin conexión al servidor: la sesión se restauró desde este dispositivo."
                }
            } catch (e: Exception) {
                _currentUser.value = null
            } finally {
                _sessionReady.value = true
            }
        }
    }

    fun refreshServerStatus() {
        viewModelScope.launch {
            _serverReachable.value = repository.isServerReachable()
        }
    }

    /** Guarda la URL del servidor; devuelve null si quedó guardada o el error. */
    fun saveServerUrl(raw: String): String? {
        val problem = ServerConfig.validate(raw)
        if (problem != null) return problem
        serverConfig.baseUrl = ServerConfig.normalize(raw)
        _serverUrl.value = serverConfig.baseUrl
        _serverReachable.value = null
        _notice.value = "Servidor actualizado: comprobando conexión..."
        refreshServerStatus()
        return null
    }

    fun login(email: String, password: String, onSuccess: (Boolean) -> Unit) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _notice.value = null
            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.user
                    _serverReachable.value = !result.offline
                    _notice.value = if (result.offline) {
                        "Sin conexión: validamos tu cuenta localmente. Sincroniza cuando tengas internet."
                    } else {
                        null
                    }
                    onSuccess(result.user.isAdmin)
                }
                is AuthResult.Failure -> {
                    if (result.code == "OFFLINE") _serverReachable.value = false
                    if (result.code != "OFFLINE" && result.code != "VALIDATION") {
                        _serverReachable.value = true
                    }
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, phone: String, password: String, onSuccess: (Boolean) -> Unit) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _notice.value = null
            when (val result = repository.register(name, email, phone, password)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.user
                    _serverReachable.value = !result.offline
                    onSuccess(result.user.isAdmin)
                }
                is AuthResult.Failure -> {
                    if (result.code == "OFFLINE") _serverReachable.value = false
                    if (result.code != "OFFLINE" && result.code != "VALIDATION") {
                        _serverReachable.value = true
                    }
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun addPoints(amount: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(points = user.points + amount)
            userDao.updateUser(updatedUser)
            _currentUser.value = updatedUser
            repository.updateProfileBestEffort(updatedUser)
        }
    }

    fun updateProfile(name: String, phone: String, address: String, avatarUrl: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(name = name, phone = phone, address = address, avatarUrl = avatarUrl)
            userDao.updateUser(updatedUser)
            _currentUser.value = updatedUser
            repository.updateProfileBestEffort(updatedUser)
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _notice.value = null
        _error.value = null
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteAccount(user)
            _currentUser.value = null
            _notice.value = null
            onSuccess()
        }
    }

    fun clearError() {
        _error.value = null
        _notice.value = null
    }

    fun updateHighScore(newScore: Int) {
        val user = _currentUser.value ?: return
        if (newScore > user.highScore) {
            viewModelScope.launch {
                val updatedUser = user.copy(highScore = newScore)
                userDao.updateUser(updatedUser)
                _currentUser.value = updatedUser
                repository.updateProfileBestEffort(updatedUser)
            }
        }
    }
}
