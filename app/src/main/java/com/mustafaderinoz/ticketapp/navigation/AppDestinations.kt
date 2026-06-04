package com.mustafaderinoz.ticketapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Register

@Serializable
object Home

@Serializable
data class TicketDetail(val ticketId: String)

@Serializable
data class EventDetail(val id: String)
