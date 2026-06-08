package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.error.AppError
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

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val ticket: Ticket? = null,
    val error: String? = null,
)

class TicketDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val ticketRepository: TicketRepository,
) : ViewModel() {

    private val ticketId: String = checkNotNull(savedStateHandle["ticketId"])
    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    init { loadTicketDetail() }

    fun loadTicketDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            ticketRepository.getTicketById(ticketId)
                .onSuccess { ticket ->
                    _state.update { it.copy(isLoading = false, ticket = ticket) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = (error as? AppError)?.toUserMessage(ErrorContext.TICKET_DETAIL)
                                ?: AppError.Unknown(error.message).toUserMessage()
                        )
                    }
                }
        }
    }
}


