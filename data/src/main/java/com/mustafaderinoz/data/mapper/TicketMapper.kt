package com.mustafaderinoz.data.mapper


import com.mustafaderinoz.core.domain.ticket.Ticket
import com.mustafaderinoz.core.domain.ticket.TicketStatus
import com.mustafaderinoz.data.dto.ticket.TicketDto

// EventDto.toDomain() ile aynı mantık:
// API alanı değişirse sadece burayı düzeltiyoruz, core'a dokunmuyoruz.
internal fun TicketDto.toDomain(): Ticket = Ticket(
    id = id,
    qrCode = qrCode,
    status = runCatching { TicketStatus.valueOf(status) }.getOrDefault(TicketStatus.VALID),
    ticketTypeId = ticketTypeId,
)