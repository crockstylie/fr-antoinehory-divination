package fr.antoinehory.divination.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.antoinehory.divination.data.database.dao.LaunchLogDao
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.database.util.DateConverter
import fr.antoinehory.divination.data.database.util.GameTypeConverter

/**
 * The main Room database class for the application.
 *
 * This class defines the database configuration including its entities, version,
 * and type converters. It serves as the primary access point to the persisted data
 * by providing abstract methods to retrieve Data Access Objects (DAOs).
 *
 * The database includes the following entities:
 * - [LaunchLog]: Stores logs of game plays.
 * - [DiceSet]: Stores user-defined sets of dice.
 *
 * Type converters [DateConverter] and [GameTypeConverter] are registered to handle
 * custom data types not natively supported by Room.
 * The database schema is exported, and the version is currently 2.
 */
@Database(
    entities = [
        LaunchLog::class,
        DiceSet::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DateConverter::class, GameTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides an instance of [LaunchLogDao] for interacting with launch log data.
     * @return The Data Access Object for [LaunchLog] entities.
     */
    abstract fun launchLogDao(): LaunchLogDao

    /**
     * Provides an instance of [DiceSetDao] for interacting with dice set data.
     * @return The Data Access Object for [DiceSet] entities.
     */
    abstract fun diceSetDao(): DiceSetDao

    /**
     * Companion object for managing the singleton instance of the [AppDatabase].
     */
    companion object {
        /**
         * Holds the singleton instance of [AppDatabase].
         * The `@Volatile` annotation ensures that writes to this property are immediately
         * visible to other threads.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of the [AppDatabase].
         *
         * If the instance is not yet created, it uses a synchronized block to ensure
         * thread-safe initialization. The database is built using [Room.databaseBuilder]
         * and configured with a fallback to destructive migration, which means the database
         * will be cleared and recreated if a schema upgrade path is not provided.
         *
         * @param context The application context, used to get the application context for the database builder.
         * @return The singleton [AppDatabase] instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "divination_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
