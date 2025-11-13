package a1.StarBeat.ui

import a1.StarBeat.data.local.entities.SongEntity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Diálogo para editar o BPM e o status de favorito de uma música.
 * Demonstra o requisito CRUD 'Update'.
 *
 * @param song A entidade SongEntity atual a ser editada.
 * @param onDismiss Callback para fechar o diálogo.
 * @param onUpdate Callback chamado com a SongEntity atualizada para salvar no Room.
 * @param onDelete Callback para deletar a música.
 * @param toggleFavorite Opcional: callback para alternar favorito diretamente (default: no-op).
 */
@Composable
fun EditSongDialog(
    song: SongEntity,
    onDismiss: () -> Unit,
    onUpdate: (SongEntity) -> Unit,
    onDelete: (SongEntity) -> Unit,
    toggleFavorite: (SongEntity) -> Unit = {} // parâmetro opcional adicionado
) {
    // Estado local para o novo BPM, inicializado com o valor atual
    var newBpm by remember { mutableStateOf(song.bpm.toString()) }
    // Estado local para o favorito
    var isFavorite by remember { mutableStateOf(song.isFavorite) }

    // Verificação de erro para o BPM
    val isBpmValid = newBpm.toIntOrNull() != null && newBpm.toInt() in 50..300

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Música: ${song.title.take(25)}...") },
        text = {
            Column {
                Text(
                    "Ajuste manual do BPM afeta a velocidade de queda dos blocos no jogo. (50-300)"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo para Edição do BPM
                TextField(
                    value = newBpm,
                    onValueChange = { newBpm = it.filter { char -> char.isDigit() } }, // Aceita apenas dígitos
                    label = { Text("BPM (Batidas Por Minuto)") },
                    isError = !isBpmValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isBpmValid) {
                    Text(
                        text = "O BPM deve ser um número entre 50 e 300.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Switch para Favorito (Exemplo simples de Update)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marcar como Favorito")
                    Switch(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it }
                    )
                }
                Button(
                    onClick = {
                        onDelete(song) // Chama o ViewModel para persistir
                    },
                ) { Text("Deletar Música") }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isBpmValid) {
                        val updatedSong = song.copy(
                            bpm = newBpm.toInt(),
                            isFavorite = isFavorite
                        )
                        onUpdate(updatedSong)
                        if (song.isFavorite != isFavorite) {
                            toggleFavorite(song)
                        }
                    }
                },
                enabled = isBpmValid // Só habilita se o BPM for válido
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}