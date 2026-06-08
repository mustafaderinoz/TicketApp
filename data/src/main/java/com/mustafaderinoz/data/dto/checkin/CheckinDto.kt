package com.mustafaderinoz.data.dto.checkin

import com.mustafaderinoz.data.dto.event.EventDto
import kotlinx.serialization.Serializable

@Serializable
data class ScanRequestDto(val qrCode: String)

@Serializable
data class ScanResponseDto(
    val ticketId: String,
    val ticketType: String,
    val event: EventDto,
    val checkedInAt: String
)