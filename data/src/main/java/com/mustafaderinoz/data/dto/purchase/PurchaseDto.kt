package com.mustafaderinoz.data.dto.purchase

import com.mustafaderinoz.data.dto.ticket.TicketDto
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseDto(
    val id: String,
    val status: String,           // "PENDING" | "PAID"
    val totalCents: Long,
    val paidAt: String? = null,
    val items: List<PurchaseItemDto> = emptyList(),
    val tickets: List<TicketDto> = emptyList(),
)