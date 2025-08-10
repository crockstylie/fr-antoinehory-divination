package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.GameType
// import fr.antoinehory.divination.data.model.LaunchLog // Non utilisé directement ici après refactor
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Data class pour représenter une ligne de statistique (EXISTANTE)
data class StatItem(
    val gameType: GameType, // Pour savoir à quel jeu appartient ce résultat
    val resultKey: String, // La clé brute stockée (ex: "HEADS", "0", "ROCK")
    val displayResult: String, // La chaîne à afficher à l'utilisateur (ex: "Pile!", "It is certain.", "Rock!")
    val count: Int,
    val percentage: Float
)

// Data class pour encapsuler toutes les statistiques d'un écran (EXISTANTE)
data class GameStatsData(
    val title: String, // Titre de l'écran (ex: "Statistiques : Pile ou Face" ou "Statistiques Globales")
    val totalPlays: Int,
    val statItems: List<StatItem>,
    val isEmpty: Boolean = statItems.isEmpty() && totalPlays == 0
)

// NOUVELLE Data class pour les données du graphique à barres global
data class GameGlobalShareEntry(
    val gameType: GameType,
    val gameDisplayName: String,
    val sharePercentage: Float,
    val launchCount: Int // Ajout du nombre de lancers pour information potentielle
)

class GameStatsViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val specificGameType: GameType? // Null pour les stats globales
) : AndroidViewModel(application) {

    private val _statsData = MutableStateFlow<GameStatsData?>(null)
    val statsData: StateFlow<GameStatsData?> = _statsData.asStateFlow()

    // NOUVELLE StateFlow pour les données du graphique à barres global
    private val _globalGameSharesData = MutableStateFlow<List<GameGlobalShareEntry>>(emptyList())
    val globalGameSharesData: StateFlow<List<GameGlobalShareEntry>> = _globalGameSharesData.asStateFlow()

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
                val relevantLogs = if (specificGameType != null) {
                    allLogs.filter { it.gameType == specificGameType }
                } else {
                    allLogs
                }

                val totalPlaysForCurrentView = relevantLogs.size // Renommé pour clarté
                val groupedResults = relevantLogs.groupBy { it.gameType to it.result }

                val statItemList = mutableListOf<StatItem>()
                groupedResults.forEach { (gameTypeAndResultKey, logs) ->
                    val gameType = gameTypeAndResultKey.first
                    val resultKey = gameTypeAndResultKey.second
                    val count = logs.size
                    val percentageBase = if (specificGameType != null) {
                        relevantLogs.filter { it.gameType == gameType }.size
                    } else {
                        relevantLogs.filter { it.gameType == gameType }.size
                    }
                    val percentage = if (percentageBase > 0) (count.toFloat() / percentageBase.toFloat()) * 100 else 0f
                    val displayResult = getDisplayStringForResult(gameType, resultKey)
                    statItemList.add(
                        StatItem(gameType, resultKey, displayResult, count, percentage)
                    )
                }
                statItemList.sortWith(compareBy({ it.gameType.name }, { -it.count }, { it.displayResult }))

                _statsData.value = GameStatsData(
                    title = screenTitle,
                    totalPlays = totalPlaysForCurrentView,
                    statItems = statItemList.toList()
                )

                // CALCUL POUR LE GRAPHIQUE GLOBAL (seulement si pas de jeu spécifique)
                if (specificGameType == null && allLogs.isNotEmpty()) {
                    val totalGlobalPlays = allLogs.size.toFloat() // Base pour le pourcentage global
                    val shares = allLogs
                        .groupBy { it.gameType }
                        .map { (gameType, logs) ->
                            val gameLaunchCount = logs.size
                            GameGlobalShareEntry(
                                gameType = gameType,
                                gameDisplayName = getGameDisplayName(gameType),
                                sharePercentage = if (totalGlobalPlays > 0) (gameLaunchCount / totalGlobalPlays) * 100 else 0f,
                                launchCount = gameLaunchCount
                            )
                        }
                        .sortedByDescending { it.launchCount } // Optionnel: trier par nombre de lancers
                    _globalGameSharesData.value = shares
                } else {
                    _globalGameSharesData.value = emptyList() // Vider si jeu spécifique ou pas de logs
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
