package a1.StarBeat.ui.viewmodels

import a1.StarBeat.data.local.entities.ScoreEntity
import a1.StarBeat.data.local.entities.UserWithFavoriteSongs
import a1.StarBeat.data.repository.GameRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: GameRepository
) : ViewModel() {

    val highScores: StateFlow<List<ScoreEntity>> = repository.highScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteSongs: StateFlow<UserWithFavoriteSongs?> = repository.getFavoriteSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearScores() {
        viewModelScope.launch {
            repository.clearScoreHistory()
        }
    }
}