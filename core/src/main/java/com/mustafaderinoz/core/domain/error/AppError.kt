package com.mustafaderinoz.core.domain.error

sealed class AppError : Exception() {
    data class Api(val code: Int, override val message: String?) : AppError()
    data class Network(override val message: String? = "İnternet bağlantısı yok") : AppError()
    data class Unknown(override val message: String? = null) : AppError()
}