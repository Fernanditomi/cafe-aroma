package com.example.cafe.data.remote

import android.content.Context
import com.example.cafe.data.ServerConfig
import com.example.cafe.data.SessionStore
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    private var cachedApi: AuthApi? = null

    @Volatile
    private var cachedUrl: String? = null

    private val gson = GsonBuilder().create()

    /**
     * Devuelve el cliente de la URL configurada actualmente.
     * Si el usuario cambia el servidor en Ajustes se reconstruye solo.
     */
    fun authApi(context: Context): AuthApi {
        val appContext = context.applicationContext
        val url = ServerConfig(appContext).baseUrl
        cachedApi?.let { api -> if (cachedUrl == url) return api }
        return synchronized(this) {
            cachedApi?.let { api -> if (cachedUrl == url) return@synchronized api }
            build(appContext, url).also {
                cachedApi = it
                cachedUrl = url
            }
        }
    }

    private fun build(context: Context, baseUrl: String): AuthApi {
        val session = SessionStore(context)

        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val token = session.token
                val request = if (token.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApi::class.java)
    }
}
