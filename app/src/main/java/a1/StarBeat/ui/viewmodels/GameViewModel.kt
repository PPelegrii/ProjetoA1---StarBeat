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
    val column: Int, // 0, 1, ou 2
    val yPosition: Float = 0.0f // Posição normalizada
)

// NOVO: Adicione um enum para estados de jogo mais claros
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
        // 2. CONFIGURA O LISTENER NO INÍCIO:
        // O MediaService irá chamar este ViewModel (this) quando a reprodução terminar.
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
            // O loop do jogo só continua enquanto o status for PLAYING
            while (_uiState.value.gameStatus == GameStatus.PLAYING) {
                // 1. Gera um novo bloco
                generateNewBlock()

                // 2. Espera o tempo da batida
                delay(beatIntervalMs)

                // 3. Atualiza a posição de todos os blocos
                updateBlockPositions()
            }
        }
    }

    // 3. IMPLEMENTAÇÃO DO CALLBACK DE FIM DE MÚSICA
    override fun onPlaybackEnded() {
        // Chamado pelo MediaService quando a música termina.
        // O status é definido como FINISHED antes de chamar endGame.
        _uiState.update { it.copy(gameStatus = GameStatus.FINISHED) }
        endGame()
    }

    override fun onCleared() {
        super.onCleared()
        mediaService.release() // Use release para liberar recursos completamente
    }

    // Lógica interna do jogo: Gera um novo bloco no topo
    private fun generateNewBlock() {
        val newColumn = (0 until columns).random()
        val newBlock = FallingBlock(column = newColumn, yPosition = 0.0f)

        _uiState.update { state ->
            state.copy(blocks = state.blocks + newBlock)
        }
    }

    // Lógica interna do jogo: Move os blocos para baixo
    private fun updateBlockPositions() {
        _uiState.update { state ->
            var updatedBlocks = state.blocks.map {
                it.copy(yPosition = it.yPosition + fallSpeedPerTick)
            }

            // Verifica se algum bloco foi perdido (passou do limite)
            val missedBlock = updatedBlocks.firstOrNull { it.yPosition > 1.0f }

            if (missedBlock != null) {
                // Se o jogador perder um bloco, encerra o jogo imediatamente
                _uiState.update { it.copy(gameStatus = GameStatus.GAMEOVER) }
                endGame()
                // Retorna a lista vazia ou a lista sem o bloco que causou o fim,
                // mas como o jogo acabou, isso não importa tanto.
                return@update state
            }

            // Filtra blocos que foram removidos (acertados) ou que passaram
            updatedBlocks = updatedBlocks.filter { it.yPosition <= 1.0f }

            state.copy(blocks = updatedBlocks)
        }
    }

    /**
     * Chamado pela UI quando o jogador toca em uma coluna.
     */
    fun onColumnTapped(column: Int) {
        _uiState.update { state ->
            if (state.gameStatus != GameStatus.PLAYING) return@update state // Ignora toques se o jogo não estiver ativo

            var score = state.score

            // Tenta encontrar um bloco na "zona de acerto" (ex: y > 0.85)
            val blockToHit = state.blocks.firstOrNull {
                it.column == column && it.yPosition > 0.85f && it.yPosition <= 1.0f
            }

            val updatedBlocks = if (blockToHit != null) {
                score += 10 // Acertou!
                // Remove o bloco que foi acertado
                state.blocks.filterNot { it.id == blockToHit.id }
            } else {
                // Errou! O toque errado encerra o jogo
                _uiState.update { it.copy(gameStatus = GameStatus.GAMEOVER) }
                endGame()
                return@update state // Sai da atualização
            }

            state.copy(blocks = updatedBlocks, score = score)
        }
    }

    /**
     * Função final chamada para encerrar o jogo (por fim de música ou falha do usuário).
     */
    fun endGame() {
        gameLoop?.cancel()
        mediaService.stop()

        val finalState = _uiState.value

        // Salva o placar se o jogo terminou ou foi concluído (não se estava em LOADING)
        if (finalState.gameStatus == GameStatus.FINISHED || finalState.gameStatus == GameStatus.GAMEOVER) {
            viewModelScope.launch {
                if (finalState.currentSong != null && finalState.score > 0) {
                    // REQUISITO: CRUD (Create) - Salva o placar
                    repository.saveGameScore(finalState.currentSong.songId, finalState.score)
                }
            }
        }

        // Garante que o estado seja atualizado para refletir o fim, caso a chamada venha de um toque ou bloco perdido.
        if (finalState.gameStatus == GameStatus.PLAYING) {
            _uiState.update { it.copy(gameStatus = GameStatus.GAMEOVER) }
        }
    }
}