package fr.antoinehory.divination.data.repository

import fr.antoinehory.divination.data.database.dao.GameLaunchCount
import fr.antoinehory.divination.data.database.dao.AllGameLaunchStats
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing [LaunchLog] data.
 * This interface defines the contract for operations related to game launch logs,
 * abstracting the data source from the rest of the application.
 */
interface LaunchLogRepository {
    /**
     * Inserts a new launch log into the data source.
     *
     * @param gameType The [GameType] of the game that was played.
     * @param result A string representation of the outcome of the game play.
     */
    suspend fun insertLog(gameType: GameType, result: String)

    /**
     * Retrieves all launch logs from the data source, ordered by recency.
     *
     * @return A [Flow] emitting a list of all [LaunchLog]s.
     */
    fun getAllLogs(): Flow<List<LaunchLog>>

    /**
     * Retrieves all launch logs for a specific [GameType] from the data source, ordered by recency.
     *
     * @param gameType The [GameType] to filter logs by.
     * @return A [Flow] emitting a list of [LaunchLog]s for the specified game type.
     */
    fun getLogsByGameType(gameType: GameType): Flow<List<LaunchLog>>

    /**
     * Retrieves a specified number of recent launch logs for a specific [GameType], ordered by recency.
     *
     * @param gameType The [GameType] to filter logs by.
     * @param count The maximum number of recent logs to retrieve.
     * @return A [Flow] emitting a list of recent [LaunchLog]s.
     */
    fun getRecentLogsByGameType(gameType: GameType, count: Int): Flow<List<LaunchLog>>

    /**
     * Retrieves the count of each game result for a specific [GameType].
     * This is typically used for displaying statistics.
     *
     * @param gameType The [GameType] to get launch counts for.
     * @return A [Flow] emitting a list of [GameLaunchCount] objects,
     *         where each object represents a result and its occurrence count.
     */
    fun getLaunchCountsByGameType(gameType: GameType): Flow<List<GameLaunchCount>>

    /**
     * Retrieves the count of each game result across all [GameType]s.
     * This provides an overview of game play statistics for all games.
     *
     * @return A [Flow] emitting a list of [AllGameLaunchStats] objects,
     *         where each object represents a game type, a result, and its occurrence count.
     */
    fun getAllGameLaunchCounts(): Flow<List<AllGameLaunchStats>>

    /**
     * Deletes all launch logs for a specific [GameType] from the data source.
     *
     * @param gameType The [GameType] whose logs are to be deleted.
     */
    suspend fun deleteLogsByGameType(gameType: GameType)

    /**
     * Deletes all launch logs from the data source.
     * Use with caution as this will remove all game history.
     */
    suspend fun deleteAllLogs()
}

