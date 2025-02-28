// ------------------------------------ Package ------------------------------------
package fr.isen.Bouhaben.isensmartcompanion.database

// ------------------------------------ Imports ------------------------------------
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ------------------------------------ Data Access Object (DAO) ------------------------------------
/**
 * DAO (Data Access Object) for handling operations on the ChatMessage table.
 * This interface defines methods to interact with the local Room database.
 */
@Dao
interface ChatMessageDao {

    /**
     * Retrieves all chat messages from the database.
     * The messages are ordered by timestamp in **descending order** (latest first).
     * @return A **Flow** of list of chat messages for reactive UI updates.
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    /**
     * Inserts a new chat message into the database.
     * If a conflict occurs (same primary key), the message will be **replaced**.
     * @param chatMessage The chat message object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(chatMessage: ChatMessage)

    /**
     * Deletes a specific chat message from the database.
     * @param chatMessage The chat message to be deleted.
     */
    @Delete
    suspend fun deleteMessage(chatMessage: ChatMessage)

    /**
     * Deletes **all chat messages** from the database.
     * This method is useful for clearing chat history.
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
