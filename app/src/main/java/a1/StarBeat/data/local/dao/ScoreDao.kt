package a1.StarBeat.data.local.dao

import a1.StarBeat.data.local.entities.ScoreEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity)

    @Query("SELECT * FROM scores WHERE user_id = :userId ORDER BY points DESC LIMIT 10")
    fun getHighScoresForUser(userId: Int): Flow<List<ScoreEntity>>

    @Query("DELETE FROM scores WHERE user_id = :userId")
    suspend fun clearScoresForUser(userId: Int)
}