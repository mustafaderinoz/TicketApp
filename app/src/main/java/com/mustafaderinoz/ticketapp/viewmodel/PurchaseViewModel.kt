package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.event.TicketType
import com.mustafaderinoz.core.domain.purchase.CreatePurchaseItem
import com.mustafaderinoz.core.domain.purchase.Purchase
import com.mustafaderinoz.core.domain.purchase.PurchaseRepository
import com.mustafaderinoz.data.network.ApiException
import com.mustafaderinoz.data.network.NetworkException
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
                    val isCapacityExceeded = error is ApiException && error.code == 409
                    _state.update {
                        it.copy(
                            isCreatingPurchase = false,
                            error = error.toPurchaseCreateMessage(),
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
                    _state.update {
                        it.copy(
                            isPaying = false,
                            error = error.toPayMessage()
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

// ── API'ye Uygun Hata Çeviriciler ─────────────────────────────────────────────

internal fun Throwable.toPurchaseCreateMessage(): String = when (this) {
    is ApiException -> when (code) {
        400 -> "Geçersiz bilet adedi veya veri formatı."
        404 -> "Seçilen bilet türü bulunamadı."
        409 -> "Kapasite aşıldı, lütfen sayfayı yenileyip stokları kontrol edin."
        in 500..599 -> "Sunucu şu anda cevap veremiyor."
        else -> "Beklenmeyen bir hata oluştu."
    }
    is NetworkException -> "İnternet bağlantısı yok."
    else -> message ?: "Bilinmeyen bir hata oluştu."
}

internal fun Throwable.toPayMessage(): String = when (this) {
    is ApiException -> when (code) {
        403 -> "Bu satın alım işlemini onaylama yetkiniz yok."
        404 -> "Satın alım kaydı bulunamadı."
        409 -> "Bu bilet zaten ödenmiş veya stok tükenmiş."
        in 500..599 -> "Sunucu şu anda cevap veremiyor."
        else -> "Beklenmeyen bir hata oluştu."
    }
    is NetworkException -> "İnternet bağlantısı yok."
    else -> message ?: "Bilinmeyen bir hata oluştu."
}