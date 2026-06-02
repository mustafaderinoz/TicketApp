package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.domain.event.EventRepository
import com.mustafaderinoz.data.network.ApiException
import com.mustafaderinoz.data.network.NetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val event: Event? = null,
    val error: String? = null,
    val quantities: Map<String, Int> = emptyMap(),
)

class EventDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            eventRepository.getEvent(eventId)
                .onSuccess { event ->
                    val initialQuantities = event.ticketTypes.associate { it.id to 0 }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            event = event,
                            quantities = initialQuantities
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.toEventDetailMessage()
                        )
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }

            eventRepository.getEvent(eventId)
                .onSuccess { event ->
                    val currentQuantities = _state.value.quantities
                    val mergedQuantities = event.ticketTypes.associate { ticketType ->
                        val existing = currentQuantities[ticketType.id] ?: 0
                        ticketType.id to minOf(existing.toLong(), ticketType.remaining).toInt()
                    }
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            event = event,
                            quantities = mergedQuantities
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            error = error.toEventDetailMessage()
                        )
                    }
                }
        }
    }

    fun increment(ticketTypeId: String) {
        val event = _state.value.event ?: return
        val ticketType = event.ticketTypes.find { it.id == ticketTypeId } ?: return
        val current = _state.value.quantities[ticketTypeId] ?: 0
        if (current >= ticketType.remaining) return
        _state.update { it.copy(quantities = it.quantities + (ticketTypeId to current + 1)) }
    }

    fun decrement(ticketTypeId: String) {
        val current = _state.value.quantities[ticketTypeId] ?: 0
        if (current <= 0) return
        _state.update { it.copy(quantities = it.quantities + (ticketTypeId to current - 1)) }
    }

    internal fun Throwable.toEventDetailMessage(): String = when (this) {
        is ApiException -> when (code) {
            404 -> "Etkinlik bulunamadı"
            in 500..599 -> "Sunucu şu anda cevap veremiyor"
            else -> "Beklenmeyen bir hata oluştu"
        }
        is NetworkException -> "İnternet bağlantısı yok"
        else -> message ?: "Bilinmeyen bir hata oluştu."
    }
}