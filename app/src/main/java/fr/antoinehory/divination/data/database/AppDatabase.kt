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
// DiceConfigListConverter est déjà géré par l'annotation sur l'entité DiceSet

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

    abstract fun launchLogDao(): LaunchLogDao
    abstract fun diceSetDao(): DiceSetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "divination_database"
                )
                    // MODIFIÉ : Utilisation de la version surchargée de fallbackToDestructiveMigration
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}