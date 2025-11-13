package a1.StarBeat.ui.viewmodels

import a1.StarBeat.data.repository.GameRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddMusicUiState(
    val title: String = "",
    val artist: String = "",
    val bpm: String = "", // BPM como String para o TextField
    val audioUri: String? = null, // URI selecionada
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class AddMusicViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMusicUiState())
    val uiState: StateFlow<AddMusicUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, error = null, saveSuccess = false) }
    }

    fun onArtistChange(artist: String) {
        _uiState.update { it.copy(artist = artist, error = null, saveSuccess = false) }
    }

    fun onBpmChange(bpm: String) {
        _uiState.update { it.copy(bpm = bpm, error = null, saveSuccess = false) }
    }

    fun onAudioUriSelected(uri: String?) {
        _uiState.update { it.copy(audioUri = uri, error = null, saveSuccess = false) }
    }
    fun saveSong() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }

            val state = _uiState.value
            val bpmInt = state.bpm.toIntOrNull()

            if (state.title.isBlank() || state.artist.isBlank() || bpmInt == null || bpmInt <= 0 || state.audioUri.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Preencha todos os campos e selecione um arquivo de áudio."
                    )
                }
                return@launch
            }

            try {
                repository.saveLocalSong(state.title, state.artist, bpmInt, state.audioUri)
                _uiState.value = AddMusicUiState(saveSuccess = true) // Reseta o formulário
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}