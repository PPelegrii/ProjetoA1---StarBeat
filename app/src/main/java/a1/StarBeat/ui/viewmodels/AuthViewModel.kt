package a1.StarBeat.ui.viewmodels

import a1.StarBeat.data.repository.GameRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class AuthViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val loggedInUserId: Flow<Int?> = repository.currentUserId

    fun login(username: String, passwordHash: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.login(username, passwordHash)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun register(username: String, passwordHash: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            if (username.isEmpty() || passwordHash.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Digite algo :)") }
                return@launch
            }
            repository.register(username, passwordHash)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}