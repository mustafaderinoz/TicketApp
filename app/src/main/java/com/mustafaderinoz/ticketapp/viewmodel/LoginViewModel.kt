package com.mustafaderinoz.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafaderinoz.core.domain.auth.AuthRepository
import com.mustafaderinoz.data.network.ApiException
import com.mustafaderinoz.data.network.NetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(val email: String = "",
                        val password: String = "",
                        val isLoading: Boolean = false,
                        val errorMessage: String? = null,
                        val isLoggedIn: Boolean = false
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.length >= 8 && !isLoading
}

class LoginViewModel(
    private val authRepository: AuthRepository //bağımlılık
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    //fun consumeError() = _state.update { it.copy(errorMessage = null) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.login(current.email, current.password)
                .onSuccess { _state.update { it.copy(isLoading = false, isLoggedIn = true) } }
                .onFailure { error -> _state.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) } }
        }
    }
}

// Ömürlük
internal fun Throwable.toUserMessage(): String = when(this)
{
    is ApiException -> when(code) {
        401 -> "Email veya şifre hatalı"
        in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> "Beklenmeyen bir hata oluştu"
    }
    is NetworkException -> "İnternet bağlantısı yok"
    else -> message ?: "Bilinmeyen bir hata oluştu."
}
