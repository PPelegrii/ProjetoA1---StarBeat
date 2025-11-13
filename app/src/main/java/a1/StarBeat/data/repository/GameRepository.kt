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

    /**
     * Tenta logar o usuário. Se sucesso, salva o ID na sessão (DataStore).
     */
    suspend fun login(username: String, passwordHash: String): Result<UserEntity> = withContext(
        Dispatchers.IO) {
        runCatching {
            val user = userDao.getByUsername(username)
            if (user == null) throw Exception("Usuário não encontrado")
            if (user.passwordHash != passwordHash) throw Exception("Senha incorreta")

            // Salva o ID na sessão
            userPreferences.saveUserId(user.userId)
            user
        }
    }

    /**
     * Tenta registrar um novo usuário. Se sucesso, loga automaticamente.
     */
    suspend fun register(username: String, passwordHash: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val existingUser = userDao.getByUsername(username)
            if (existingUser != null) throw Exception("Nome de usuário já existe")

            // Cria um UserEntity sem especificar o id (Room irá gerar um id Int automaticamente)
            val newUser = UserEntity(
                username = username,
                passwordHash = passwordHash
            )

            // Insere e obtém o id da linha gerado (Long)
            val rowId = userDao.register(newUser)
            val generatedId = rowId.toInt()

            // Recupera o usuário persistido com o id gerado
            val persistedUser = userDao.getById(generatedId) ?: newUser.copy(userId = generatedId)

            // Salva o id gerado nas preferências (sessão)
            userPreferences.saveUserId(generatedId)

            persistedUser
        }
    }

    /**
     * Limpa o ID do usuário da sessão (DataStore).
     */
    suspend fun logout() {
        userPreferences.clearUserId()
    }

    // --- Funções de Músicas ---

    // Combina as músicas da API com as músicas locais cadastradas
    val allSongs: Flow<List<SongEntity>> =
        songDao.getLocalSongs().combine(songDao.getApiSongs()) { local, api ->
            // Coloca músicas locais primeiro e remove duplicatas por songId (prioriza local)
            val map = linkedMapOf<String, SongEntity>()
            local.forEach { map[it.songId] = it }
            api.forEach { if (!map.containsKey(it.songId)) map[it.songId] = it }
            map.values.toList()
        }

    // Busca as relações de favoritos apenas para o usuário logado
    @OptIn(ExperimentalCoroutinesApi::class)
    val favoriteRelations: Flow<List<UserSongFavoriteCrossRef>> = currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else songDao.getFavoriteRelationsForUser(userId)
    }

    // Busca as músicas favoritas apenas para o usuário logado
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFavoriteSongs(): Flow<UserWithFavoriteSongs?> = currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else songDao.getFavoriteSongsForUser(userId)
    }

    /**
     * Adiciona ou remove um favorito (toggle) para o usuário logado.
     */
    suspend fun toggleFavorite(songId: String) = withContext(Dispatchers.IO) {
        // Pega o ID do usuário logado no momento da ação
        val userId = currentUserId.first() ?: throw Exception("Usuário não autenticado")

        val favorite = UserSongFavoriteCrossRef(userId = userId, songId = songId)

        val existing = songDao.getFavoriteRelationsForUser(userId).first()
        if (existing.any { it.songId == songId }) {
            songDao.removeFavorite(favorite)
        } else {
            songDao.addFavorite(favorite)
        }
    }

    /**
     * Salva uma nova música cadastrada manualmente pelo usuário.
     */
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


    // --- Funções de Placar ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val highScores: Flow<List<ScoreEntity>> = currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(emptyList()) // Se não há usuário, não há placares
        } else {
            scoreDao.getHighScoresForUser(userId) // Busca placares do usuário
        }
    }

    /**
     * Salva o placar do jogo para o usuário logado.
     */
    suspend fun saveGameScore(songId: String, points: Int) = withContext(Dispatchers.IO) {
        val userId = currentUserId.first() ?: return@withContext // Pega o ID logado

        val score = ScoreEntity(
            userId = userId, // Salva o placar com o ID do usuário
            songId = songId,
            points = points
        )
        scoreDao.insertScore(score)
    }

    /**
     * Limpa o histórico de placares apenas do usuário logado.
     */
    suspend fun clearScoreHistory() = withContext(Dispatchers.IO) {
        val userId = currentUserId.first() ?: return@withContext
        scoreDao.clearScoresForUser(userId) // Deleta placares do usuário
    }
}