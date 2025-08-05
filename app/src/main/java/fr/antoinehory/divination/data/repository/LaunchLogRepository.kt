// In data/repository/LaunchLogRepository.kt
package fr.antoinehory.divination.data.repository

import fr.antoinehory.divination.data.database.dao.GameLaunchCount
import fr.antoinehory.divination.data.database.dao.AllGameLaunchStats
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import kotlinx.coroutines.flow.Flow

interface LaunchLogRepository {
    suspend fun insertLog(gameType: GameType, result: String)
    fun getAllLogs(): Flow<List<LaunchLog>>
    fun getLogsByGameType(gameType: GameType): Flow<List<LaunchLog>>
    fun getLaunchCountsByGameType(gameType: GameType): Flow<List<GameLaunchCount>>
    fun getAllGameLaunchCounts(): Flow<List<AllGameLaunchStats>>
    suspend fun deleteLogsByGameType(gameType: GameType)
    suspend fun deleteAllLogs()
}
