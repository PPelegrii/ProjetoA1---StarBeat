package a1.StarBeat.ui.screens

import a1.StarBeat.ui.viewmodels.LibraryViewModel
import a1.StarBeat.data.local.entities.ScoreEntity
import a1.StarBeat.data.local.entities.SongEntity
import a1.StarBeat.ui.EditSongDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onSongSelected: (songId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val songs = uiState.songs
    val highScores = uiState.highScores
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    var songToEdit by remember { mutableStateOf<SongEntity?>(null) }

    songToEdit?.let { song ->
        EditSongDialog(
            song = song,
            onDismiss = { songToEdit = null },
            onUpdate = { updatedSong ->
                viewModel.updateSong(updatedSong) 
                songToEdit = null 
            },
            onDelete = { updatedSong ->
                viewModel.deleteSong(updatedSong) 
                songToEdit = null 
            },
            toggleFavorite = { s -> viewModel.toggleFavorite(s.songId) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Biblioteca de Musicas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.syncSongs() }) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Sincronizar API")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.error != null) {
            Text(
                text = "ERRO: ${uiState.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
-
        HighScorePanel(
            highScores = highScores,
            onClearScores = { viewModel.clearScores() }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Selecione uma Música para Jogar:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (songs.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma música encontrada. Sincronize com a API.")
                    }
                }
            } else {
                items(songs, key = { it.songId }) { song ->
                    SongListItem(
                        song = song,
                        onPlay = onSongSelected,
                        onEdit = { songToEdit = song },
                        onToggleFavorite = { viewModel.toggleFavorite(song.songId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HighScorePanel(
    highScores: List<ScoreEntity>,
    onClearScores: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recordes (TOP 10)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Button(onClick = onClearScores, enabled = highScores.isNotEmpty()) {
                    Icon(Icons.Default.Delete, contentDescription = "Limpar Histórico")
                    Text(" Limpar")
                }
            }

            Spacer(Modifier.height(8.dp))
            if (highScores.isEmpty()) {
                Text("Nenhum placar registrado ainda.")
            } else {
                highScores.take(10).forEachIndexed { index, score ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${index + 1}. Música ID ${score.songId.take(4)}...")
                        Text("Pontos: ${score.points}", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SongListItem(
    song: SongEntity,
    onPlay: (String) -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay(song.songId) }
            .padding(vertical = 12.dp)
            .background(Color.White, MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(
                "Artista: ${song.artistName} | BPM: ${song.bpm}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        IconButton(onClick = onToggleFavorite) {
            if (song.isFavorite) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorito", tint = Color.Red)
            } else {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Marcar favorito")
            }
        }

        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar BPM e Favorito",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    HorizontalDivider(color = Color.LightGray)
}