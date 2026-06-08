package com.mustafaderinoz.core.domain.ticket

data class Ticket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val ticketTypeId: String,
    val ticketTypeName: String,
    val ticketTypePriceCents: Long,
    val eventId: String,
    val eventName: String,
    val eventVenue: String,
    val eventStartsAt: String,
)