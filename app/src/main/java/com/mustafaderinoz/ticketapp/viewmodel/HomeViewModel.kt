package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.auth.AuthRepository
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.domain.event.EventRepository
import com.mustafaderinoz.core.domain.ticket.TicketUi
import com.mustafaderinoz.core.domain.ticket.TicketRepository
import com.mustafaderinoz.data.network.ApiException
import com.mustafaderinoz.data.network.NetworkException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isEventsLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val eventsError: String? = null,

    val isTicketsLoading: Boolean = false,
    val tickets: List<TicketUi> = emptyList(),
    val ticketsError: String? = null,

    val isRefreshing: Boolean = false
)

class HomeViewModel(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isEventsLoading = !isRefresh,
                    isTicketsLoading = !isRefresh,
                    isRefreshing =isRefresh,
                    eventsError = null,
                    ticketsError = null
                )
            }


            val eventsDeferred = async { eventRepository.getEvents() }
            val ticketsDeferred = async { ticketRepository.getPurchasedTickets() }


            val eventsResult = eventsDeferred.await()
            val ticketsResult = ticketsDeferred.await()

            // State'e aktarılacak geçici değişkenler
            var fetchedEvents: List<Event> = emptyList()
            var newEventsError: String? = null

            var enrichedTickets: List<TicketUi> = emptyList()
            var newTicketsError: String? = null

            // Etkinliklerin Sonucunu İşle
            eventsResult
                .onSuccess { fetchedEvents = it }
                .onFailure { newEventsError = it.toHomeMessage() }

            // Biletlerin Sonucunu İşle ve Zenginleştir (Enrich)
            ticketsResult
                .onSuccess { rawTickets ->
                    // Etkinlikler başarıyla yüklendiyse (veya boşsa) map'leri oluştur
                    val ticketTypeToEventMap = fetchedEvents
                        .flatMap { event -> event.ticketTypes.map { it.id to event } }
                        .toMap()

                    val ticketTypeMap = fetchedEvents
                        .flatMap { it.ticketTypes }
                        .associateBy { it.id }

                    //  UI modeline çevirme
                    enrichedTickets = rawTickets.map { ticket ->
                        TicketUi(
                            ticket = ticket,
                            event = ticketTypeToEventMap[ticket.ticketTypeId],
                            ticketType = ticketTypeMap[ticket.ticketTypeId],
                        )
                    }
                }
                .onFailure { newTicketsError = it.toHomeMessage() }

            // Tek Seferde Tüm State'i Güncelle
            _state.update {
                it.copy(
                    isEventsLoading = false,
                    events = fetchedEvents,
                    eventsError = newEventsError,

                    isTicketsLoading = false,
                    tickets = enrichedTickets,
                    ticketsError = newTicketsError,
                    isRefreshing = false
                )
            }
        }
    }
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

internal fun Throwable.toHomeMessage(): String = when (this) {
    is ApiException -> when (code) {
        in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> "Beklenmeyen bir hata oluştu"
    }
    is NetworkException -> "İnternet bağlantısı yok"
    else -> message ?: "Bilinmeyen bir hata oluştu."
}
