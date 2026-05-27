package com.example.safekidsmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safekidsmobile.data.repository.AuthRepository
import com.example.safekidsmobile.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(AuthUiState())
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(AuthUiState())
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = _loginState.value.copy(error = "Email and password are required")
            return
        }
        viewModelScope.launch {
            _loginState.value = AuthUiState(isLoading = true)
            _loginState.value = when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> AuthUiState(isSuccess = true)
                is AuthResult.Error  -> AuthUiState(error = result.message)
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            _registerState.value = _registerState.value.copy(error = "All fields are required")
            return
        }
        if (password.length < 6) {
            _registerState.value = _registerState.value.copy(error = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _registerState.value = AuthUiState(isLoading = true)
            _registerState.value = when (val result = authRepository.register(email, password, fullName)) {
                is AuthResult.Success -> AuthUiState(isSuccess = true)
                is AuthResult.Error  -> AuthUiState(error = result.message)
            }
        }
    }

    fun clearLoginState() { _loginState.value = AuthUiState() }
    fun clearRegisterState() { _registerState.value = AuthUiState() }
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
    fun logout() {
        authRepository.logout()
        _loginState.value = AuthUiState()
        _registerState.value = AuthUiState()
    }
}
