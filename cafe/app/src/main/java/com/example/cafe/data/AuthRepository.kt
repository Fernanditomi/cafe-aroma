package com.example.cafe.data

import android.content.Context
import com.example.cafe.data.remote.ApiClient
import com.example.cafe.data.remote.ApiErrorResponse
import com.example.cafe.data.remote.LoginRequest
import com.example.cafe.data.remote.RegisterRequest
import com.example.cafe.data.remote.UpdateProfileRequest
import com.example.cafe.data.remote.UserDto
import com.example.cafe.data.remote.toEntity
import com.example.cafe.utils.emailError
import com.example.cafe.utils.passwordError
import com.example.cafe.utils.sha256Hex
import com.example.cafe.utils.verifyStoredPassword
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

sealed class AuthResult {
    data class Success(val user: User, val offline: Boolean = false) : AuthResult()
    data class Failure(val message: String, val code: String = "ERROR") : AuthResult()
}

/**
 * Puente entre la app y la API (PostgreSQL en Docker).
 * - Conectado: login/registro/validacion de sesion contra el servidor.
 * - Sin conexion: valida contra la copia local (Room) y lo señala con offline = true.
 */
class AuthRepository(context: Context) {

    private val appContext = context.applicationContext
    private val api get() = ApiClient.authApi(appContext)
    private val session = SessionStore(appContext)
    private val userDao = AppDatabase.getDatabase(appContext).userDao()
    private val gson = Gson()

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val emailProblem = emailError(email)
        if (emailProblem != null) return@withContext AuthResult.Failure(emailProblem, "VALIDATION")
        if (password.isBlank()) {
            return@withContext AuthResult.Failure("La contraseña es obligatoria.", "VALIDATION")
        }

        try {
            val response = api.login(LoginRequest(email.trim(), password))
            val body = response.body()
            if (response.isSuccessful && body?.user != null) {
                AuthResult.Success(persist(body.user!!, body.token, sha256Hex(password)))
            } else {
                AuthResult.Failure(mapError(response), mapCode(response))
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: IOException) {
            offlineLogin(email, password)
        } catch (e: Exception) {
            AuthResult.Failure("No se pudo contactar al servidor. Intenta de nuevo.", "UNEXPECTED")
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val problem = emailError(email) ?: passwordError(password) ?: run {
            if (name.trim().length < 3) "Ingresa tu nombre completo." else null
        }
        if (problem != null) return@withContext AuthResult.Failure(problem, "VALIDATION")

        try {
            val response = api.register(
                RegisterRequest(name = name.trim(), email = email.trim(), phone = phone.trim(), password = password)
            )
            val body = response.body()
            if (response.isSuccessful && body?.user != null) {
                AuthResult.Success(persist(body.user!!, body.token, sha256Hex(password)))
            } else {
                AuthResult.Failure(mapError(response), mapCode(response))
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: IOException) {
            AuthResult.Failure(
                "No hay conexión con el servidor: tu cuenta se crea en la base de datos y necesitas internet para registrarte.",
                "OFFLINE"
            )
        } catch (e: Exception) {
            AuthResult.Failure("No se pudo completar el registro. Intenta de nuevo.", "UNEXPECTED")
        }
    }

    suspend fun restoreSession(): AuthResult.Success? = withContext(Dispatchers.IO) {
        val token = session.token
        val cached = session.cachedUser

        if (token.isNullOrBlank()) {
            return@withContext cached?.let { offlineSuccess(it) }
        }

        try {
            val response = api.me("Bearer $token")
            val user = response.body()?.user
            when {
                response.isSuccessful && user != null ->
                    AuthResult.Success(persist(user, token, ""))

                response.code() == 401 || response.code() == 403 -> {
                    session.clear()
                    null
                }

                else -> cached?.let { offlineSuccess(it) }
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            cached?.let { offlineSuccess(it) }
        }
    }

    suspend fun updateProfileBestEffort(user: User) = withContext(Dispatchers.IO) {
        val token = session.token
        if (token.isNullOrBlank()) return@withContext
        try {
            api.updateProfile(
                "Bearer $token",
                UpdateProfileRequest(
                    name = user.name,
                    phone = user.phone,
                    address = user.address,
                    avatarUrl = user.avatarUrl,
                    points = user.points,
                    highScore = user.highScore
                )
            )
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            // El servidor no esta disponible: los cambios quedan guardados localmente.
        }
    }

    suspend fun deleteAccount(user: User) = withContext(Dispatchers.IO) {
        val token = session.token
        if (!token.isNullOrBlank()) {
            try {
                api.deleteAccount("Bearer $token")
            } catch (c: CancellationException) {
                throw c
            } catch (e: Exception) {
                // Se omite: la cuenta local se elimina igualmente.
            }
        }
        userDao.deleteUser(user)
        session.clear()
    }

    suspend fun isServerReachable(): Boolean = withContext(Dispatchers.IO) {
        try {
            api.health().isSuccessful
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        session.clear()
    }

    private suspend fun offlineLogin(email: String, password: String): AuthResult {
        val local = userDao.getUserByEmail(email.trim())
        return if (local != null && verifyStoredPassword(password, local.password)) {
            session.cachedUser = local.toDto()
            AuthResult.Success(local, offline = true)
        } else {
            AuthResult.Failure(
                "Sin conexión al servidor y no existe una cuenta local para este correo.",
                "OFFLINE"
            )
        }
    }

    private suspend fun persist(dto: UserDto, token: String?, localPasswordHash: String): User {
        if (!token.isNullOrBlank()) session.token = token
        val existing = userDao.getUserByEmail(dto.email)
        val passwordField = when {
            localPasswordHash.isNotBlank() -> localPasswordHash
            else -> existing?.password ?: ""
        }
        val entity = dto.toEntity(passwordField)
        userDao.upsertUser(entity)
        session.cachedUser = dto
        return entity
    }

    private suspend fun offlineSuccess(dto: UserDto): AuthResult.Success? {
        val local = userDao.getUserByEmail(dto.email) ?: return null
        return AuthResult.Success(local, offline = true)
    }

    private fun User.toDto(): UserDto = UserDto(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = address,
        avatarUrl = avatarUrl,
        points = points,
        registrationDate = registrationDate,
        highScore = highScore,
        isAdmin = isAdmin,
        role = role
    )

    private fun mapError(response: Response<*>): String {
        val serverMessage = runCatching {
            response.errorBody()?.charStream()?.use { gson.fromJson(it, ApiErrorResponse::class.java) }
        }.getOrNull()?.error

        return when (response.code()) {
            400 -> serverMessage ?: "Revisa los datos ingresados."
            401 -> serverMessage ?: "Correo o contraseña incorrectos."
            409 -> serverMessage ?: "El correo ya está registrado."
            429 -> serverMessage ?: "Demasiados intentos. Espera unos minutos y vuelve a intentar."
            in 500..599 -> "El servidor tuvo un problema. Intenta más tarde."
            else -> serverMessage ?: "Error inesperado (${response.code()})."
        }
    }

    private fun mapCode(response: Response<*>): String {
        val code = runCatching {
            response.errorBody()?.charStream()?.use { gson.fromJson(it, ApiErrorResponse::class.java) }
        }.getOrNull()?.code
        return code ?: "HTTP_${response.code()}"
    }
}
