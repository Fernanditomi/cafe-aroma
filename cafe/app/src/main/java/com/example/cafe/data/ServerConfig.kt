package com.example.cafe.data

import android.content.Context
import android.net.Uri

/**
 * URL base de la API, configurable desde Ajustes para que la app
 * funcione en cualquier celular sin reinstalar (Render, servidor local, etc.).
 */
class ServerConfig(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("cafe_api", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_URL, null)?.takeIf { it.isNotBlank() } ?: BuildConfigDefault
        set(value) {
            prefs.edit().putString(KEY_URL, normalize(value)).apply()
        }

    companion object {
        private const val KEY_URL = "base_url"

        // El valor por defecto viene de BuildConfig (Render en distribucion).
        val BuildConfigDefault: String
            get() = com.example.cafe.BuildConfig.API_BASE_URL

        fun normalize(raw: String): String {
            var url = raw.trim()
            if (url.isEmpty()) return BuildConfigDefault
            if (!url.contains("://")) {
                val host = url.substringBefore("/").substringBefore(":")
                val local = host == "localhost" || host == "127.0.0.1" ||
                    host.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))
                url = if (local) "http://$url" else "https://$url"
            }
            if (!url.endsWith("/")) url += "/"
            if (!url.endsWith("/api/")) url += "api/"
            return url
        }

        /** Devuelve null si la URL es válida o un mensaje de error en caso contrario. */
        fun validate(raw: String): String? {
            val url = normalize(raw)
            val uri = runCatching { Uri.parse(url) }.getOrNull()
                ?: return "La URL no es válida."
            val scheme = uri.scheme?.lowercase()
            if (scheme != "http" && scheme != "https") return "Debe empezar con http:// o https://"
            if (uri.host.isNullOrBlank()) return "Falta el dominio o la IP del servidor."
            return null
        }
    }
}
