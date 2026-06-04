package com.mustafaderinoz.data.remote
import com.mustafaderinoz.data.dto.purchase.CreatePurchaseRequestDto
import com.mustafaderinoz.data.dto.purchase.PurchaseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PurchaseApi {
    @POST("/purchases")
    suspend fun createPurchase(@Body body: CreatePurchaseRequestDto): PurchaseDto

    @POST("/purchases/{id}/pay")
    suspend fun pay(@Path("id") purchaseId: String): PurchaseDto

    @GET("/purchases/{id}")
    suspend fun getPurchase(@Path("id") purchaseId: String): PurchaseDto
}