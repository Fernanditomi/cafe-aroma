package com.example.cafe.data.remote

import com.example.cafe.data.User

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)

data class RegisterRequest(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = ""
)

data class UserDto(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val points: Int = 0,
    val registrationDate: Long = 0L,
    val highScore: Int = 0,
    val isAdmin: Boolean = false,
    val role: String = "CLIENTE"
)

data class AuthResponse(
    val token: String = "",
    val user: UserDto? = null
)

data class UserResponse(
    val user: UserDto? = null
)

data class DeleteResponse(
    val deleted: Boolean = false
)

data class HealthResponse(
    val status: String = "",
    val database: String = ""
)

data class ApiErrorResponse(
    val error: String? = null,
    val code: String? = null
)

data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val avatarUrl: String? = null,
    val points: Int? = null,
    val highScore: Int? = null
)

fun UserDto.toEntity(localPassword: String = ""): User = User(
    id = id,
    name = name,
    email = email,
    password = localPassword,
    phone = phone,
    address = address,
    avatarUrl = avatarUrl,
    points = points,
    registrationDate = registrationDate.takeIf { it > 0L } ?: System.currentTimeMillis(),
    highScore = highScore,
    isAdmin = isAdmin,
    role = role
)
