package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "prompt_history")
data class PromptHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val toolId: String,
    val toolTitle: String,
    val sectionId: String,
    val sectionTitle: String,
    val userInput: String,
    val secondaryInput: String,
    val parametersSummary: String,
    val parametersJson: String,
    val generatedOutput: String,
    val sourceBadge: String,
    val isSavedFavorite: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Dao
interface PromptHistoryDao {
    @Query("SELECT * FROM prompt_history ORDER BY createdAtMillis DESC")
    fun observeAllHistory(): Flow<List<PromptHistoryEntity>>

    @Query("SELECT * FROM prompt_history WHERE isSavedFavorite = 1 ORDER BY createdAtMillis DESC")
    fun observeSavedItems(): Flow<List<PromptHistoryEntity>>

    @Query("SELECT * FROM prompt_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PromptHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PromptHistoryEntity): Long

    @Query("UPDATE prompt_history SET isSavedFavorite = :saved WHERE id = :id")
    suspend fun setSavedFavorite(id: Long, saved: Boolean)

    @Query("DELETE FROM prompt_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM prompt_history")
    suspend fun clearAll()
}

@Database(
    entities = [PromptHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SNCreativityDatabase : RoomDatabase() {
    abstract fun promptHistoryDao(): PromptHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: SNCreativityDatabase? = null

        fun getInstance(context: Context): SNCreativityDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SNCreativityDatabase::class.java,
                    "sn_creativity_local.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class PromptHistoryRepository(private val dao: PromptHistoryDao) {
    val allHistory: Flow<List<PromptHistoryEntity>> = dao.observeAllHistory()
    val savedHistory: Flow<List<PromptHistoryEntity>> = dao.observeSavedItems()

    suspend fun saveEntry(entry: PromptHistoryEntity): Long = dao.insert(entry)

    suspend fun toggleFavorite(id: Long, saved: Boolean) = dao.setSavedFavorite(id, saved)

    suspend fun getEntry(id: Long): PromptHistoryEntity? = dao.getById(id)

    suspend fun deleteEntry(id: Long) = dao.deleteById(id)

    suspend fun clearAllHistory() = dao.clearAll()
}
