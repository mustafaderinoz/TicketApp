package com.mustafaderinoz.data.dto.ticket
import kotlinx.serialization.Serializable

@Serializable
data class TicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val ticketTypeId: String,
)