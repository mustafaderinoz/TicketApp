package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.error.AppError
import com.mustafaderinoz.core.domain.event.TicketType
import com.mustafaderinoz.core.domain.purchase.CreatePurchaseItem
import com.mustafaderinoz.core.domain.purchase.Purchase
import com.mustafaderinoz.core.domain.purchase.PurchaseRepository
import com.mustafaderinoz.core.util.ErrorContext
import com.mustafaderinoz.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseUiState(
    val isCreatingPurchase: Boolean = false,
    val isPaying: Boolean = false,
    val purchaseForConfirmation: Purchase? = null,
    val ticketTypes: List<TicketType> = emptyList(),
    val quantities: Map<String, Int> = emptyMap(),
    val error: String? = null,
    val isPurchaseCompleted: Boolean = false,
    val completedPurchaseId: String? = null,
    val shouldRefreshEvent: Boolean = false
)

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PurchaseUiState())
    val state: StateFlow<PurchaseUiState> = _state.asStateFlow()

    fun createPurchase(
        ticketTypes: List<TicketType>,
        quantities: Map<String, Int>,
    ) {
        val items = quantities
            .filter { (_, qty) -> qty > 0 }
            .map { (id, qty) -> CreatePurchaseItem(ticketTypeId = id, quantity = qty) }

        if (items.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isCreatingPurchase = true, error = null) }

            purchaseRepository.createPurchase(items)
                .onSuccess { purchase ->
                    _state.update {
                        it.copy(
                            isCreatingPurchase = false,
                            purchaseForConfirmation = purchase,
                            ticketTypes = ticketTypes,
                            quantities = quantities
                        )
                    }
                }
                .onFailure { error ->
                    val isCapacityExceeded = error is AppError.Api && error.code == 409
                    val message = (error as? AppError)?.toUserMessage(ErrorContext.PURCHASE_CREATE)
                        ?: AppError.Unknown(error.message).toUserMessage()
                    _state.update {
                        it.copy(
                            isCreatingPurchase = false,
                            error = message,
                            shouldRefreshEvent = isCapacityExceeded
                        )
                    }
                }
        }
    }

    fun pay() {
        val purchaseId = _state.value.purchaseForConfirmation?.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isPaying = true, error = null) }

            purchaseRepository.pay(purchaseId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isPaying = false,
                            isPurchaseCompleted = true,
                            completedPurchaseId = purchaseId,
                            purchaseForConfirmation = null
                        )
                    }
                }
                .onFailure { error ->
                    val message = (error as? AppError)?.toUserMessage(ErrorContext.PAY)
                        ?: AppError.Unknown(error.message).toUserMessage()

                    _state.update {
                        it.copy(
                            isPaying = false,
                            error = message
                        )
                    }
                }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun dismissConfirmation() {
        _state.update { it.copy(purchaseForConfirmation = null) }
    }

    fun onNavigationConsumed() {
        _state.update { it.copy(isPurchaseCompleted = false, completedPurchaseId = null) }
    }

    fun onRefreshConsumed() {
        _state.update { it.copy(shouldRefreshEvent = false) }
    }
}

