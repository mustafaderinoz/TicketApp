package com.mustafaderinoz.core.domain.ticket

data class Ticket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val ticketTypeId: String,
)