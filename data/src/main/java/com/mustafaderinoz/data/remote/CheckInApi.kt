package com.mustafaderinoz.data.remote

import com.mustafaderinoz.data.dto.checkin.ScanRequestDto
import com.mustafaderinoz.data.dto.checkin.ScanResponseDto
import com.mustafaderinoz.data.dto.event.EventDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheckInApi {
    @POST("/checkin/scan")
    suspend fun scan(@Body body: ScanRequestDto): ScanResponseDto

    @GET("/checkin/events")
    suspend fun getAssignedEvents(): List<EventDto>
}