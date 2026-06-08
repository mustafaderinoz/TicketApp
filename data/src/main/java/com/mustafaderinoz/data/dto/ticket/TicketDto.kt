package com.mustafaderinoz.data.dto.ticket
import kotlinx.serialization.Serializable

@Serializable
data class TicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String? = null,
    val checkedInBy: String? = null,
    val ticketType: TicketTypeWithEventDto,
)

@Serializable
data class TicketTypeWithEventDto(
    val id: String,
    val name: String,
    val priceCents: Long,
    val event: TicketEventDto,
)

@Serializable
data class TicketEventDto(
    val id: String,
    val name: String,
    val place: String,
    val startsAt: String,
)