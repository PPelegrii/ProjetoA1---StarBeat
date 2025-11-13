package a1.StarBeat.ui.screens

import a1.StarBeat.ui.viewmodels.ProfileViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val highScores by viewModel.highScores.collectAsStateWithLifecycle()
    val favoritesData by viewModel.favoriteSongs.collectAsStateWithLifecycle()

    val favoriteSongs = favoritesData?.favoriteSongs ?: emptyList()
    val username = favoritesData?.user?.username ?: "Usuário"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil de $username",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    viewModel.logout()
                    onLogout()
                }) {
                    Text("Sair")
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Meus Recordes", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    if (highScores.isEmpty()) {
                        Text("Nenhum placar registrado.")
                    } else {
                        highScores.forEach { score ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Música: ${score.songId.take(6)}...", modifier = Modifier.weight(1f))
                                Text("Pontos: ${score.points}", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Button(onClick = { viewModel.clearScores() }, enabled = highScores.isNotEmpty()) {
                        Text("Limpar Recordes")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Músicas Favoritas",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (favoriteSongs.isEmpty()) {
            item {
                Text("Você ainda não favoritou nenhuma música.")
            }
        } else {
            items(favoriteSongs, key = { it.songId }) { song ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(song.title, fontWeight = FontWeight.Medium)
                        Text("BPM: ${song.bpm} ${if (song.isLocal) "(Local)" else ""}", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}