package com.mustafaderinoz.core.domain.purchase

data class PurchaseItem(
    val id: String,
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Long,
)