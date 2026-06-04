package com.mustafaderinoz.data.remote

import com.mustafaderinoz.data.dto.ticket.TicketDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TicketApi {
    @GET("/me/tickets")       // Endpoint'i backend'e göre güncelle
    suspend fun getPurchasedTickets(): List<TicketDto>
    @GET("me/tickets/{id}")
    suspend fun getTicketById(@Path("id") id: String): TicketDto
}