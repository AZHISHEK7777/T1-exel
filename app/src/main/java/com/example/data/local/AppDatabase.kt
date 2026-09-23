package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_sensitivities")
data class SavedSensitivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val dpi: Int,
    val buttonSize: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "global_chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val senderName: String,
    val senderRole: String, // "ADMIN", "VIP", "PRO"
    val messageText: String,
    val attachmentUri: String? = null,
    val attachmentType: String = "NONE", // "IMAGE", "TACTICAL", "NONE"
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false
)

@Dao
interface SensitivityDao {
    @Query("SELECT * FROM saved_sensitivities ORDER BY createdAt DESC")
    fun getAllSensitivities(): Flow<List<SavedSensitivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensitivity(sensitivity: SavedSensitivityEntity): Long

    @Query("DELETE FROM saved_sensitivities WHERE id = :id")
    suspend fun deleteSensitivity(id: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM global_chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM global_chat_messages")
    suspend fun clearMessages()

    @Query("DELETE FROM global_chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM global_chat_messages WHERE senderName IN ('Abhishek (Developer)', 'Rahul_Pro', 'Viper_ES') OR messageText LIKE '%Welcome to T1 Esports Community Hub%' OR messageText LIKE '%Tatsuya + Maro + Hayato%' OR messageText LIKE '%Anyone up for custom 1v1 room%'")
    suspend fun deleteFakeMessages()
}

@Database(entities = [SavedSensitivityEntity::class, ChatMessageEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensitivityDao(): SensitivityDao
    abstract fun chatDao(): ChatDao
}
