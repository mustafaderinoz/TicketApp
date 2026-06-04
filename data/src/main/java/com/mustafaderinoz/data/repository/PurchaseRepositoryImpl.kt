package com.mustafaderinoz.data.repository

import com.mustafaderinoz.core.domain.purchase.CreatePurchaseItem
import com.mustafaderinoz.core.domain.purchase.Purchase
import com.mustafaderinoz.core.domain.purchase.PurchaseRepository
import com.mustafaderinoz.data.dto.purchase.CreatePurchaseRequestDto
import com.mustafaderinoz.data.dto.purchase.PurchaseItemRequestDto
import com.mustafaderinoz.data.mapper.toDomain
import com.mustafaderinoz.data.remote.PurchaseApi
import com.mustafaderinoz.data.util.runCatchingApi

class PurchaseRepositoryImpl(
    private val purchaseApi: PurchaseApi,
) : PurchaseRepository {

    override suspend fun createPurchase(items: List<CreatePurchaseItem>): Result<Purchase> =
        runCatchingApi {
            purchaseApi.createPurchase(
                CreatePurchaseRequestDto(
                    items = items.map { PurchaseItemRequestDto(it.ticketTypeId, it.quantity) }
                )
            )
        }.map { it.toDomain() }

    override suspend fun pay(purchaseId: String): Result<Purchase> =
        runCatchingApi { purchaseApi.pay(purchaseId) }.map { it.toDomain() }

    override suspend fun getPurchase(purchaseId: String): Result<Purchase> =
        runCatchingApi { purchaseApi.getPurchase(purchaseId) }.map { it.toDomain() }
}