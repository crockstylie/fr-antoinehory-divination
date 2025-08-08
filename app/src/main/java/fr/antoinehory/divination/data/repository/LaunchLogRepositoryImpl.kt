// In data/repository/LaunchLogRepositoryImpl.kt
package fr.antoinehory.divination.data.repository

import fr.antoinehory.divination.data.database.dao.LaunchLogDao
import fr.antoinehory.divination.data.database.dao.GameLaunchCount
import fr.antoinehory.divination.data.database.dao.AllGameLaunchStats
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import kotlinx.coroutines.flow.Flow
import java.util.Date // S'assurer que l'import est correct si LaunchLog le prend en paramètre directement

class LaunchLogRepositoryImpl(
    private val launchLogDao: LaunchLogDao
) : LaunchLogRepository {

    override suspend fun insertLog(gameType: GameType, result: String) {
        val newLog = LaunchLog(
            gameType = gameType,
            result = result,
            timestamp = Date() // Le timestamp est généré ici ou dans le constructeur de LaunchLog
        )
        launchLogDao.insertLog(newLog)
    }

    override fun getAllLogs(): Flow<List<LaunchLog>> {
        return launchLogDao.getAllLogs()
    }

    override fun getLogsByGameType(gameType: GameType): Flow<List<LaunchLog>> {
        return launchLogDao.getLogsByGameType(gameType)
    }

    // Implémentation de la nouvelle fonction
    override fun getRecentLogsByGameType(gameType: GameType, count: Int): Flow<List<LaunchLog>> {
        return launchLogDao.getRecentLogsByGameType(gameType, count)
    }

    override fun getLaunchCountsByGameType(gameType: GameType): Flow<List<GameLaunchCount>> {
        return launchLogDao.getLaunchCountsByGameType(gameType)
    }

    override fun getAllGameLaunchCounts(): Flow<List<AllGameLaunchStats>> {
        return launchLogDao.getAllGameLaunchCounts()
    }

    override suspend fun deleteLogsByGameType(gameType: GameType) {
        launchLogDao.deleteLogsByGameType(gameType)
    }

    override suspend fun deleteAllLogs() {
        launchLogDao.deleteAllLogs()
    }
}