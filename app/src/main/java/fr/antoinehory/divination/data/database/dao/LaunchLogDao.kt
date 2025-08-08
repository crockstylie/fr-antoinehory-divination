// In data/database/dao/LaunchLogDao.kt
package fr.antoinehory.divination.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import kotlinx.coroutines.flow.Flow

@Dao
interface LaunchLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LaunchLog)

    @Query("SELECT * FROM launch_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LaunchLog>>

    @Query("SELECT * FROM launch_logs WHERE gameType = :gameType ORDER BY timestamp DESC")
    fun getLogsByGameType(gameType: GameType): Flow<List<LaunchLog>>

    // Nouvelle fonction pour récupérer les N derniers lancers
    @Query("SELECT * FROM launch_logs WHERE gameType = :gameType ORDER BY timestamp DESC LIMIT :count")
    fun getRecentLogsByGameType(gameType: GameType, count: Int): Flow<List<LaunchLog>>

    @Query("SELECT result, COUNT(result) as count FROM launch_logs WHERE gameType = :gameType GROUP BY result")
    fun getLaunchCountsByGameType(gameType: GameType): Flow<List<GameLaunchCount>>

    @Query("SELECT gameType, result, COUNT(result) as count FROM launch_logs GROUP BY gameType, result")
    fun getAllGameLaunchCounts(): Flow<List<AllGameLaunchStats>>


    @Query("DELETE FROM launch_logs WHERE gameType = :gameType")
    suspend fun deleteLogsByGameType(gameType: GameType)

    @Query("DELETE FROM launch_logs")
    suspend fun deleteAllLogs()
}

// Data class pour les statistiques d'un jeu spécifique
data class GameLaunchCount(
    val result: String,
    val count: Int
)

// Data class pour les statistiques agrégées de tous les jeux
data class AllGameLaunchStats(
    val gameType: GameType,
    val result: String,
    val count: Int
)

