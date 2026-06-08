package com.mustafaderinoz.core.domain.auth

import kotlinx.coroutines.flow.Flow

// Soyut Sözleşme: ne yapılacağını belirtir, nasıl yapılacağını değil.
interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val userRole: Flow<UserRole?>

    suspend fun login(email : String, password: String): Result<AuthSession>
    suspend fun register(email : String, password: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
}