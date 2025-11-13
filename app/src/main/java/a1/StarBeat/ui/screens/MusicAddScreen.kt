package a1.StarBeat.ui.screens

import a1.StarBeat.ui.viewmodels.AddMusicViewModel
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri

@Composable
fun AddMusicScreen(
    viewModel: AddMusicViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher para abrir o picker de documentos (audio/*)
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                // Persiste permissão de leitura para a URI selecionada
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) { /* ignore */ }
                viewModel.onAudioUriSelected(uri.toString())
            }
        }
    )

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Música salva com sucesso!", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Adicionar Música Local",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.onTitleChange(it) },
            label = { Text("Nome da Música") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.artist,
            onValueChange = { viewModel.onArtistChange(it) },
            label = { Text("Nome do Artista") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.bpm,
            onValueChange = { viewModel.onBpmChange(it) },
            label = { Text("BPM (Batidas Por Minuto)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Botão para escolher arquivo de áudio
        Button(onClick = { pickAudioLauncher.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = if (uiState.audioUri == null) "Selecionar Arquivo de Áudio" else "Áudio Selecionado")
        }

        uiState.audioUri?.let {
            Text(text = "Arquivo: ${it.toUri().lastPathSegment ?: it}", maxLines = 1)
        }

        Spacer(Modifier.height(8.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.saveSong() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Música Local")
            }
        }

        uiState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        if (uiState.saveSuccess) {
            Text("Música salva com sucesso!", color = MaterialTheme.colorScheme.primary)
        }
    }
}