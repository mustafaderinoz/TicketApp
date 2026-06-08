package com.mustafaderinoz.data.repository

import com.mustafaderinoz.core.domain.checkin.CheckInRepository
import com.mustafaderinoz.core.domain.checkin.ScanResult
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.data.dto.checkin.ScanRequestDto
import com.mustafaderinoz.data.mapper.toDomain
import com.mustafaderinoz.data.remote.CheckInApi
import com.mustafaderinoz.data.util.runCatchingApi

internal class CheckInRepositoryImpl(
    private val checkInApi: CheckInApi
) : CheckInRepository {
    override suspend fun scanQr(qrCode: String): Result<ScanResult> =
        runCatchingApi {
            checkInApi.scan(ScanRequestDto(qrCode))
        }.map { it.toDomain() }

    override suspend fun getAssignedEvents(): Result<List<Event>> =
        runCatchingApi {
            checkInApi.getAssignedEvents()
        }.map { list -> list.map { it.toDomain() } }
}