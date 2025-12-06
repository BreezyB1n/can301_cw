package com.example.can301_cw.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.UserRepository
import com.example.can301_cw.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val isRegistrationSuccess: Boolean = false
)

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearRegistrationSuccess() {
        _uiState.update { it.copy(isRegistrationSuccess = false) }
    }

    fun login(account: String, password: String, onSuccess: () -> Unit) {
        if (account.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Username and password cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Simulate network delay for better UX
            delay(500) 
            
            val result = userRepository.login(account, password)
            
            _uiState.update { state ->
                when (result) {
                    is UserRepository.LoginResult.Success -> {
                        onSuccess()
                        state.copy(isLoading = false, currentUser = result.user, error = null)
                    }
                    is UserRepository.LoginResult.Error -> {
                        state.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isRegistrationSuccess = false) }
            delay(500)

            val result = userRepository.register(username, email, password)

            _uiState.update { state ->
                when (result) {
                    is UserRepository.RegisterResult.Success -> {
                        onSuccess()
                        state.copy(
                            isLoading = false, 
                            currentUser = result.user, 
                            error = null,
                            isRegistrationSuccess = true
                        )
                    }
                    is UserRepository.RegisterResult.Error -> {
                        state.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

