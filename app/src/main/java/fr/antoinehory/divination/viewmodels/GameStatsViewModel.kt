package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.entity.LaunchLog // AJOUT NÉCESSAIRE
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

    // AJOUT : StateFlow pour l'historique complet des lancements du jeu spécifique
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

            // Nous avons besoin de tous les logs pour les deux calculs (_statsData et _globalGameSharesData)
            // et potentiellement pour _fullHistoryLogs si specificGameType n'est pas null.
            launchLogRepository.getAllLogs().collectLatest { allLogs ->
                // Calcul pour _statsData (logique existante)
                val relevantLogsForStatsData = if (specificGameType != null) {
                    allLogs.filter { it.gameType == specificGameType }
                } else {
                    allLogs
                }
                val totalPlaysForCurrentScreen = relevantLogsForStatsData.size
                val groupedResults = relevantLogsForStatsData.groupBy { it.gameType to it.result }
                val statItemList = mutableListOf<StatItem>()

                groupedResults.forEach { (gameTypeAndResultKey, logs) ->
                    val gameType = gameTypeAndResultKey.first
                    val resultKey = gameTypeAndResultKey.second
                    val count = logs.size
                    // Pour le pourcentage dans StatItem, on se base sur les logs pertinents pour ce type de jeu sur l'écran actuel
                    val percentageBase = relevantLogsForStatsData.filter { it.gameType == gameType }.size
                    val percentage = if (percentageBase > 0) (count.toFloat() / percentageBase.toFloat()) * 100 else 0f
                    val displayResult = getDisplayStringForResult(gameType, resultKey)
                    statItemList.add(
                        StatItem(
                            gameType = gameType,
                            resultKey = resultKey,
                            displayResult = displayResult,
                            count = count,
                            percentage = percentage
                        )
                    )
                }
                statItemList.sortWith(compareBy({ it.gameType.name }, { -it.count }, { it.displayResult }))
                _statsData.value = GameStatsData(
                    title = screenTitle,
                    totalPlays = totalPlaysForCurrentScreen,
                    statItems = statItemList.toList()
                )

                // Calcul pour _globalGameSharesData (uniquement si stats globales - logique existante)
                if (specificGameType == null) {
                    val grandTotalAllPlays = allLogs.size
                    if (grandTotalAllPlays > 0) {
                        val shares = allLogs
                            .groupBy { it.gameType }
                            .map { (gameType, logsForGame) ->
                                val totalPlaysForThisGame = logsForGame.size
                                val sharePercentage = (totalPlaysForThisGame.toFloat() / grandTotalAllPlays.toFloat()) * 100
                                GameGlobalShareEntry(
                                    gameType = gameType,
                                    gameDisplayName = getGameDisplayName(gameType),
                                    totalPlaysForGame = totalPlaysForThisGame,
                                    sharePercentage = sharePercentage
                                )
                            }
                            .sortedByDescending { it.sharePercentage }
                        _globalGameSharesData.value = shares
                    } else {
                        _globalGameSharesData.value = emptyList()
                    }
                    _fullHistoryLogs.value = emptyList() // Pas d'historique spécifique si stats globales
                } else {
                    // C'est ici qu'on met à jour _fullHistoryLogs pour le jeu spécifique
                    // On peut réutiliser relevantLogsForStatsData qui sont déjà filtrés pour le specificGameType
                    // Et on les trie par timestamp descendant pour l'affichage de l'historique
                    _fullHistoryLogs.value = relevantLogsForStatsData.sortedByDescending { it.timestamp }
                    _globalGameSharesData.value = emptyList() // Pas de parts globales si stats spécifiques
                }
            }
        }
    }

    private fun getGameDisplayName(gameType: GameType): String {
        // ... (inchangé) ...
        val resources = application.resources
        return when (gameType) {
            GameType.COIN_FLIP -> resources.getString(R.string.coin_flip_screen_title)
            GameType.MAGIC_EIGHT_BALL -> resources.getString(R.string.magic_ball_screen_title)
            GameType.DICE_ROLL -> resources.getString(R.string.dice_roll_screen_title)
            GameType.ROCK_PAPER_SCISSORS -> resources.getString(R.string.rps_screen_title)
        }
    }

    private fun getDisplayStringForResult(gameType: GameType, resultKey: String): String {
        // ... (inchangé) ...
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
                val diceNumber = resultKey.toIntOrNull() ?: 0
                resources.getString(R.string.dice_result_format, diceNumber)
            }
            GameType.ROCK_PAPER_SCISSORS -> when (resultKey) {
                "ROCK" -> resources.getString(R.string.rps_result_rock)
                "PAPER" -> resources.getString(R.string.rps_result_paper)
                "SCISSORS" -> resources.getString(R.string.rps_result_scissors)
                else -> resultKey
            }
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

