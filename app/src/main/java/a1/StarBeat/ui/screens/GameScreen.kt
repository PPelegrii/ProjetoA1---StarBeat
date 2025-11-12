package a1.StarBeat.ui.screens

import a1.StarBeat.ui.viewmodels.FallingBlock
import a1.StarBeat.ui.viewmodels.GameStatus
import a1.StarBeat.ui.viewmodels.GameViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Número de colunas do jogo
private const val LANE_COUNT = 5
// Altura da "zona de acerto" onde o jogador deve tocar
private val HIT_ZONE_HEIGHT = 100.dp
// O tamanho (altura) de cada bloco que cai
private val BLOCK_HEIGHT = 50.dp

/**
 * A tela principal do jogo.
 *
 * @param songId O ID da música a ser carregada (passado pela navegação).
 * @param viewModel O GameViewModel injetado (via ViewModelFactory).
 * @param onGameEnd Callback para ser chamado quando o jogo terminar (para navegar de volta).
 */
@Composable
fun GameScreen(
    songId: String,
    viewModel: GameViewModel,
    onGameEnd: () -> Unit
) {
    // Coleta o UiState do ViewModel de forma segura
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 1. Efeito para carregar a música quando a tela é exibida pela primeira vez
    LaunchedEffect(songId) {
        if (uiState.gameStatus != GameStatus.PLAYING && uiState.gameStatus != GameStatus.GAMEOVER) {
            viewModel.loadSong(songId)
        }
    }

    // 2. Efeito para parar o jogo e salvar o placar se o usuário sair da tela
    DisposableEffect(viewModel) {
        onDispose {
            if (uiState.gameStatus == GameStatus.PLAYING) {
                viewModel.endGame()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E2E2E)) // Fundo escuro
    ) {
        // Pega as dimensões da tela para calcular as posições
        val screenHeight = constraints.maxHeight.toFloat()
        val screenWidth = constraints.maxWidth.toFloat()
        val laneWidth = screenWidth / LANE_COUNT

        // 3. Desenha as linhas que separam as colunas
        DrawLaneSeparators(laneWidth = laneWidth, screenHeight = screenHeight)

        // 4. Desenha a "Zona de Acerto" (Hit Zone) clicável
        HitZone(
            laneCount = LANE_COUNT,
            onLaneTapped = { laneIndex ->
                viewModel.onColumnTapped(laneIndex)
            }
        )

        // 5. Converte Dp em Px para cálculos de posição e desenha os blocos
        // É necessário obter a densidade atual para fazer a conversão de Dp para Px
        val density = LocalDensity.current
        val hitZoneTopPx = with(density) { screenHeight - HIT_ZONE_HEIGHT.toPx() }
        val blockHeightPx = with(density) { BLOCK_HEIGHT.toPx() }
        val laneWidthDp = with(density) { laneWidth.toDp() }


        uiState.blocks.forEach { block ->
            // Mapeia a posição Y (0.0 a 1.0) para a altura da tela em pixels
            // O bloco "nasce" em 0.0 e "atinge" o alvo em 1.0
            val yPositionPx = block.yPosition * (hitZoneTopPx - blockHeightPx)
            val yPositionDp = with(density) { yPositionPx.toDp() }

            FallingBlock(
                block = block,
                yPositionDp = yPositionDp,
                laneWidth = laneWidthDp
            )
        }
        // 6. Desenha o placar e o nome da música
        GameOverlayUI(
            score = uiState.score,
            songTitle = uiState.currentSong?.title ?: "Carregando..."
        )

        // 7. Mostra a tela de "Game Over"
        if (uiState.gameStatus == GameStatus.GAMEOVER) {
            GameOverOverlay(
                finalScore = uiState.score,
                onClose = onGameEnd // Usa o callback para navegar de volta
            )
        }
    }
}

/**
 * Desenha as linhas verticais que separam as colunas.
 */
@Composable
private fun DrawLaneSeparators(laneWidth: Float, screenHeight: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (i in 1 until LANE_COUNT) {
            val x = laneWidth * i
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(x, 0f),
                end = Offset(x, screenHeight),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Desenha a zona de acerto na parte inferior, que é clicável.
 */
@Composable
private fun BoxWithConstraintsScope.HitZone(
    laneCount: Int,
    onLaneTapped: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HIT_ZONE_HEIGHT)
            .align(Alignment.BottomCenter)
            .background(Color.Black.copy(alpha = 0.9f))
    ) {
        repeat(laneCount) { laneIndex ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .border(1.dp, Color.Gray.copy(alpha = 0.9f))
                    .clickable {
                        onLaneTapped(laneIndex)
                    }
            )
        }
    }
}

/**
 * Um Composable para um único bloco caindo.
 */
@Composable
private fun FallingBlock(
    block: FallingBlock,
    yPositionDp: Dp,
    laneWidth: Dp
) {
    Box(
        modifier = Modifier
            .size(width = laneWidth, height = BLOCK_HEIGHT)
            .offset(x = (laneWidth * block.column), y = yPositionDp)
            .background(Color.Cyan)
            .border(1.dp, Color.Black)
    )
}

/**
 * Desenha o placar e o título da música.
 */
@Composable
private fun BoxWithConstraintsScope.GameOverlayUI(score: Int, songTitle: String) {
    // Título da música
    Text(
        text = songTitle,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(16.dp),
        color = Color.White,
        fontSize = 16.sp
    )

    // Placar
    Text(
        text = "Score: $score",
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(16.dp),
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * A tela escura que aparece quando o jogo termina.
 */
@Composable
private fun GameOverOverlay(finalScore: Int, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) { }, // Bloqueia cliques no jogo
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Fim de Jogo",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Placar Final: $finalScore",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(onClick = onClose) {
                Text(text = "Voltar para Biblioteca")
            }
        }
    }
}
