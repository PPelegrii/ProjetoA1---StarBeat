package a1.StarBeat.data.repository

import a1.StarBeat.data.local.AppDatabase
import a1.StarBeat.data.local.entities.ScoreEntity
import a1.StarBeat.data.local.entities.SongEntity
import a1.StarBeat.data.local.entities.UserEntity
import a1.StarBeat.data.local.entities.UserSongFavoriteCrossRef
import a1.StarBeat.data.local.entities.UserWithFavoriteSongs
import a1.StarBeat.data.preferences.UserPreferences
import a1.StarBeat.data.remote.JamendoApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class GameRepository(
    database: AppDatabase,
    private val jamendoApiService: JamendoApiService,
    private val userPreferences: UserPreferences
) {
    private val songDao = database.songDao()
    private val scoreDao = database.scoreDao()
    private val userDao = database.userDao()

    val currentUserId: Flow<Int?> = userPreferences.loggedInUserId

    suspend fun login(username: String, passwordHash: String): Result<UserEntity> = withContext(
        Dispatchers.IO) {
        runCatching {
            val user = userDao.getByUsername(username)
            if (user == null) throw Exception("Usuário não encontrado")
            if (user.passwordHash != passwordHash) throw Exception("Senha incorreta")

            userPreferences.saveUserId(user.userId)
            user
        }
    }

    suspend fun register(username: String, passwordHash: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val existingUser = userDao.getByUsername(username)
            if (existingUser != null) throw Exception("Nome de usuário já existe")

            val newUser = UserEntity(
                username = username,
                passwordHash = passwordHash
            )

            val rowId = userDao.register(newUser)
            val generatedId = rowId.toInt()

            val persistedUser = userDao.getById(generatedId) ?: newUser.copy(userId = generatedId)

            userPreferences.saveUserId(generatedId)

            persistedUser
        }
    }

    suspend fun logout() {
        userPreferences.clearUserId()
    }

    val allSongs: Flow<List<SongEntity>> =
        songDao.getLocalSongs().combine(songDao.getApiSongs()) { local, api ->

            val map = linkedMapOf<String, SongEntity>()
            local.forEach { map[it.songId] = it }
            api.forEach { if (!map.containsKey(it.songId)) map[it.songId] = it }
            map.values.toList()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteRelations: Flow<List<UserSongFavoriteCrossRef>> = currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else songDao.getFavoriteRelationsForUser(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFavoriteSongs(): Flow<UserWithFavoriteSongs?> = currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else songDao.getFavoriteSongsForUser(userId)
    }


    suspend fun toggleFavorite(songId: String) = withContext(Dispatchers.IO) {

        val userId = currentUserId.first() ?: throw Exception("Usuário não autenticado")

        val favorite = UserSongFavoriteCrossRef(userId = userId, songId = songId)

        val existing = songDao.getFavoriteRelationsForUser(userId).first()
        if (existing.any { it.songId == songId }) {
            songDao.removeFavorite(favorite)
        } else {
            songDao.addFavorite(favorite)
        }
    }

    suspend fun saveLocalSong(title: String, artist: String, bpm: Int, audioUri: String) = withContext(Dispatchers.IO) {
        val newSong = SongEntity(
            songId = java.util.UUID.randomUUID().toString(),
            title = title,
            artistName = artist,
            audioPreviewUrl = audioUri, // armazena a URI do arquivo local (content://... ou file://...)
            bpm = bpm,
            isLocal = true,
            creatorUserId = currentUserId.first() ?: 0 // usuario logado no momento ou 0
        )
        songDao.insertSongLocal(newSong)
    }

    suspend fun syncSongsFromApi(clientId: String): Result<Unit> {
        return runCatching {
            val response = jamendoApiService.searchTracks(clientId = clientId)
            val songs = response.results!!.mapNotNull { it.toEntity() }
            if (songs.isNotEmpty()) {
                songDao.insertSongsApi(songs)
            }
        }
    }

    suspend fun updateSong(updatedSong: SongEntity) = withContext(Dispatchers.IO) {
        songDao.insertOrUpdateSong(updatedSong)
    }

    suspend fun deleteSong(updatedSong: SongEntity) = withContext(Dispatchers.IO) {
        songDao.deleteSong(updatedSong)
    }

    suspend fun getSong(songId: String): SongEntity? = songDao.getSongById(songId)


    @OptIn(ExperimentalCoroutinesApi::class)
    val highScores: Flow<List<ScoreEntity>> = currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(emptyList())
        } else {
            scoreDao.getHighScoresForUser(userId)
        }
    }

    suspend fun saveGameScore(songId: String, points: Int) = withContext(Dispatchers.IO) {
        val userId = currentUserId.first() ?: return@withContext // Pega o ID logado

        val score = ScoreEntity(
            userId = userId, 
            songId = songId,
            points = points
        )
        scoreDao.insertScore(score)
    }

    suspend fun clearScoreHistory() = withContext(Dispatchers.IO) {
        val userId = currentUserId.first() ?: return@withContext
        scoreDao.clearScoresForUser(userId)
    }
}