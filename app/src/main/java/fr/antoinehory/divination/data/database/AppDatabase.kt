// Dans un nouveau fichier data/database/AppDatabase.kt
package fr.antoinehory.divination.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.antoinehory.divination.data.database.dao.LaunchLogDao
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.database.util.DateConverter
import fr.antoinehory.divination.data.database.util.GameTypeConverter

@Database(
    entities = [LaunchLog::class], // Uniquement notre nouvelle entité pour l'instant
    version = 1,                   // Première version de la base de données
    exportSchema = true            // Bon à garder pour les futures migrations
)
@TypeConverters(DateConverter::class, GameTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun launchLogDao(): LaunchLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "divination_database" // Nom de votre fichier de base de données
                )
                    // Pas besoin de .addMigrations() ou .fallbackToDestructiveMigration() pour la version 1
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}