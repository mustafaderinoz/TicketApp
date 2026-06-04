package com.mustafaderinoz.data.util

import com.mustafaderinoz.data.mapper.toAppError
import com.mustafaderinoz.data.network.ApiException
import com.mustafaderinoz.data.network.NetworkException
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> runCatchingApi(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch(e: HttpException)
{
    Result.failure(ApiException(code = e.code(), errorMessage = e.message(), cause=e).toAppError())
} catch(e: IOException)
{
    Result.failure(NetworkException(e).toAppError())
} catch(e: Exception)
{
    Result.failure(e.toAppError())
}