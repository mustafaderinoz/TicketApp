package com.mustafaderinoz.data.di

import com.mustafaderinoz.core.domain.auth.AuthRepository
import com.mustafaderinoz.core.domain.event.EventRepository
import com.mustafaderinoz.core.domain.purchase.PurchaseRepository
import com.mustafaderinoz.core.domain.ticket.TicketRepository
import com.mustafaderinoz.data.local.TokenStore
import com.mustafaderinoz.data.network.AuthInterceptor
import com.mustafaderinoz.data.network.TokenAuthenticator
import com.mustafaderinoz.data.remote.AuthApi
import com.mustafaderinoz.data.remote.EventApi
import com.mustafaderinoz.data.remote.PurchaseApi
import com.mustafaderinoz.data.remote.TicketApi
import com.mustafaderinoz.data.repository.AuthRepositoryImpl
import com.mustafaderinoz.data.repository.EventRepositoryImpl
import com.mustafaderinoz.data.repository.PurchaseRepositoryImpl
import com.mustafaderinoz.data.repository.TicketRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val BASE_URL = "https://tickets-api.halitkalayci.com/"
private val REFRESH_CLIENT = named("refresh_client")
private val REFRESH_RETROFIT = named("refresh_retrofit")
private val REFRESH_API = named("refresh_api")

val dataModule = module {
    // Scope (Kapsam)
    // 3 temel seçenek

    // Yaşam döngüsündeki bağımlılığın davranış biçimi

    // Single (Singleton) -> Uygulama yaşam döngüsü boyunca tek örnek.
    single {
        Json {
            ignoreUnknownKeys = true // Cevapta var olan ama classta olmayan alanları ignore et.
            explicitNulls = false
            isLenient = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    single {
        TokenStore(context=get())
    }
    single { AuthInterceptor(tokenStore = get()) }

    single {
        TokenAuthenticator(
            tokenStore = get(),
            refreshApiProvider = { get(REFRESH_API) }
            //refreshApiProvider = get(REFRESH_API)
        )
    }
    // Refresh Stack
    single(REFRESH_CLIENT) {
        OkHttpClient.Builder().addInterceptor(get<HttpLoggingInterceptor>()).build()
    }

    single(REFRESH_RETROFIT)
    {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get(REFRESH_CLIENT))
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single(REFRESH_API)
    {
        get<Retrofit>(REFRESH_RETROFIT).create(AuthApi::class.java)
    }
    // Refresh Stack


    // HTTP isteklerini yönetmek..
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single {
        get<Retrofit>().create(AuthApi::class.java)
    }
    single {
        get<Retrofit>().create(EventApi::class.java)
    }
    single {
        get<Retrofit>().create(TicketApi::class.java)
    }
    single {
        get<Retrofit>().create(PurchaseApi::class.java)
    }


    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            tokenStore = get()
        )
    }
    single<EventRepository> {
        EventRepositoryImpl(
           eventApi = get()
        )
    }
    single<TicketRepository> {
        TicketRepositoryImpl(ticketApi = get())
    }
    single<PurchaseRepository> {
        PurchaseRepositoryImpl(purchaseApi = get())
    }


    // factory -> Her çağırıldığı noktada yeni instance üretir. Her fonksiyon için birer örnek

    // scoped -> Class -> tüm fonksiyonlarına 1 örnek
}