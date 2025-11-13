package a1.StarBeat.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    tableName = "user_favorites",
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
    ]
)
data class UserSongFavoriteCrossRef(
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "song_id") val songId: String
)

data class UserWithFavoriteSongs(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "songId",
        associateBy = Junction(
            value = UserSongFavoriteCrossRef::class,
            parentColumn = "user_id",
            entityColumn = "song_id"
        )
    )
    val favoriteSongs: List<SongEntity>
)