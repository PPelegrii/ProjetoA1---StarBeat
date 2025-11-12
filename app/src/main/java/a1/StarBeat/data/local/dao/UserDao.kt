package a1.StarBeat.data.local.dao

import a1.StarBeat.data.local.entities.UserEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun register(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE UserId = :id LIMIT 1")
    suspend fun getById(id: Int): UserEntity?
}