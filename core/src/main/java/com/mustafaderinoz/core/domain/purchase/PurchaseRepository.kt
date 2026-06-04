package com.mustafaderinoz.core.domain.purchase

interface PurchaseRepository {
    /** Yeni satın alma oluşturur; başarıda Purchase döner. */
    suspend fun createPurchase(items: List<CreatePurchaseItem>): Result<Purchase>

    /** Mock ödeme; başarıda güncel Purchase döner. */
    suspend fun pay(purchaseId: String): Result<Purchase>

    /** Satın alma detayı. */
    suspend fun getPurchase(purchaseId: String): Result<Purchase>
}

data class CreatePurchaseItem(
    val ticketTypeId: String,
    val quantity: Int,
)