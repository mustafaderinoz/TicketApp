package com.mustafaderinoz.core.domain.checkin
import com.mustafaderinoz.core.domain.event.Event

data class ScanResult(
    val ticketId: String,
    val ticketType: String,
    val event: Event,
    val checkedInAt: String
)

