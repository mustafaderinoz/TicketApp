package com.mustafaderinoz.data.repository

import com.mustafaderinoz.core.domain.auth.AuthRepository
import com.mustafaderinoz.core.domain.auth.AuthSession
import com.mustafaderinoz.core.domain.auth.User
import com.mustafaderinoz.core.domain.auth.UserRole
import com.mustafaderinoz.data.dto.auth.CredentialsDto
import com.mustafaderinoz.data.local.TokenStore
import com.mustafaderinoz.data.remote.AuthApi
import com.mustafaderinoz.data.util.runCatchingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = tokenStore.accessToken.map { it != null }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.login(CredentialsDto(email=email, password=password))
    }.onSuccess {
        tokenStore.save(it.accessToken, it.refreshToken)
        // jwt'i bi yere yaz..
    }
        .map {
                i -> AuthSession(
            user = User(
                i.user.id, i.user.email, UserRole.fromApi(i.user.role),
            ),
            accessToken = i.accessToken,
            refreshToken = i.refreshToken)
        }


    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.register(CredentialsDto(email = email, password = password))
    }.onSuccess {
        tokenStore.save(it.accessToken, it.refreshToken)
    }.map { i ->
        AuthSession(
            user = User(i.user.id, i.user.email, UserRole.fromApi(i.user.role)),
            accessToken = i.accessToken,
            refreshToken = i.refreshToken
        )
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        tokenStore.clear()
    }
}