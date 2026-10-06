package com.example.cafe.utils

import java.security.MessageDigest

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[a-zA-Z]{2,}$")

fun sha256Hex(value: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    val builder = StringBuilder(bytes.size * 2)
    for (byte in bytes) {
        val b = byte.toInt()
        builder.append(Character.forDigit((b shr 4) and 0xF, 16))
        builder.append(Character.forDigit(b and 0xF, 16))
    }
    return builder.toString()
}

fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

fun emailError(email: String): String? = when {
    email.isBlank() -> "El correo electrónico es obligatorio."
    !isValidEmail(email) -> "Ingresa un correo válido, por ejemplo: nombre@correo.com"
    else -> null
}

fun passwordError(password: String): String? = when {
    password.isBlank() -> "La contraseña es obligatoria."
    password.length < 8 -> "Debe tener al menos 8 caracteres."
    password.length > 128 -> "La contraseña es demasiado larga."
    !password.any { it.isLetter() } || !password.any { it.isDigit() } ->
        "Debe incluir letras y números."
    else -> null
}

fun passwordStrength(password: String): Int = when {
    password.length >= 12 && password.any { !it.isLetterOrDigit() } -> 4
    password.length >= 10 -> 3
    password.length >= 8 -> 2
    password.isNotEmpty() -> 1
    else -> 0
}

fun strengthLabel(strength: Int): String = when (strength) {
    1 -> "Débil"
    2 -> "Aceptable"
    3 -> "Fuerte"
    4 -> "Excelente"
    else -> ""
}

/**
 * Compara la contraseña ingresada con la copia local:
 * - hash SHA-256 guardado durante el ultimo login en linea
 * - texto plano de cuentas creadas antes (compatibilidad)
 */
fun verifyStoredPassword(plain: String, stored: String): Boolean {
    if (stored.isBlank()) return false
    return stored == sha256Hex(plain) || stored == plain
}
