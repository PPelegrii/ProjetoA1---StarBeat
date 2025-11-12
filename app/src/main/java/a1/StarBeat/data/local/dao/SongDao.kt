package a1.StarBeat.data.local.dao

import a1.StarBeat.data.local.entities.SongEntity
import a1.StarBeat.data.local.entities.UserSongFavoriteCrossRef
import a1.StarBeat.data.local.entities.UserWithFavoriteSongs
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    // Insere ou ATUALIZA músicas (usado para UPDATE de BPM/Favorito)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongsApi(songs: List<SongEntity>)

    // Insere uma única música (para músicas locais)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongLocal(song: SongEntity)

    @Query("SELECT * FROM songs WHERE isLocal = 0")
    fun getApiSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isLocal = 1")
    fun getLocalSongs(): Flow<List<SongEntity>>

    // Mostra todas as músicas (API + Criadas)
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    // Para a Tela de Perfil
    @Query("SELECT * FROM songs WHERE isFavorite = 1 AND creatorUserId = :userId")
    fun getFavoriteSongs(userId: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE songId = :id")
    suspend fun getSongById(id: String): SongEntity?

    @Delete
    suspend fun deleteSong(song: SongEntity)

    // --- Funções de Favoritos (Novas) ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: UserSongFavoriteCrossRef)

    @Delete
    suspend fun removeFavorite(favorite: UserSongFavoriteCrossRef)

    // Busca todas as músicas favoritas de um usuário
    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getFavoriteSongsForUser(userId: Int): Flow<UserWithFavoriteSongs?>

    @Query("SELECT * FROM user_favorites WHERE user_id = :userId")
    fun getFavoriteRelationsForUser(userId: Int): Flow<List<UserSongFavoriteCrossRef>>
}