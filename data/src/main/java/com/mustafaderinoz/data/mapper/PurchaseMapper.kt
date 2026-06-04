package com.mustafaderinoz.data.mapper

import com.mustafaderinoz.core.domain.purchase.Purchase
import com.mustafaderinoz.core.domain.purchase.PurchaseItem
import com.mustafaderinoz.core.domain.purchase.PurchaseStatus
import com.mustafaderinoz.data.dto.purchase.PurchaseDto
import com.mustafaderinoz.data.dto.purchase.PurchaseItemDto


internal fun PurchaseDto.toDomain(): Purchase = Purchase(
    id = id,
    status = runCatching { PurchaseStatus.valueOf(status) }.getOrDefault(PurchaseStatus.PENDING),
    totalCents = totalCents,
    paidAt = paidAt,
    items = items.map { it.toDomain() },
    tickets = tickets.map { it.toDomain() },
)

internal fun PurchaseItemDto.toDomain(): PurchaseItem = PurchaseItem(
    id = id,
    ticketTypeId = ticketTypeId,
    quantity = quantity,
    unitPriceCents = unitPriceCents,
)

