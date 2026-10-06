package com.example.cafe.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): Response<UserResponse>

    @PATCH("users/me")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body body: UpdateProfileRequest
    ): Response<UserResponse>

    @DELETE("users/me")
    suspend fun deleteAccount(@Header("Authorization") authorization: String): Response<DeleteResponse>

    @GET("health")
    suspend fun health(): Response<HealthResponse>
}
