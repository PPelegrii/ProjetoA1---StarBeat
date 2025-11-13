package a1.StarBeat.ui.viewmodels

import a1.StarBeat.data.local.entities.SongEntity
import a1.StarBeat.data.media.MediaListener
import a1.StarBeat.data.media.MediaService
import a1.StarBeat.data.repository.GameRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class FallingBlock(
    val id: String = UUID.randomUUID().toString(),
    val column: Int,
    val yPosition: Float = 0.0f
)

enum class GameStatus {
    LOADING, PLAYING, GAMEOVER, FINISHED
}

data class GameUiState(
    val currentSong: SongEntity? = null,
    val blocks: List<FallingBlock> = emptyList(),
    val score: Int = 0,
    val gameStatus: GameStatus = GameStatus.LOADING,
    val error: String? = null
)

class GameViewModel(
    private val repository: GameRepository,
    private val mediaService: MediaService
) : ViewModel(), MediaListener { // <-- 1. DECLARA A IMPLEMENTAÇÃO DO LISTENER AQUI!

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoop: Job? = null
    private val columns = 5
    private val fallSpeedPerTick = 0.05f

    init {
        mediaService.setListener(this)
    }

    fun loadSong(songId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(gameStatus = GameStatus.LOADING) }
            val song = repository.getSong(songId)
            if (song == null) {
                _uiState.update { it.copy(error = "Música não encontrada", gameStatus = GameStatus.GAMEOVER) }
            } else {
                _uiState.update { it.copy(currentSong = song, gameStatus = GameStatus.PLAYING) }
                startGameLoop(song)
            }
        }
    }

    private fun startGameLoop(song: SongEntity) {
        gameLoop?.cancel()

        val beatIntervalMs = (60_000 / song.bpm).toLong()

        mediaService.play(song.audioPreviewUrl)

        gameLoop = viewModelScope.launch {
            while (_uiState.value.gameStatus == GameStatus.PLAYING) {

                generateNewBlock()

                delay(beatIntervalMs)

                updateBlockPositions()
            }
        }
    }

    override fun onPlaybackEnded() {
        _uiState.update { it.copy(gameStatus = GameStatus.FINISHED) }
        endGame()
    }

    override fun onCleared() {
        super.onCleared()
        mediaService.release()
    }

    private fun generateNewBlock() {
        val newColumn = (0 until columns).random()
        val newBlock = FallingBlock(column = newColumn, yPosition = 0.0f)

        _uiState.update { state ->
            state.copy(blocks = state.blocks + newBlock)
        }
    }

    private fun updateBlockPositions() {
        _uiState.update { state ->
            var updatedBlocks = state.blocks.map {
                it.copy(yPosition = it.yPosition + fallSpeedPerTick)
            }

            val missedBlock = updatedBlocks.firstOrNull { it.yPosition > 1.0f }

            if (missedBlock != null) {
                _uiState.update { it.copy(gameStatus = GameStatus.GAMEOVER) }
                endGame()
                return@update state
            }

            updatedBlocks = updatedBlocks.filter { it.yPosition <= 1.0f }

            state.copy(blocks = updatedBlocks)
        }
    }

    fun onColumnTapped(column: Int) {
        _uiState.update { state ->
            if (state.gameStatus != GameStatus.PLAYING) return@update state

            var score = state.score

            val blockToHit = state.blocks.firstOrNull {
                it.column == column && it.yPosition > 0.85f && it.yPosition <= 1.0f
            }

            val updatedBlocks = if (blockToHit != null) {
                score += 10
                state.blocks.filterNot { it.id == blockToHit.id }
            } else {
                _uiState.update { it.copy(gameStatus = GameStatus.GAMEOVER) }
                endGame()
                return@update state
            }

            state.copy(blocks = updatedBlocks, score = score)
        }
    }

    fun endGame() {
        gameLoop?.cancel()
        mediaService.stop()

        val finalState = _uiState.value

        if (finalState.gameStatus == GameStatus.FINISHED || finalState.gameStatus == GameStatus.GAMEOVER) {
            viewModelScope.launch {
                if (finalState.currentSong != null && finalState.score > 0) {
                    repository.saveGameScore(finalState.currentSong.songId, finalState.score)
                }
            }
        }

        if (finalState.gameStatus == GameStatus.PLAYING) {
            _uiState.update { it.copy(gameStatus = GameStatus.GAMEOVER) }
        }
    }
}