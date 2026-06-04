package com.mustafaderinoz.core.domain.purchase

import com.mustafaderinoz.core.domain.ticket.Ticket

data class Purchase(
    val id: String,
    val status: PurchaseStatus,
    val totalCents: Long,
    val paidAt: String?,
    val items: List<PurchaseItem>,
    val tickets: List<Ticket>,
)