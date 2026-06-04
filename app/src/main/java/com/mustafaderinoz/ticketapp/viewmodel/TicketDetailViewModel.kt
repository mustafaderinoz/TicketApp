package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val ticket: TicketUi? = null,
    val error: String? = null,
)

class TicketDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val ticketId: String = checkNotNull(savedStateHandle["ticketId"])
    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    init {
        loadTicketDetail()
    }

    fun loadTicketDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val ticketDeferred = async { ticketRepository.getTicketById(ticketId) }
            val eventsDeferred = async { eventRepository.getEvents() }

            val ticketResult = ticketDeferred.await()
            val eventsResult = eventsDeferred.await()

            // State'e aktarılacak geçici değişkenler
            var enrichedTicket: TicketUi? = null
            var detailError: String? = null

            // Önce biletin başarılı olup olmadığına bakıyoruz
            ticketResult
                .onSuccess { ticket ->
                    // Bilet başarılıysa, etkinliklerin de başarılı olup olmadığına bakıyoruz
                    eventsResult
                        .onSuccess { events ->
                            //index[0]-> Event(id=, name=Etkinlik Deneme, description=Etkinlik, venue=, startsAt=, endsAt=,
                            //   ticketTypes=[TicketType(id=fb, name=Bilet 1, priceCents=69999, capacity=100, soldCount=6, remaining=94), //   TicketType().......],
                            // map yapmasaydık eğer etkinliklere bakıp içerdeki biletleri tek tek gezip id uyuyormu diye bakardık
                            val ticketTypeToEventMap = events
                                .flatMap { event -> event.ticketTypes.map { it.id to event } } //it.id to event, bilet id si key ,event ise value yapar
                                .toMap()
                            //  Key: 7794d643... (Bilet 1 ID) -> Value: Event(id=5dc5..., name=Etkinlik Deneme......tickettypes=[....])Key: 39284f66... (Bilet 2 ID) -> Value: Event(id=5dc5..., name=Etkinlik Deneme)

                            val ticketTypeMap = events
                                .flatMap { it.ticketTypes }
                                .associateBy { it.id }
                            //Key (TicketTypeId): 39284f66-f672-4385-8a0e-93b926ae04d9
                            //Value: TicketType(id=39284f66-f672-4385-8a0e-93b926ae04d9, name=Bilet 2, priceCents=89999, capacity=100, soldCount=10, remaining=90)

                            enrichedTicket = TicketUi(
                                ticket = ticket,
                                event = ticketTypeToEventMap[ticket.ticketTypeId],
                                ticketType = ticketTypeMap[ticket.ticketTypeId],
                            )
                        }
                        .onFailure { error ->
                            detailError = error.toTicketDetailMessage()
                        }
                }
                .onFailure { error ->
                    detailError = error.toTicketDetailMessage()
                }

            // Tüm işlemler bittikten sonra tek bir güncelleme
            _state.update {
                it.copy(
                    isLoading = false,
                    ticket = enrichedTicket,
                    error = detailError
                )
            }
        }
    }
}

// Bilet Detay sayfasına özel (404 içermeyen) hata yakalayıcı
internal fun Throwable.toTicketDetailMessage(): String = when (this) {
    is ApiException -> when (code) {
        in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> "Beklenmeyen bir hata oluştu"
    }
    is NetworkException -> "İnternet bağlantısı yok"
    else -> message ?: "Bilinmeyen bir hata oluştu."
}