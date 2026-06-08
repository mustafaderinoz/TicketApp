package com.mustafaderinoz.core.domain.checkin
import com.mustafaderinoz.core.domain.event.Event

interface CheckInRepository {
    suspend fun scanQr(qrCode: String): Result<ScanResult>
    suspend fun getAssignedEvents(): Result<List<Event>>
}