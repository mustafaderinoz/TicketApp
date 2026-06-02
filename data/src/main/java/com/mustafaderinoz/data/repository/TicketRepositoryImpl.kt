package com.mustafaderinoz.data.repository

import com.mustafaderinoz.core.domain.ticket.TicketRepository
import com.mustafaderinoz.core.domain.ticket.PurchasedTicket
import com.mustafaderinoz.data.mapper.toDomain
import com.mustafaderinoz.data.remote.TicketApi
import com.mustafaderinoz.data.util.runCatchingApi

internal class TicketRepositoryImpl(
    private val ticketApi: TicketApi
) : TicketRepository {

    override suspend fun getPurchasedTickets(): Result<List<PurchasedTicket>> =
        runCatchingApi {
            ticketApi.getPurchasedTickets()
        }.map { list -> list.map { it.toDomain() } }

    override suspend fun getTicketById(id: String): Result<PurchasedTicket> =
        runCatchingApi {
            ticketApi.getTicketById(id)
        }.map { it.toDomain() }
}