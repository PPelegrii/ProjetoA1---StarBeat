package a1.StarBeat.ui.viewmodels

import a1.StarBeat.data.local.entities.ScoreEntity
import a1.StarBeat.data.local.entities.SongEntity
import a1.StarBeat.data.repository.GameRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isLoading: Boolean = false,
    val songs: List<SongEntity> = emptyList(),
    val highScores: List<ScoreEntity> = emptyList(),
    val error: String? = null
)

class LibraryViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.allSongs,
        repository.highScores,
        _isLoading,
        _error
    ) { songs, scores, loading, error ->
        LibraryUiState(
            isLoading = loading,
            songs = songs,
            highScores = scores,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState(isLoading = true)
    )

    fun syncSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.syncSongsFromApi(clientId = "31dfeabd") // Client ID aqui
                .onFailure {
                    _error.value = "Falha ao sincronizar: ${it.message}"
                }
            _isLoading.value = false
        }
    }

    fun updateSong(song: SongEntity) {
        viewModelScope.launch {
            try {
                repository.updateSong(song)
            } catch (e: Exception) {
                _error.value = "Falha ao atualizar música: ${e.message}"
            }
        }
    }

    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            try {
                repository.deleteSong(song)
            } catch (e: Exception) {
                _error.value = "Falha ao remover música: ${e.message}"
            }
        }
    }

    fun clearScores() {
        viewModelScope.launch {
            try {
                repository.clearScoreHistory()
            } catch (e: Exception) {
                _error.value = "Falha ao limpar placares: ${e.message}"
            }
        }
    }

    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(songId)
            } catch (e: Exception) {
                _error.value = "Falha ao atualizar favorito: ${e.message}"
            }
        }
    }
}