package com.mustafaderinoz.core.domain.ticket

import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.domain.event.TicketType

data class TicketUi(
    val ticket: Ticket,
    val event: Event?,
    val ticketType: TicketType?,
) {
    val eventName: String get() = event?.name ?: ""
    val eventVenue: String get() = event?.venue ?: ""
    val eventStartsAt: String get() = event?.startsAt ?: ""
    val ticketTypeName: String get() = ticketType?.name ?: ""
    val ticketTypePriceCents: Long get() = ticketType?.priceCents ?: 0L
    val id: String get() = ticket.id
    val qrCode: String get() = ticket.qrCode
    val status: String get() = ticket.status.name
}
