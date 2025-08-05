package fr.antoinehory.divination

import android.app.Application
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import fr.antoinehory.divination.data.repository.LaunchLogRepositoryImpl

class DivinationApplication : Application() {

    // Instance de AppDatabase accessible globalement mais initialisée paresseusement
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Instance de LaunchLogRepository accessible globalement
    // Elle utilise le DAO de notre base de données.
    val launchLogRepository: LaunchLogRepository by lazy {
        LaunchLogRepositoryImpl(database.launchLogDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Vous pouvez ajouter d'autres initialisations globales ici si nécessaire
    }
}
