package fr.antoinehory.divination

import android.app.Application
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import fr.antoinehory.divination.data.repository.LaunchLogRepositoryImpl

/**
 * Custom [Application] class for the Divination application.
 *
 * This class is the entry point of the application and is used for initializing
 * application-wide components, such as the database and repositories.
 */
class DivinationApplication : Application() {

    /**
     * Lazily initializes the [AppDatabase] instance for the application.
     * The database is created when it's first accessed.
     */
    private val database by lazy { AppDatabase.getDatabase(this) }

    /**
     * Lazily initializes the [LaunchLogRepository] instance.
     * This repository is responsible for handling launch log data operations
     * and depends on the [AppDatabase.launchLogDao].
     */
    val launchLogRepository: LaunchLogRepository by lazy {
        LaunchLogRepositoryImpl(database.launchLogDao())
    }

    /**
     * Called when the application is starting, before any other application objects have been created.
     *
     * Use this method to perform global application initialization.
     * Currently, it calls the superclass's implementation.
     */
    override fun onCreate() {
        super.onCreate()
        // Application-specific initializations can be added here if needed.
    }
}
