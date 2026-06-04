package com.mustafaderinoz.data.mapper

import com.mustafaderinoz.core.domain.error.AppError
import com.mustafaderinoz.data.network.ApiException
import com.mustafaderinoz.data.network.NetworkException

fun Throwable.toAppError(): AppError = when (this) {
    is ApiException -> AppError.Api(code, message)
    is NetworkException -> AppError.Network(message)
    else                -> AppError.Unknown(message)
}