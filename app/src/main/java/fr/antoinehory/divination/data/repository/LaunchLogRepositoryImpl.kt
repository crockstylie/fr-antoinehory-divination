package fr.antoinehory.divination.data.repository

import fr.antoinehory.divination.data.database.dao.LaunchLogDao
import fr.antoinehory.divination.data.database.dao.GameLaunchCount
import fr.antoinehory.divination.data.database.dao.AllGameLaunchStats
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Implementation of [LaunchLogRepository] that uses [LaunchLogDao] for data operations.
 * This class handles the logic for creating and retrieving [LaunchLog] entities.
 *
 * @property launchLogDao The Data Access Object for [LaunchLog] entities.
 */
class LaunchLogRepositoryImpl(
    private val launchLogDao: LaunchLogDao
) : LaunchLogRepository {

    /**
     * Inserts a new launch log into the database via the [LaunchLogDao].
     * A new [LaunchLog] entity is created with the current timestamp.
     *
     * @param gameType The [GameType] of the game that was played.
     * @param result A string representation of the outcome of the game play.
     */
    override suspend fun insertLog(gameType: GameType, result: String) {
        val newLog = LaunchLog(
            gameType = gameType,
            result = result,
            timestamp = Date()
        )
        launchLogDao.insertLog(newLog)
    }

    /**
     * Retrieves all launch logs from the database via the [LaunchLogDao].
     *
     * @return A [Flow] emitting a list of all [LaunchLog]s.
     */
    override fun getAllLogs(): Flow<List<LaunchLog>> {
        return launchLogDao.getAllLogs()
    }

    /**
     * Retrieves all launch logs for a specific [GameType] from the database via the [LaunchLogDao].
     *
     * @param gameType The [GameType] to filter logs by.
     * @return A [Flow] emitting a list of [LaunchLog]s for the specified game type.
     */
    override fun getLogsByGameType(gameType: GameType): Flow<List<LaunchLog>> {
        return launchLogDao.getLogsByGameType(gameType)
    }

    /**
     * Retrieves a specified number of recent launch logs for a specific [GameType]
     * from the database via the [LaunchLogDao].
     *
     * @param gameType The [GameType] to filter logs by.
     * @param count The maximum number of recent logs to retrieve.
     * @return A [Flow] emitting a list of recent [LaunchLog]s.
     */
    override fun getRecentLogsByGameType(gameType: GameType, count: Int): Flow<List<LaunchLog>> {
        return launchLogDao.getRecentLogsByGameType(gameType, count)
    }

    /**
     * Retrieves the count of each game result for a specific [GameType]
     * from the database via the [LaunchLogDao].
     *
     * @param gameType The [GameType] to get launch counts for.
     * @return A [Flow] emitting a list of [GameLaunchCount] objects.
     */
    override fun getLaunchCountsByGameType(gameType: GameType): Flow<List<GameLaunchCount>> {
        return launchLogDao.getLaunchCountsByGameType(gameType)
    }

    /**
     * Retrieves the count of each game result across all [GameType]s
     * from the database via the [LaunchLogDao].
     *
     * @return A [Flow] emitting a list of [AllGameLaunchStats] objects.
     */
    override fun getAllGameLaunchCounts(): Flow<List<AllGameLaunchStats>> {
        return launchLogDao.getAllGameLaunchCounts()
    }

    /**
     * Deletes all launch logs for a specific [GameType] from the database via the [LaunchLogDao].
     *
     * @param gameType The [GameType] whose logs are to be deleted.
     */
    override suspend fun deleteLogsByGameType(gameType: GameType) {
        launchLogDao.deleteLogsByGameType(gameType)
    }

    /**
     * Deletes all launch logs from the database via the [LaunchLogDao].
     */
    override suspend fun deleteAllLogs() {
        launchLogDao.deleteAllLogs()
    }
}
