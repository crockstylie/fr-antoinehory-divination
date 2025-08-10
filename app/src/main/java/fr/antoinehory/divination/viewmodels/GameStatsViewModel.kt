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

/**
 * Represents a single statistical item for a game result.
 *
 * @property gameType The type of game this stat belongs to.
 * @property resultKey The raw key identifying the specific result (e.g., "HEADS", "Rolled D6 Set").
 * @property displayResult The user-friendly, localized string representing the result.
 * @property count The number of times this specific result occurred.
 * @property percentage The percentage of this result's occurrence relative to the total plays for its game type.
 */
data class StatItem(
    val gameType: GameType,
    val resultKey: String,
    val displayResult: String,
    val count: Int,
    val percentage: Float
)

/**
 * Encapsulates all data required to display game statistics on a screen.
 *
 * @property title The title for the statistics screen (e.g., "Global Stats", "Coin Flip Stats").
 * @property totalPlays The total number of plays for the context (either global or for a specific game).
 * @property statItems A list of [StatItem] objects detailing individual result statistics.
 * @property isEmpty Convenience flag, true if there are no stat items and no total plays.
 */
data class GameStatsData(
    val title: String,
    val totalPlays: Int,
    val statItems: List<StatItem>,
    val isEmpty: Boolean = statItems.isEmpty() && totalPlays == 0
)

/**
 * Represents the share of plays for a specific game type in the global statistics.
 *
 * @property gameType The type of game.
 * @property gameDisplayName The user-friendly, localized name of the game.
 * @property totalPlaysForGame The total number of times this specific game was played.
 * @property sharePercentage The percentage of this game's plays relative to all games played.
 */
data class GameGlobalShareEntry(
    val gameType: GameType,
    val gameDisplayName: String,
    val totalPlaysForGame: Int,
    val sharePercentage: Float
)

/**
 * ViewModel responsible for loading, processing, and providing game statistics.
 *
 * It can display either global statistics for all games or detailed statistics for a
 * specific game type. It interacts with [LaunchLogRepository] to fetch game data.
 *
 * @param application The application context, used for accessing resources (e.g., strings).
 * @param launchLogRepository The repository for accessing launch log data.
 * @param specificGameType If not null, the ViewModel will load stats specifically for this [GameType].
 *                         If null, global statistics for all games will be loaded.
 */
class GameStatsViewModel(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val specificGameType: GameType?
) : AndroidViewModel(application) {

    /**
     * Internal [MutableStateFlow] for the processed game statistics data.
     */
    private val _statsData = MutableStateFlow<GameStatsData?>(null)
    /**
     * Public [StateFlow] exposing the [GameStatsData] to be displayed on the UI.
     * Null if data is not yet loaded.
     */
    val statsData: StateFlow<GameStatsData?> = _statsData.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the list of global game share entries.
     * Only populated when viewing global statistics ([specificGameType] is null).
     */
    private val _globalGameSharesData = MutableStateFlow<List<GameGlobalShareEntry>>(emptyList())
    /**
     * Public [StateFlow] exposing the list of [GameGlobalShareEntry] objects.
     * Used to show the distribution of plays among different games in the global view.
     */
    val globalGameSharesData: StateFlow<List<GameGlobalShareEntry>> = _globalGameSharesData.asStateFlow()

    /**
     * Internal [MutableStateFlow] for the complete list of launch logs for a specific game type.
     * Only populated when viewing statistics for a specific game ([specificGameType] is not null).
     */
    private val _fullHistoryLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    /**
     * Public [StateFlow] exposing the list of [LaunchLog] objects for a specific game's history.
     */
    val fullHistoryLogs: StateFlow<List<LaunchLog>> = _fullHistoryLogs.asStateFlow()

    /**
     * Initializes the ViewModel by triggering the loading of statistics.
     */
    init {
        loadStats()
    }

    /**
     * Loads and processes game statistics from the [launchLogRepository].
     *
     * This function determines the screen title based on whether global or specific game stats are requested.
     * It fetches all launch logs, then filters and groups them to calculate:
     * - Individual [StatItem]s for each game result, including counts and percentages.
     * - Special handling is applied for [GameType.DICE_ROLL] to extract dice set names from the result string.
     * - The overall [GameStatsData] containing the title, total plays, and sorted list of stat items.
     *
     * If global statistics are being viewed ([specificGameType] is null):
     * - It calculates [GameGlobalShareEntry] for each game type.
     * - The full history log list is cleared.
     *
     * If statistics for a specific game are being viewed ([specificGameType] is not null):
     * - The full history log list for that game is populated and sorted by timestamp.
     * - The global game shares list is cleared.
     */
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
                            // Extracts the dice set name (e.g., "D6 Set", "Custom Dice") from the result string.
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
                                    displayResult = getDisplayStringForResult(GameType.DICE_ROLL, setName),
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

                // Sort stats by game type name, then by count (descending), then by display result.
                statItemList.sortWith(compareBy({ it.gameType.name }, { -it.count }, { it.displayResult }))

                val totalPlaysForStatsData = if (specificGameType != null) logsToProcessForStatItems.size else allLogs.size

                _statsData.value = GameStatsData(
                    title = screenTitle,
                    totalPlays = totalPlaysForStatsData,
                    statItems = statItemList.toList()
                )

                // Populate global shares or specific game history based on context
                if (specificGameType == null) { // Global stats view
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
                    _fullHistoryLogs.value = emptyList() // Clear specific history in global view
                } else { // Specific game stats view
                    _fullHistoryLogs.value = logsToProcessForStatItems.sortedByDescending { it.timestamp }
                    _globalGameSharesData.value = emptyList() // Clear global shares in specific view
                }
            }
        }
    }

    /**
     * Retrieves the localized display name for a given [GameType].
     *
     * @param gameType The [GameType] for which to get the display name.
     * @return The localized string representing the game's name.
     */
    private fun getGameDisplayName(gameType: GameType): String {
        val resources = application.resources
        return when (gameType) {
            GameType.COIN_FLIP -> resources.getString(R.string.coin_flip_screen_title)
            GameType.MAGIC_EIGHT_BALL -> resources.getString(R.string.magic_ball_screen_title)
            GameType.DICE_ROLL -> resources.getString(R.string.dice_roll_screen_title)
            GameType.ROCK_PAPER_SCISSORS -> resources.getString(R.string.rps_screen_title)
        }
    }

    /**
     * Retrieves a localized, user-friendly display string for a given game result.
     *
     * For most games, it maps a result key (e.g., "HEADS", "ROCK") to a localized string.
     * For [GameType.MAGIC_EIGHT_BALL], it attempts to look up answers from a string array using an index
     * or provides a fallback display.
     * For [GameType.DICE_ROLL], the result key (dice set name) is usually displayed as is.
     *
     * @param gameType The [GameType] of the result.
     * @param resultKey The raw key identifying the result.
     * @return A localized string suitable for display to the user.
     */
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
                    possibleAnswers.getOrNull(index) ?: resultKey // Fallback to key if index is out of bounds
                } else if (resultKey == "FALLBACK") { // Handle a specific "FALLBACK" key
                    val fallbackDisplay = resources.getString(R.string.magic_ball_default_answer_if_empty)
                    // Ensure fallback string is not blank, otherwise use a generic stats fallback.
                    if (fallbackDisplay.isNotBlank()) fallbackDisplay else application.getString(R.string.stats_fallback_answer_display)
                } else {
                    resultKey // If not an index or "FALLBACK", return the key itself
                }
            }
            GameType.DICE_ROLL -> {
                // For dice rolls, the resultKey is often the dice set name, which is already user-friendly.
                resultKey
            }
            GameType.ROCK_PAPER_SCISSORS -> when (resultKey) {
                // Assumes RPSOutcome.name is used as the resultKey
                RPSOutcome.ROCK.name -> resources.getString(R.string.rps_result_rock)
                RPSOutcome.PAPER.name -> resources.getString(R.string.rps_result_paper)
                RPSOutcome.SCISSORS.name -> resources.getString(R.string.rps_result_scissors)
                else -> resultKey
            }
            // else -> resultKey // Default case for any other game types not explicitly handled
        }
    }
}

/**
 * Factory for creating instances of [GameStatsViewModel].
 *
 * This factory is necessary because [GameStatsViewModel] has constructor dependencies
 * ([Application], [LaunchLogRepository], [GameType]?) that need to be provided during ViewModel creation.
 *
 * @param application The application context.
 * @param launchLogRepository The repository for launch log data.
 * @param specificGameType The specific [GameType] for which to show stats, or null for global stats.
 */
class GameStatsViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository,
    private val specificGameType: GameType?
) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `modelClass`.
     *
     * @param modelClass A class whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not assignable from [GameStatsViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameStatsViewModel(application, launchLogRepository, specificGameType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for GameStatsViewModelFactory")
    }
}