package com.mustafaderinoz.core.domain.ticket

interface TicketRepository {
    suspend fun getPurchasedTickets(): Result<List<Ticket>>
    suspend fun getTicketById(id: String): Result<Ticket>
}