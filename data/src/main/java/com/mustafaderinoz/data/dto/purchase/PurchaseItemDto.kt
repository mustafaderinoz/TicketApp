package com.mustafaderinoz.data.dto.purchase

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseItemDto(
    val id: String,
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Long,
)