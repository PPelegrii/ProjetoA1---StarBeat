package a1.StarBeat.data.remote

import a1.StarBeat.data.local.entities.SongEntity
import com.google.gson.annotations.SerializedName

data class JamendoTracksResponse(
    val results: List<JamendoTrackItem>?
)

data class JamendoTrackItem(
    val id: String,
    val name: String,                  // título da faixa
    val artist_name: String,           // artista
    val duration: Int?,                 // em segundos
    val audio: String?,                 // stream url
    val audiodownload: String?,         // download url
    val audiodownload_allowed: Boolean?,// se permite download
    val album_image: String?,            // capa
    val musicinfo: MusicInfo?
) {
    fun toEntity(): SongEntity? {
        if (audiodownload.isNullOrEmpty()) return null // Ignora músicas sem áudio

        // Mapeia a "speed" da API para um 'bpm' numérico
        val bpm = when (this.musicinfo?.speed) {
            "verylow" -> 90
            "low" -> 115
            "medium" -> 150
            "high" -> 225
            "veryhigh" -> 300
            else -> 150 // Padrão se o campo 'speed' não vier preenchido
        }

        return SongEntity(
            songId = this.id,
            title = this.name,
            artistName = this.artist_name,
            audioPreviewUrl = this.audiodownload,
            bpm = bpm,
            creatorUserId = 0,
        )
    }
}

/**
 * Classe principal para o item da música (Track).
 */
data class TrackDta(
    val id: String,
    val name: String,
    val artist_name: String,
    @SerializedName("audiodownload")
    val audiodownload: String, // Usamos SerializedName se o campo JSON for diferente do Kotlin
    val musicinfo: MusicInfo?
)

/**
 * Classe aninhada para extrair o campo de velocidade (speed),
 * que mapeamos para o BPM.
 */
data class MusicInfo(
    val speed: String, // O campo que contém "verylow", "medium", etc.
    val vocalinstrumental: String
)
