package a1.StarBeat.data.local

import a1.StarBeat.data.local.dao.ScoreDao
import a1.StarBeat.data.local.dao.SongDao
import a1.StarBeat.data.local.dao.UserDao
import a1.StarBeat.data.local.entities.ScoreEntity
import a1.StarBeat.data.local.entities.SongEntity
import a1.StarBeat.data.local.entities.UserEntity
import a1.StarBeat.data.local.entities.UserSongFavoriteCrossRef
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =
    [
        SongEntity::class,
        ScoreEntity::class,
        UserEntity::class,
        UserSongFavoriteCrossRef::class
    ],
    version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun scoreDao(): ScoreDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "StarDB"

                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}