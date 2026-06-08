package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.auth.AuthRepository
import com.mustafaderinoz.core.domain.error.AppError
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.domain.event.EventRepository
import com.mustafaderinoz.core.domain.ticket.Ticket
import com.mustafaderinoz.core.domain.ticket.TicketRepository
import com.mustafaderinoz.core.util.ErrorContext
import com.mustafaderinoz.core.util.toUserMessage
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
    val tickets: List<Ticket> = emptyList(),
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

    init { loadData() }

    fun loadData(isRefresh: Boolean = false) {
        if (_state.value.isEventsLoading || _state.value.isRefreshing) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isEventsLoading = !isRefresh,
                    isTicketsLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    eventsError = null,
                    ticketsError = null
                )
            }

            val eventsDeferred = async { eventRepository.getEvents() }
            val ticketsDeferred = async { ticketRepository.getPurchasedTickets() }

            val eventsResult = eventsDeferred.await()
            val ticketsResult = ticketsDeferred.await()

            var fetchedEvents: List<Event> = emptyList()
            var newEventsError: String? = null
            var fetchedTickets: List<Ticket> = emptyList()
            var newTicketsError: String? = null

            eventsResult
                .onSuccess { fetchedEvents = it }
                .onFailure { error ->
                    newEventsError = (error as? AppError)?.toUserMessage(ErrorContext.HOME)
                        ?: AppError.Unknown(error.message).toUserMessage()
                }

            ticketsResult
                .onSuccess { fetchedTickets = it }
                .onFailure { error ->
                    newTicketsError = (error as? AppError)?.toUserMessage(ErrorContext.HOME)
                        ?: AppError.Unknown(error.message).toUserMessage()
                }

            _state.update {
                it.copy(
                    isEventsLoading = false,
                    events = fetchedEvents,
                    eventsError = newEventsError,
                    isTicketsLoading = false,
                    tickets = fetchedTickets,
                    ticketsError = newTicketsError,
                    isRefreshing = false
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

