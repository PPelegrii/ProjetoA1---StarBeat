package a1.StarBeat.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistName: String,
    val audioPreviewUrl: String, // URL da Jamendo (API)
    var bpm: Int,
    val creatorUserId: Int,

    var isFavorite: Boolean = false,

    @ColumnInfo(defaultValue = "0")
    val isLocal: Boolean = false
)