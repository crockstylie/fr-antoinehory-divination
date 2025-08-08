package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Data class pour représenter une ligne de statistique (inchangée)
data class StatItem(
    val gameType: GameType,
    val resultKey: String,
    val displayResult: String,
    val count: Int,
    val percentage: Float
)

// Data class pour encapsuler toutes les statistiques d'un écran (inchangée)
data class GameStatsData(
    val title: String,
    val totalPlays: Int,
    val statItems: List<StatItem>,
    val isEmpty: Boolean = statItems.isEmpty() && totalPlays == 0
)

// Data class pour représenter la part de chaque jeu dans les statistiques globales (inchangée)
data class GameGlobalShareEntry(
    val gameType: GameType,
    val gameDisplayName: String,
    val totalPlaysForGame: Int,
    val sharePercentage: Float
)

class GameStatsViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val specificGameType: GameType? // Null pour les stats globales
) : AndroidViewModel(application) {

    private val _statsData = MutableStateFlow<GameStatsData?>(null)
    val statsData: StateFlow<GameStatsData?> = _statsData.asStateFlow()

    private val _globalGameSharesData = MutableStateFlow<List<GameGlobalShareEntry>>(emptyList())
    val globalGameSharesData: StateFlow<List<GameGlobalShareEntry>> = _globalGameSharesData.asStateFlow()

    private val _fullHistoryLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    val fullHistoryLogs: StateFlow<List<LaunchLog>> = _fullHistoryLogs.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val screenTitle = if (specificGameType != null) {
                application.getString(R.string.stats_screen_title_specific, getGameDisplayName(specificGameType))
            } else {
                application.getString(R.string.stats_screen_title_global)
            }

            launchLogRepository.getAllLogs().collectLatest { allLogs ->
                val statItemList = mutableListOf<StatItem>()

                val logsToProcessForStatItems = if (specificGameType != null) {
                    allLogs.filter { it.gameType == specificGameType }
                } else {
                    allLogs
                }

                val groupedByActualGameType = logsToProcessForStatItems.groupBy { it.gameType }

                groupedByActualGameType.forEach { (actualGameType, logsForThisGameType) ->
                    val totalPlaysForThisActualGameType = logsForThisGameType.size

                    if (actualGameType == GameType.DICE_ROLL) {
                        val diceSetCounts = mutableMapOf<String, Int>()
                        logsForThisGameType.forEach { log ->
                            val setNamePattern = "Rolled\\s(.*?)(?:\\.\\sTotal:|\\sTotal:)"
                            val match = Regex(setNamePattern).find(log.result)
                            val setName = match?.groups?.get(1)?.value?.trim() ?: application.getString(R.string.stats_unknown_dice_set)
                            diceSetCounts[setName] = (diceSetCounts[setName] ?: 0) + 1
                        }

                        diceSetCounts.forEach { (setName, count) ->
                            val percentage = if (totalPlaysForThisActualGameType > 0) (count.toFloat() / totalPlaysForThisActualGameType.toFloat()) * 100 else 0f
                            statItemList.add(
                                StatItem(
                                    gameType = GameType.DICE_ROLL,
                                    resultKey = setName,
                                    displayResult = getDisplayStringForResult(GameType.DICE_ROLL, setName), // getDisplayStringForResult gérera l'affichage du nom du set
                                    count = count,
                                    percentage = percentage
                                )
                            )
                        }
                    } else {
                        val resultsInThisGameType = logsForThisGameType.groupBy { it.result }
                        resultsInThisGameType.forEach { (resultKey, logsWithThisResult) ->
                            val count = logsWithThisResult.size
                            val percentage = if (totalPlaysForThisActualGameType > 0) (count.toFloat() / totalPlaysForThisActualGameType.toFloat()) * 100 else 0f
                            statItemList.add(
                                StatItem(
                                    gameType = actualGameType,
                                    resultKey = resultKey,
                                    displayResult = getDisplayStringForResult(actualGameType, resultKey),
                                    count = count,
                                    percentage = percentage
                                )
                            )
                        }
                    }
                }

                statItemList.sortWith(compareBy({ it.gameType.name }, { -it.count }, { it.displayResult }))

                val totalPlaysForStatsData = if (specificGameType != null) logsToProcessForStatItems.size else allLogs.size

                _statsData.value = GameStatsData(
                    title = screenTitle,
                    totalPlays = totalPlaysForStatsData,
                    statItems = statItemList.toList()
                )

                if (specificGameType == null) {
                    val grandTotalAllPlays = allLogs.size
                    if (grandTotalAllPlays > 0) {
                        _globalGameSharesData.value = allLogs
                            .groupBy { it.gameType }
                            .map { (gameType, logsForGame) ->
                                GameGlobalShareEntry(
                                    gameType = gameType,
                                    gameDisplayName = getGameDisplayName(gameType),
                                    totalPlaysForGame = logsForGame.size,
                                    sharePercentage = (logsForGame.size.toFloat() / grandTotalAllPlays.toFloat()) * 100
                                )
                            }
                            .sortedByDescending { it.sharePercentage }
                    } else {
                        _globalGameSharesData.value = emptyList()
                    }
                    _fullHistoryLogs.value = emptyList()
                } else {
                    _fullHistoryLogs.value = logsToProcessForStatItems.sortedByDescending { it.timestamp }
                    _globalGameSharesData.value = emptyList()
                }
            }
        }
    }

    private fun getGameDisplayName(gameType: GameType): String {
        val resources = application.resources
        return when (gameType) {
            GameType.COIN_FLIP -> resources.getString(R.string.coin_flip_screen_title)
            GameType.MAGIC_EIGHT_BALL -> resources.getString(R.string.magic_ball_screen_title)
            GameType.DICE_ROLL -> resources.getString(R.string.dice_roll_screen_title)
            GameType.ROCK_PAPER_SCISSORS -> resources.getString(R.string.rps_screen_title)
        }
    }

    private fun getDisplayStringForResult(gameType: GameType, resultKey: String): String {
        val resources = application.resources
        return when (gameType) {
            GameType.COIN_FLIP -> when (resultKey) {
                "HEADS" -> resources.getString(R.string.coin_flip_result_heads)
                "TAILS" -> resources.getString(R.string.coin_flip_result_tails)
                else -> resultKey
            }
            GameType.MAGIC_EIGHT_BALL -> {
                val index = resultKey.toIntOrNull()
                if (index != null) {
                    val possibleAnswers = resources.getStringArray(R.array.magic_ball_possible_answers)
                    possibleAnswers.getOrNull(index) ?: resultKey
                } else if (resultKey == "FALLBACK") {
                    val fallbackDisplay = resources.getString(R.string.magic_ball_default_answer_if_empty)
                    if (fallbackDisplay.isNotBlank()) fallbackDisplay else application.getString(R.string.stats_fallback_answer_display)
                } else {
                    resultKey
                }
            }
            GameType.DICE_ROLL -> {
                resultKey // Pour DICE_ROLL, resultKey est le nom du set. On le retourne directement.
            }
            GameType.ROCK_PAPER_SCISSORS -> when (resultKey) {
                // S'assurer que RPSOutcome est défini et que .name correspond à resultKey
                RPSOutcome.ROCK.name -> resources.getString(R.string.rps_result_rock)
                RPSOutcome.PAPER.name -> resources.getString(R.string.rps_result_paper)
                RPSOutcome.SCISSORS.name -> resources.getString(R.string.rps_result_scissors)
                else -> resultKey
            }
            else -> resultKey // Branche else pour exhaustivité
        }
    }
}

class GameStatsViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val specificGameType: GameType?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameStatsViewModel(application, launchLogRepository, specificGameType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for GameStatsViewModelFactory")
    }
}

// Assurez-vous que RPSOutcome est défini ou importé correctement. Par exemple:
// enum class RPSOutcome { ROCK, PAPER, SCISSORS }

