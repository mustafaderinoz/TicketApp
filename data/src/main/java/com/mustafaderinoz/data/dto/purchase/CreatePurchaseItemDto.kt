package com.mustafaderinoz.data.dto.purchase

import kotlinx.serialization.Serializable

@Serializable
data class CreatePurchaseRequestDto(
    val items: List<PurchaseItemRequestDto>,
)