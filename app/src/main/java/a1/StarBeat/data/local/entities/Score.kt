package a1.StarBeat.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entidade B (Placar):
 * Armazena o histórico de pontuação.
 * Usamos ForeignKey para ligar o placar à música jogada.
 */
@Entity(
    tableName = "scores",
    primaryKeys = ["user_id", "song_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["songId"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class ScoreEntity(
    @ColumnInfo(name = "user_id", index = true) val userId: Int,
    @ColumnInfo(name = "song_id", index = true) val songId: String,
    val points: Int,
    val timestamp: Long = System.currentTimeMillis()
)