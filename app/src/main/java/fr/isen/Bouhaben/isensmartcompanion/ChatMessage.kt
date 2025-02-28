// ------------------------------------ Package ------------------------------------
package fr.isen.Bouhaben.isensmartcompanion.database

// ------------------------------------ Imports ------------------------------------
import androidx.room.Entity
import androidx.room.PrimaryKey

// ------------------------------------ Entity Definition ------------------------------------
/**
 * Represents a chat message entity stored in the Room database.
 * Each instance of this class corresponds to a row in the **chat_messages** table.
 *
 * @property id Unique identifier for each message (auto-generated).
 * @property question The user's input message.
 * @property answer The AI's generated response.
 * @property timestamp The time when the message was created (default: current system time).
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-incremented primary key
    val question: String,  // User's question
    val answer: String,  // AI's response
    val timestamp: Long = System.currentTimeMillis()  // Timestamp for sorting
)
