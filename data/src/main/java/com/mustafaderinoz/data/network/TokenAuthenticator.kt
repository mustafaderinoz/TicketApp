package com.mustafaderinoz.data.network

import com.mustafaderinoz.data.dto.auth.RefreshRequestDto
import com.mustafaderinoz.data.local.TokenStore
import com.mustafaderinoz.data.remote.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// Sadece HTTP 401'lerde çalış, Refresh akışı sürdür.
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApiProvider: () -> AuthApi,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // İsteğin tekrar tekrar buraya düşmesi -> refresh olsa bile 401 gelebilir
        if(response.priorResponseCount() >= 1) return null
        // İstek 401'e düştüğü anda, eğer sistemde jwt-refresh pairi tanımlıysa git refresh ile yeni jwt alıp isteği tekrar dene.
        val refreshToken = tokenStore.refreshTokenBlocking() ?: return null;

        //... bu token ile jwt yenilemeye çalış..
        return synchronized(this) // lock
        {
            // Bu blokta birden fazla istek aynı anda 401 alırsa kuyruğa girer..
            // Blok bitene kadar, yeni yapıları beklet..
            val current = tokenStore.accessTokenBlocking()
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // Başkası tarafından token değişmise, onu kullan ve devam et..
            if(current != null && current != sentToken){
                return@synchronized response.request.signWith(current)
            }

            val newPair = runCatching {
                runBlocking { refreshApiProvider().refresh(RefreshRequestDto(refreshToken)) }
            }.getOrNull()

            if(newPair==null){
                // Refresh başarısız
                tokenStore.clearBlocking()
                return@synchronized null
            }
            val currentRole = tokenStore.userRoleBlocking() ?: "USER"
            tokenStore.saveBlocking(newPair.accessToken, newPair.refreshToken, currentRole)

            response.request.signWith(newPair.accessToken)
        }

    }
    private fun Request.signWith(accessToken: String): Request = newBuilder().header("Authorization", "Bearer $accessToken").build()
    private fun Response.priorResponseCount() : Int{
        var count = 0
        var prior = priorResponse
        while(prior != null)
        {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}