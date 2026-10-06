package com.example.cafe.data

import android.content.Context
import com.example.cafe.data.remote.UserDto
import com.google.gson.Gson

/**
 * Guarda el token JWT y el ultimo usuario conocido en SharedPreferences
 * para restaurar la sesion y poder validar el login sin conexion.
 */
class SessionStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("cafe_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var cachedUser: UserDto?
        get() = prefs.getString(KEY_USER, null)?.let { json ->
            runCatching { gson.fromJson(json, UserDto::class.java) }.getOrNull()
        }
        set(value) {
            prefs.edit().putString(KEY_USER, value?.let { gson.toJson(it) }).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "cached_user"
    }
}
