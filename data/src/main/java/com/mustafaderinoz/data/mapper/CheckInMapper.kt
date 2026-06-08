package com.mustafaderinoz.data.mapper

import com.mustafaderinoz.core.domain.checkin.ScanResult
import com.mustafaderinoz.data.dto.checkin.ScanResponseDto


internal fun ScanResponseDto.toDomain() = ScanResult(
    ticketId = ticketId,
    ticketType = ticketType,
    event = event.toDomain(),
    checkedInAt = checkedInAt
)