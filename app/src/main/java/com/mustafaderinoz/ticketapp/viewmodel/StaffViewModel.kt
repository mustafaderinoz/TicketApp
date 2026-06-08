package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.auth.AuthRepository
import com.mustafaderinoz.core.domain.checkin.CheckInRepository
import com.mustafaderinoz.core.domain.checkin.ScanResult
import com.mustafaderinoz.core.domain.error.AppError
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.util.ErrorContext
import com.mustafaderinoz.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StaffUiState(
    val isScanning: Boolean = false,
    val scanResult: ScanResult? = null,
    val scanError: String? = null,
    val toastMessage: String? = null,

    val isEventsLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val assignedEvents: List<Event> = emptyList(),
    val eventsError: String? = null
)

class StaffViewModel(
    private val checkInRepository: CheckInRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StaffUiState())
    val state: StateFlow<StaffUiState> = _state.asStateFlow()

    private var lastScannedCode: String? = null

    init {
        loadAssignedEvents()
    }


    fun loadAssignedEvents(isRefresh: Boolean = false) {
        if (_state.value.isEventsLoading || _state.value.isRefreshing) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isEventsLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    eventsError = null
                )
            }

            checkInRepository.getAssignedEvents()
                .onSuccess { events ->
                    _state.update {
                        it.copy(
                            isEventsLoading = false,
                            isRefreshing = false,
                            assignedEvents = events
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isEventsLoading = false,
                            isRefreshing = false,
                            eventsError = (error as? AppError)?.toUserMessage(ErrorContext.GENERIC)
                                ?: AppError.Unknown(error.message).toUserMessage()
                        )
                    }
                }
        }
    }

    fun onQrScanned(qrCode: String) {
        if (_state.value.isScanning || qrCode == lastScannedCode) return
        lastScannedCode = qrCode

        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanError = null, scanResult = null) }

            checkInRepository.scanQr(qrCode)
                .onSuccess { result ->
                    _state.update { it.copy(isScanning = false, scanResult = result) }
                }
                .onFailure { error ->
                    val errorMessage = (error as? AppError)?.toUserMessage(ErrorContext.SCAN)
                        ?: AppError.Unknown(error.message).toUserMessage()

                    val isAlreadyUsed = error is AppError.Api && error.code == 409

                    _state.update {
                        it.copy(
                            isScanning = false,
                            scanError = if (isAlreadyUsed) null else errorMessage,
                            toastMessage = if (isAlreadyUsed) errorMessage else null
                        )
                    }
                }
        }
    }

    fun clearResult() {
        lastScannedCode = null
        _state.update { it.copy(scanResult = null, scanError = null) }
    }
    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }
    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}