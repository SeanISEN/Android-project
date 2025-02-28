// ------------------------------------ Package ------------------------------------
package fr.isen.Bouhaben.isensmartcompanion.database

// ------------------------------------ Imports ------------------------------------
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ------------------------------------ Database Definition ------------------------------------
/**
 * Room Database for storing chat messages exchanged between the user and the AI.
 * It contains a single table defined by the `ChatMessage` entity.
 */
@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {

    // Abstract function to get the DAO for accessing chat messages
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        /**
         * Retrieves an instance of the Room database.
         * Ensures a **singleton instance** to prevent multiple instances of the database
         * opening at the same time, which could cause memory leaks or inconsistencies.
         */
        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
