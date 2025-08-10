package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.FullGameHistoryList
import fr.antoinehory.divination.ui.common.defaultLogResultFormatter
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.GameGlobalShareEntry
import fr.antoinehory.divination.viewmodels.GameStatsData
import fr.antoinehory.divination.viewmodels.GameStatsViewModel
import fr.antoinehory.divination.viewmodels.GameStatsViewModelFactory
import fr.antoinehory.divination.viewmodels.StatItem
// Imports for the moved chart composables
import fr.antoinehory.divination.ui.common.charts.GameResultPieChart
import fr.antoinehory.divination.ui.common.charts.GlobalDistributionChart
import fr.antoinehory.divination.ui.common.charts.getStatItemColors
// java.util.Date is used in Preview
import java.util.Date

/**
 * Composable screen for displaying game statistics.
 * It can show global statistics for all games or detailed statistics for a [specificGameType].
 * Data is fetched and managed by [GameStatsViewModel].
 *
 * @param onNavigateBack Callback function to handle back navigation.
 * @param specificGameType An optional [GameType]. If provided, statistics for this specific game are shown.
 *                         If null, global statistics for all games are displayed.
 */
@Composable
fun GameStatsScreen(
    onNavigateBack: () -> Unit,
    specificGameType: GameType?
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository // Used by the ViewModel factory.

    // Instantiate ViewModel, passing the specificGameType to its factory.
    val viewModel: GameStatsViewModel = viewModel(
        factory = GameStatsViewModelFactory(application, launchLogRepository, specificGameType)
    )

    // Collect states from the ViewModel.
    val statsData by viewModel.statsData.collectAsState() // Core statistics data.
    val globalGameShares by viewModel.globalGameSharesData.collectAsState() // Data for global distribution chart.
    val fullHistory by viewModel.fullHistoryLogs.collectAsState() // Full game history (used for specific game view).

    AppScaffold(
        // Title is dynamic: shows "Loading..." or the actual title from statsData.
        title = statsData?.title ?: stringResource(id = R.string.stats_screen_title_global_loading),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        // Main content area, passing down the collected states.
        GameStatsContent(
            statsData = statsData,
            globalGameShares = globalGameShares,
            specificGameType = specificGameType,
            fullHistoryForSpecificGame = fullHistory,
            modifier = Modifier.padding(paddingValues) // Apply padding from Scaffold.
        )
    }
}

/**
 * Main content composable for the Game Statistics screen.
 * It displays charts, lists of statistics, and game history based on the provided data.
 * Shows a loading indicator if [statsData] is null.
 *
 * @param statsData The core [GameStatsData] (can be null during loading).
 * @param globalGameShares Data for the global game distribution chart.
 * @param specificGameType The [GameType] for which specific stats are shown, or null for global.
 * @param fullHistoryForSpecificGame List of [LaunchLog] entities for the specific game's history view.
 * @param modifier [Modifier] to be applied to the root Column of this content.
 */
@Composable
fun GameStatsContent(
    statsData: GameStatsData?,
    globalGameShares: List<GameGlobalShareEntry>,
    specificGameType: GameType?,
    fullHistoryForSpecificGame: List<fr.antoinehory.divination.data.database.entity.LaunchLog>,
    modifier: Modifier = Modifier
) {
    // Show loading indicator if data is not yet available.
    if (statsData == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // Horizontal padding for the content list.
    ) {
        // Display total plays if there are any.
        if (statsData.totalPlays > 0) {
            item {
                Text(
                    text = stringResource(R.string.stats_total_plays, statsData.totalPlays),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
        }

        // Display global distribution chart only in global stats view and if data exists.
        val chartShown = specificGameType == null && globalGameShares.isNotEmpty()
        if (chartShown) {
            item {
                Text(
                    text = stringResource(R.string.stats_global_distribution_chart_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = if (statsData.totalPlays > 0) 8.dp else 16.dp, bottom = 8.dp)
                )
                GlobalDistributionChart(chartEntries = globalGameShares)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) // Visual separator.
            }
        }

        // Handle cases where there are no statistical items (e.g., no results for a game).
        if (statsData.statItems.isEmpty()) {
            if (statsData.totalPlays == 0) { // If absolutely no plays recorded.
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize() // Fill available space in LazyColumn item.
                            .padding(top = if (chartShown) 16.dp else 0.dp), // Adjust padding if chart was shown.
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.stats_no_data_available),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // If viewing specific game stats and no items, show full history if available.
            if (specificGameType != null) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    FullGameHistoryList(
                        logs = fullHistoryForSpecificGame, // Will be empty if no data.
                        logResultFormatter = defaultLogResultFormatter(),
                        modifier = Modifier.heightIn(max = 300.dp) // Crucial to limit height in LazyColumn.
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            return@LazyColumn // Exit if there are no stat items to display further.
        }

        // Logic for displaying stat items, either grouped by game or for a single game.
        val displayItems = statsData.statItems
        val shouldGroupByGame = specificGameType == null // Group if in global stats view.

        if (shouldGroupByGame) {
            // Group stat items by game type for global view.
            val groupedByGame = displayItems.groupBy { it.gameType }
            groupedByGame.entries.forEachIndexed { groupIndex, (gameType, itemsInGroup) ->
                val itemColors = getStatItemColors(itemsInGroup, OrakniumGold, OrakniumBackground)
                val totalPlaysForThisGame = itemsInGroup.sumOf { it.count }
                item { // Section header for each game type.
                    Text(
                        text = LocalContext.current.getString(gameType.displayNameResourceId),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = if (chartShown || groupIndex > 0) 16.dp else 8.dp, // Adjust top padding.
                            bottom = 4.dp
                        )
                    )
                    Text( // Total plays for this specific game.
                        text = stringResource(R.string.stats_total_plays_for_game, totalPlaysForThisGame),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box( // Centered Pie Chart for this game.
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameResultPieChart(
                            statItems = itemsInGroup,
                            modifier = Modifier.size(170.dp)
                        )
                    }
                }
                // List individual stat items for this game group.
                items(itemsInGroup, key = { it.gameType.name + it.resultKey }) { statItem ->
                    StatRow(
                        statItem = statItem,
                        legendColor = itemColors[statItem.resultKey] // Pass color for legend.
                    )
                }
                // Add spacer between game groups, except for the last one.
                if (groupIndex < groupedByGame.size - 1) {
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        } else { // Displaying stats for a specific game (not grouped).
            val itemColors = getStatItemColors(displayItems, OrakniumGold, OrakniumBackground)
            item { // Pie chart for the specific game.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GameResultPieChart(
                        statItems = displayItems,
                        modifier = Modifier.size(170.dp)
                    )
                }
            }
            // List individual stat items for the specific game.
            items(displayItems, key = { it.gameType.name + it.resultKey }) { statItem ->
                StatRow(
                    statItem = statItem,
                    legendColor = itemColors[statItem.resultKey]
                )
            }

            // Display full game history for the specific game type.
            item {
                Spacer(modifier = Modifier.height(24.dp))
                FullGameHistoryList(
                    logs = fullHistoryForSpecificGame,
                    logResultFormatter = defaultLogResultFormatter(),
                    modifier = Modifier.heightIn(max = 300.dp) // Limit height.
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * A composable that displays a single row of statistical information (e.g., a game outcome).
 * It shows the result's display name, its count, its percentage, and an optional color legend.
 *
 * @param statItem The [StatItem] data to be displayed in this row.
 * @param modifier Optional [Modifier] to be applied to the root Column of the row.
 * @param legendColor An optional [Color] to display as a small square legend indicator
 *                    next to the result text. Useful for associating with chart segments.
 */
@Composable
fun StatRow(statItem: StatItem, modifier: Modifier = Modifier, legendColor: Color? = null) {
    Column(modifier = modifier) { // Each StatRow is a Column containing the Row and a Divider.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Vertical padding for the content of the row.
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display a colored box as a legend item if legendColor is provided.
            if (legendColor != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp) // Small square for the legend color.
                        .background(legendColor)
                )
                Spacer(Modifier.width(8.dp)) // Space between legend and text.
            }
            // Displayed result text (e.g., "Heads", "Win", "Answer X").
            Text(
                text = statItem.displayResult,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f) // Text takes available horizontal space.
            )
            // Count of this specific result.
            Text(
                text = stringResource(R.string.stats_count_format, statItem.count),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp) // Padding around the count.
            )
            // Percentage representation of this result.
            Text(
                text = stringResource(R.string.stats_percentage_format, statItem.percentage),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        HorizontalDivider() // Visual separator below each stat row.
    }
}

/**
 * Extension property for [GameType] to get its corresponding display name string resource ID.
 * This is used for dynamically setting titles or labels in the UI based on the game type.
 */
val GameType.displayNameResourceId: Int
    get() = when (this) {
        GameType.COIN_FLIP -> R.string.coin_flip_screen_title
        GameType.MAGIC_EIGHT_BALL -> R.string.magic_ball_screen_title
        GameType.DICE_ROLL -> R.string.dice_roll_screen_title
        GameType.ROCK_PAPER_SCISSORS -> R.string.rps_screen_title
        // Note: If GameType.DICE_SET is a distinct game mode with its own stats, it should be added here.
    }

// Previews
/**
 * Preview composable for the [GameStatsScreen] showing global statistics with a chart.
 * Uses sample data to simulate a populated screen.
 */
@Preview(showBackground = true, name = "Stats - Global with Chart")
@Composable
fun GameStatsScreenPreview_GlobalWithChart() {
    val previewStats = GameStatsData(
        title = "Statistiques Globales (Aperçu)", // This title is from ViewModel, AppScaffold uses dynamic title.
        totalPlays = 140,
        statItems = listOf(
            StatItem(GameType.COIN_FLIP, "HEADS", "Pile!", 30, 75.0f),
            StatItem(GameType.COIN_FLIP, "TAILS", "Face!", 10, 25.0f),
            StatItem(GameType.DICE_ROLL, "4", "4", 60, 100.0f), // Assuming only one outcome for dice for simplicity
            StatItem(GameType.ROCK_PAPER_SCISSORS, "ROCK", "Pierre", 20, 50f),
            StatItem(GameType.ROCK_PAPER_SCISSORS, "PAPER", "Feuille", 20, 50f)
        )
    )
    val previewGlobalShares = listOf(
        // Percentages should ideally be calculated based on totalPlays of each game vs grand total.
        // For simplicity, using illustrative fixed percentages here.
        GameGlobalShareEntry(GameType.DICE_ROLL, "Lancer de Dés", 60, (60f/140f)*100), // Corrected calculation
        GameGlobalShareEntry(GameType.COIN_FLIP, "Pile ou Face", 40, (40f/140f)*100),   // Corrected calculation
        GameGlobalShareEntry(GameType.ROCK_PAPER_SCISSORS, "Pierre...", 40, (40f/140f)*100) // Corrected calculation
    )
    DivinationAppTheme {
        AppScaffold(
            title = "Aperçu Stats avec Graph.", // Static title for preview clarity.
            canNavigateBack = false, onNavigateBack = {}
        ) { paddingValues ->
            GameStatsContent( // Directly use GameStatsContent for preview.
                statsData = previewStats,
                globalGameShares = previewGlobalShares,
                specificGameType = null, // Global view.
                fullHistoryForSpecificGame = emptyList(),
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Preview composable for the [GameStatsScreen] showing statistics for a specific game (Coin Flip).
 * Uses sample data for a single game type.
 */
@Preview(showBackground = true, name = "Stats - Specific Game (Coin Flip)")
@Composable
fun GameStatsScreenPreview_SpecificGame() {
    val previewStats = GameStatsData(
        title = "Statistiques : Pile ou Face", // Example specific title.
        totalPlays = 10, // Total plays for this specific game.
        statItems = listOf(
            StatItem(GameType.COIN_FLIP, "HEADS", "Pile!", 6, 60.0f),
            StatItem(GameType.COIN_FLIP, "TAILS", "Face!", 4, 40.0f)
        )
    )
    val previewFullHistory = listOf(
        // Using fully qualified name for LaunchLog as it's from a different package.
        fr.antoinehory.divination.data.database.entity.LaunchLog(timestamp = Date(System.currentTimeMillis() - 10000), gameType = GameType.COIN_FLIP, result = "HEADS"),
        fr.antoinehory.divination.data.database.entity.LaunchLog(timestamp = Date(System.currentTimeMillis() - 20000), gameType = GameType.COIN_FLIP, result = "TAILS"),
        fr.antoinehory.divination.data.database.entity.LaunchLog(timestamp = Date(System.currentTimeMillis() - 30000), gameType = GameType.COIN_FLIP, result = "HEADS")
    )
    DivinationAppTheme {
        AppScaffold(
            title = "Aperçu Stats (Spécifique)", // Static title for preview.
            canNavigateBack = true, onNavigateBack = {}
        ) { paddingValues ->
            GameStatsContent(
                statsData = previewStats,
                globalGameShares = emptyList(), // Not shown for specific game stats.
                specificGameType = GameType.COIN_FLIP,
                fullHistoryForSpecificGame = previewFullHistory,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Preview composable for the [GameStatsScreen] showing the state when no data is available.
 * Simulates a scenario with zero total plays and empty stat items.
 */
@Preview(showBackground = true, name = "Stats - No Data")
@Composable
fun GameStatsScreenPreview_NoData() {
    val previewStatsEmpty = GameStatsData(
        title = "Statistiques : Lancer de Dés", // Example title, content shows no data.
        totalPlays = 0,
        statItems = emptyList(),
        isEmpty = true // Assuming isEmpty is a property of GameStatsData, if not, this can be removed.
    )
    DivinationAppTheme {
        AppScaffold(
            title = "Aperçu Stats (Aucune Donnée)", // Static title for preview.
            canNavigateBack = true, onNavigateBack = {}
        ) { paddingValues ->
            GameStatsContent(
                statsData = previewStatsEmpty,
                globalGameShares = emptyList(),
                specificGameType = GameType.DICE_ROLL, // Example specific game type with no data.
                fullHistoryForSpecificGame = emptyList(),
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

