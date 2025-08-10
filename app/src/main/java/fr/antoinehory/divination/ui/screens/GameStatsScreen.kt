package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import android.graphics.Paint as AndroidPaint // Aliasing to avoid potential naming conflicts
import java.util.Date // Used in Preview

/**
 * Utility function to generate a map of colors for a list of [StatItem]s.
 * Colors are interpolated between a [startColor] and [endColor] based on the item's
 * percentage, after sorting the items by percentage in descending order.
 * This is typically used for assigning consistent colors to pie chart segments and their legends.
 *
 * @param statItems The list of [StatItem] objects to generate colors for.
 * @param startColor The color used for the item with the highest percentage (or for a single item).
 * @param endColor The color used for the item with the lowest percentage.
 * @return A map where keys are [StatItem.resultKey] and values are the calculated [Color].
 *         Returns an empty map if [statItems] is empty.
 */
fun getStatItemColors(
    statItems: List<StatItem>,
    startColor: Color,
    endColor: Color
): Map<String, Color> {
    if (statItems.isEmpty()) return emptyMap()
    // Sort items by percentage to ensure consistent color mapping based on rank.
    val sortedItems = statItems.sortedByDescending { it.percentage }
    val colors = mutableMapOf<String, Color>()
    sortedItems.forEachIndexed { index, item ->
        // Calculate fraction for lerp: 0 for first (highest %), 1 for last (lowest %).
        // If only one item, fraction is 0.5 to pick a color in the middle (or startColor if start=end).
        val fraction = if (sortedItems.size > 1) {
            index.toFloat() / (sortedItems.size - 1).toFloat()
        } else {
            0.5f // For a single item, use a mid-point or the startColor.
        }
        colors[item.resultKey] = lerp(startColor, endColor, fraction)
    }
    return colors
}

/**
 * A composable that draws a pie chart based on a list of [StatItem]s.
 * Each slice of the pie represents a [StatItem], with its size proportional to [StatItem.count].
 * Colors for the slices are interpolated between [startColor] and [endColor].
 * An optional border can be drawn around the pie chart.
 *
 * @param statItems The list of [StatItem] objects to represent in the pie chart.
 * @param modifier [Modifier] to be applied to the chart's container Box.
 * @param startColor The color for the largest slice or the first in the sorted list.
 * @param endColor The color for the smallest slice or the last in the sorted list.
 * @param borderColor The color of the border drawn around the pie chart.
 * @param borderWidthDp The width of the border in Dp.
 */
@Composable
fun GameResultPieChart(
    statItems: List<StatItem>,
    modifier: Modifier = Modifier,
    startColor: Color = OrakniumGold,
    endColor: Color = OrakniumBackground,
    borderColor: Color = OrakniumGold,
    borderWidthDp: Dp = 1.5.dp
) {
    if (statItems.isEmpty()) return // Don't draw if there's no data.

    // Sort items by percentage for consistent color assignment if colors are generated by rank.
    // Here, colors are passed or generated externally, but sorting ensures a predictable drawing order.
    val sortedItems = statItems.sortedByDescending { it.percentage }
    val totalCount = sortedItems.sumOf { it.count }.toFloat().coerceAtLeast(0.001f) // Ensure totalCount is not zero.
    val density = LocalDensity.current
    val borderWidthPx = with(density) { borderWidthDp.toPx() } // Convert Dp to Px.

    Box(modifier = modifier
        .aspectRatio(1f) // Maintain a square aspect ratio for the pie chart.
        .padding(8.dp) // Padding around the canvas.
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var currentStartAngle = -90f // Start drawing from the top.

            sortedItems.forEachIndexed { index, item ->
                val sweepAngle = if (totalCount > 0f) {
                    (item.count.toFloat() / totalCount) * 360f // Calculate sweep angle for the slice.
                } else {
                    0f
                }
                // Calculate color fraction for interpolation (similar to getStatItemColors).
                val fraction = if (sortedItems.size > 1) {
                    index.toFloat() / (sortedItems.size - 1).toFloat()
                } else { 0.5f }
                val sliceColor = lerp(startColor, endColor, fraction)

                if (sweepAngle > 0f) {
                    drawArc(
                        color = sliceColor,
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true // Draw slices towards the center.
                    )
                }
                currentStartAngle += sweepAngle // Update start angle for the next slice.
            }

            // Draw an outer border for the pie chart if borderWidthPx > 0.
            if (borderWidthPx > 0f) {
                drawArc(
                    color = borderColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false, // Don't fill the center for the border.
                    style = Stroke(width = borderWidthPx) // Draw as a stroke.
                )
            }
        }
    }
}

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

/**
 * Composable function that draws a global distribution bar chart.
 * This chart visualizes the share of total plays for each game type within the application.
 *
 * @param chartEntries A list of [GameGlobalShareEntry] data points, where each entry
 *                     represents a game type, its display name, total plays, and share percentage.
 * @param modifier Optional [Modifier] to be applied to the Canvas where the chart is drawn.
 */
@Composable
fun GlobalDistributionChart(chartEntries: List<GameGlobalShareEntry>, modifier: Modifier = Modifier) {
    if (chartEntries.isEmpty()) { // Do not attempt to draw if there's no data.
        return
    }
    val density = LocalDensity.current
    val barColor = MaterialTheme.colorScheme.primary // Primary color for the bars.

    // Paint for drawing game names below each bar.
    val gameNameTextColor = MaterialTheme.colorScheme.onSurface
    val gameNameTextPaint = remember(gameNameTextColor, density) { // Remember paint to avoid recomposition.
        AndroidPaint().apply {
            color = gameNameTextColor.toArgb()
            textAlign = AndroidPaint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() } // Convert sp to px.
            isAntiAlias = true
        }
    }

    // Paint for drawing the total play count value inside each bar.
    val valueTextColorInBar = OrakniumBackground // Contrasting color for text on primary-colored bars.
    val valueTextPaint = remember(valueTextColorInBar, density) {
        AndroidPaint().apply {
            color = valueTextColorInBar.toArgb()
            textAlign = AndroidPaint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD) // Bold text.
        }
    }

    // Define padding and spacing values for chart layout in pixels.
    val bottomPaddingForGameName = with(density) { 25.dp.toPx() }
    val topPadding = with(density) { 8.dp.toPx() }
    val spacingBetweenBars = with(density) { 8.dp.toPx() }
    val valueTextMarginFromBarBottomPx = with(density) { 4.dp.toPx() } // Margin for text inside the bar.

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(220.dp) // Fixed height for the chart.
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val numBars = chartEntries.size

        // Calculate the width of each bar based on available canvas width and number of bars.
        val totalSpacing = spacingBetweenBars * (numBars - 1).coerceAtLeast(0)
        val barWidth = if (numBars > 0) (canvasWidth - totalSpacing) / numBars else 0f

        if (barWidth <= 0f && numBars > 0) return@Canvas // Not enough space to draw individual bars.

        // Determine the maximum percentage to scale bar heights appropriately.
        val maxPercentage = chartEntries.maxOfOrNull { it.sharePercentage }?.takeIf { it > 0f } ?: 1f
        val chartAreaHeight = canvasHeight - topPadding - bottomPaddingForGameName // Usable height for drawing bars.

        if (chartAreaHeight <= 0f) return@Canvas // Not enough vertical space for bars.

        val textBounds = android.graphics.Rect() // Used for measuring text dimensions.

        chartEntries.forEachIndexed { index, entry ->
            // Calculate bar dimensions and position.
            val barHeight = (entry.sharePercentage / maxPercentage) * chartAreaHeight
            val barLeft = index * (barWidth + spacingBetweenBars)
            val barTopY = canvasHeight - bottomPaddingForGameName - barHeight // Y-coordinate for the top of the bar.
            val barBottomY = canvasHeight - bottomPaddingForGameName // Y-coordinate for the bottom of the bar.

            // Draw the bar.
            drawRect(
                color = barColor,
                topLeft = Offset(x = barLeft, y = barTopY),
                size = Size(barWidth.coerceAtLeast(0f), barHeight.coerceAtLeast(0f)) // Ensure non-negative dimensions.
            )

            // Draw the total plays value inside the bar, if space permits.
            val valueText = entry.totalPlaysForGame.toString()
            valueTextPaint.getTextBounds(valueText, 0, valueText.length, textBounds) // Measure text.
            val valueTextHeight = textBounds.height()

            if (barHeight > valueTextHeight + (2 * valueTextMarginFromBarBottomPx)) { // Check if bar is tall enough for text.
                // Calculate baseline for vertically centering text within its allocated space inside the bar.
                val valueTextBaselineY = barBottomY - valueTextMarginFromBarBottomPx - textBounds.bottom
                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    barLeft + barWidth / 2, // Center text horizontally within the bar.
                    valueTextBaselineY,
                    valueTextPaint
                )
            }

            // Draw the game name below the bar.
            val gameNameText = entry.gameDisplayName
            // Calculate baseline for game name text, placing it below the chart area.
            val gameNameBaselineY = canvasHeight - bottomPaddingForGameName + with(density) { 15.dp.toPx() } // Offset for text below x-axis.

            drawContext.canvas.nativeCanvas.drawText(
                gameNameText,
                barLeft + barWidth / 2, // Center text horizontally with the bar.
                gameNameBaselineY,
                gameNameTextPaint
            )
        }
    }
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
        GameGlobalShareEntry(GameType.DICE_ROLL, "Lancer de Dés", 60, 42.8f), // 60/140
        GameGlobalShareEntry(GameType.COIN_FLIP, "Pile ou Face", 40, 28.5f),   // 40/140
        GameGlobalShareEntry(GameType.ROCK_PAPER_SCISSORS, "Pierre...", 40, 28.5f) // 40/140
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
        isEmpty = true // Assuming isEmpty is a property of GameStatsData.
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

