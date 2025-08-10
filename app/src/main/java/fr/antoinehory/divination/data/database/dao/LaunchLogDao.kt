package fr.antoinehory.divination.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [LaunchLog] entities.
 * Provides methods to interact with the "launch_logs" table in the database,
 * including statistical queries.
 */
@Dao
interface LaunchLogDao {

    /**
     * Inserts a [LaunchLog] into the database. If a log with the same ID already exists,
     * it will be replaced.
     *
     * @param log The [LaunchLog] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LaunchLog)

    /**
     * Retrieves all [LaunchLog]s from the database, ordered by timestamp in descending order (most recent first).
     *
     * @return A [Flow] emitting a list of all [LaunchLog]s.
     */
    @Query("SELECT * FROM launch_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LaunchLog>>

    /**
     * Retrieves all [LaunchLog]s for a specific [GameType] from the database,
     * ordered by timestamp in descending order (most recent first).
     *
     * @param gameType The [GameType] to filter logs by.
     * @return A [Flow] emitting a list of [LaunchLog]s for the specified game type.
     */
    @Query("SELECT * FROM launch_logs WHERE gameType = :gameType ORDER BY timestamp DESC")
    fun getLogsByGameType(gameType: GameType): Flow<List<LaunchLog>>

    /**
     * Retrieves a specified number of recent [LaunchLog]s for a specific [GameType],
     * ordered by timestamp in descending order.
     *
     * @param gameType The [GameType] to filter logs by.
     * @param count The maximum number of recent logs to retrieve.
     * @return A [Flow] emitting a list of recent [LaunchLog]s.
     */
    @Query("SELECT * FROM launch_logs WHERE gameType = :gameType ORDER BY timestamp DESC LIMIT :count")
    fun getRecentLogsByGameType(gameType: GameType, count: Int): Flow<List<LaunchLog>>

    /**
     * Retrieves the count of each game result for a specific [GameType].
     *
     * @param gameType The [GameType] to get launch counts for.
     * @return A [Flow] emitting a list of [GameLaunchCount] objects,
     *         where each object represents a result and its occurrence count.
     */
    @Query("SELECT result, COUNT(result) as count FROM launch_logs WHERE gameType = :gameType GROUP BY result")
    fun getLaunchCountsByGameType(gameType: GameType): Flow<List<GameLaunchCount>>

    /**
     * Retrieves the count of each game result across all [GameType]s.
     *
     * @return A [Flow] emitting a list of [AllGameLaunchStats] objects,
     *         where each object represents a game type, a result, and its occurrence count.
     */
    @Query("SELECT gameType, result, COUNT(result) as count FROM launch_logs GROUP BY gameType, result")
    fun getAllGameLaunchCounts(): Flow<List<AllGameLaunchStats>>


    /**
     * Deletes all [LaunchLog]s for a specific [GameType] from the database.
     *
     * @param gameType The [GameType] whose logs are to be deleted.
     */
    @Query("DELETE FROM launch_logs WHERE gameType = :gameType")
    suspend fun deleteLogsByGameType(gameType: GameType)

    /**
     * Deletes all [LaunchLog]s from the database.
     */
    @Query("DELETE FROM launch_logs")
    suspend fun deleteAllLogs()
}

/**
 * Represents the count of a specific game result for a particular game type.
 * Used for statistical aggregation.
 *
 * @property result The string representation of the game result.
 * @property count The number of times this result occurred.
 */
data class GameLaunchCount(
    val result: String,
    val count: Int
)

/**
 * Represents the count of a specific game result for a particular game type,
 * including the game type itself. Used for global game statistics aggregation.
 *
 * @property gameType The [GameType] of the game.
 * @property result The string representation of the game result.
 * @property count The number of times this result occurred for this game type.
 */
data class AllGameLaunchStats(
    val gameType: GameType,
    val result: String,
    val count: Int
)

