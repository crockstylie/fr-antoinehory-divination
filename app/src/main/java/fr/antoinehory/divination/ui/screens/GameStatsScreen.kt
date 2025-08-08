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
import fr.antoinehory.divination.ui.common.FullGameHistoryList // Importé depuis ui.common
import fr.antoinehory.divination.ui.common.defaultLogResultFormatter // Importé depuis ui.common (GameHistoryDisplay.kt)
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.GameGlobalShareEntry
import fr.antoinehory.divination.viewmodels.GameStatsData
import fr.antoinehory.divination.viewmodels.GameStatsViewModel
import fr.antoinehory.divination.viewmodels.GameStatsViewModelFactory
import fr.antoinehory.divination.viewmodels.StatItem
import android.graphics.Paint as AndroidPaint
import java.util.Date // Pour la Preview

// --- Fonctions utilitaires et pour les graphiques ---
fun getStatItemColors(
    statItems: List<StatItem>,
    startColor: Color,
    endColor: Color
): Map<String, Color> {
    if (statItems.isEmpty()) return emptyMap()
    val sortedItems = statItems.sortedByDescending { it.percentage }
    val colors = mutableMapOf<String, Color>()
    sortedItems.forEachIndexed { index, item ->
        val fraction = if (sortedItems.size > 1) {
            index.toFloat() / (sortedItems.size - 1).toFloat()
        } else {
            0.5f
        }
        colors[item.resultKey] = lerp(startColor, endColor, fraction)
    }
    return colors
}

@Composable
fun GameResultPieChart(
    statItems: List<StatItem>,
    modifier: Modifier = Modifier,
    startColor: Color = OrakniumGold,
    endColor: Color = OrakniumBackground,
    borderColor: Color = OrakniumGold,
    borderWidthDp: Dp = 1.5.dp
) {
    if (statItems.isEmpty()) return

    val sortedItems = statItems.sortedByDescending { it.percentage }
    val totalCount = sortedItems.sumOf { it.count }.toFloat().coerceAtLeast(0.001f)
    val density = LocalDensity.current
    val borderWidthPx = with(density) { borderWidthDp.toPx() }

    Box(modifier = modifier
        .aspectRatio(1f)
        .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var currentStartAngle = -90f

            sortedItems.forEachIndexed { index, item ->
                val sweepAngle = if (totalCount > 0f) {
                    (item.count.toFloat() / totalCount) * 360f
                } else {
                    0f
                }
                val fraction = if (sortedItems.size > 1) {
                    index.toFloat() / (sortedItems.size - 1).toFloat()
                } else { 0.5f }
                val sliceColor = lerp(startColor, endColor, fraction)

                if (sweepAngle > 0f) {
                    drawArc(
                        color = sliceColor,
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }
                currentStartAngle += sweepAngle
            }

            if (borderWidthPx > 0f) {
                drawArc(
                    color = borderColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = borderWidthPx)
                )
            }
        }
    }
}

// --- Écran principal et ses composants ---

@Composable
fun GameStatsScreen(
    onNavigateBack: () -> Unit,
    specificGameType: GameType?
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    val viewModel: GameStatsViewModel = viewModel(
        factory = GameStatsViewModelFactory(application, launchLogRepository, specificGameType)
    )

    val statsData by viewModel.statsData.collectAsState()
    val globalGameShares by viewModel.globalGameSharesData.collectAsState()
    val fullHistory by viewModel.fullHistoryLogs.collectAsState()

    AppScaffold(
        title = statsData?.title ?: stringResource(id = R.string.stats_screen_title_global_loading),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        GameStatsContent(
            statsData = statsData,
            globalGameShares = globalGameShares,
            specificGameType = specificGameType,
            fullHistoryForSpecificGame = fullHistory,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun GameStatsContent(
    statsData: GameStatsData?,
    globalGameShares: List<GameGlobalShareEntry>,
    specificGameType: GameType?,
    fullHistoryForSpecificGame: List<fr.antoinehory.divination.data.database.entity.LaunchLog>,
    modifier: Modifier = Modifier
) {
    if (statsData == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (statsData.totalPlays > 0) {
            item {
                Text(
                    text = stringResource(R.string.stats_total_plays, statsData.totalPlays),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
        }

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
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        if (statsData.statItems.isEmpty()) {
            if (statsData.totalPlays == 0) { // Uniquement si aucun jeu n'a été joué du tout
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize() // Prend toute la place restante dans la LazyColumn
                            .padding(top = if (chartShown) 16.dp else 0.dp),
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
            // S'il n'y a pas de statItems, il n'y a rien d'autre à afficher dans cette section.
            // Si specificGameType != null et fullHistoryForSpecificGame est vide,
            // FullGameHistoryList affichera son propre message "pas d'historique".
            // Nous devons toujours inclure la section de l'historique si c'est un écran spécifique.
            if (specificGameType != null) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    FullGameHistoryList(
                        logs = fullHistoryForSpecificGame, // Sera vide si aucune donnée
                        logResultFormatter = defaultLogResultFormatter(),
                        modifier = Modifier.heightIn(max = 300.dp) // Crucial
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            return@LazyColumn
        }


        // Cette section ne sera atteinte que si statsData.statItems n'est PAS vide.
        val displayItems = statsData.statItems
        val shouldGroupByGame = specificGameType == null

        if (shouldGroupByGame) { // Cas Global
            val groupedByGame = displayItems.groupBy { it.gameType }
            groupedByGame.entries.forEachIndexed { groupIndex, (gameType, itemsInGroup) ->
                // Pas besoin de vérifier itemsInGroup.isNotEmpty() ici car displayItems n'est pas vide
                val itemColors = getStatItemColors(itemsInGroup, OrakniumGold, OrakniumBackground)
                val totalPlaysForThisGame = itemsInGroup.sumOf { it.count }
                item {
                    Text(
                        text = LocalContext.current.getString(gameType.displayNameResourceId),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            top = if (chartShown || groupIndex > 0) 16.dp else 8.dp,
                            bottom = 4.dp
                        )
                    )
                    Text(
                        text = stringResource(R.string.stats_total_plays_for_game, totalPlaysForThisGame),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
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
                items(itemsInGroup, key = { it.gameType.name + it.resultKey }) { statItem ->
                    StatRow(
                        statItem = statItem,
                        legendColor = itemColors[statItem.resultKey]
                    )
                }
                if (groupIndex < groupedByGame.size - 1) {
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        } else { // Cas Spécifique (displayItems ne sont que pour ce jeu et ne sont pas vides)
            val itemColors = getStatItemColors(displayItems, OrakniumGold, OrakniumBackground)
            item { // PieChart pour le jeu spécifique
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
            items(displayItems, key = { it.gameType.name + it.resultKey }) { statItem ->
                StatRow(
                    statItem = statItem,
                    legendColor = itemColors[statItem.resultKey]
                )
            }

            // Affichage de l'historique complet pour le jeu spécifique
            // (specificGameType est non-null ici car nous sommes dans le 'else')
            item { // L'item est toujours créé, FullGameHistoryList gère l'affichage si la liste est vide.
                Spacer(modifier = Modifier.height(24.dp))
                FullGameHistoryList(
                    logs = fullHistoryForSpecificGame,
                    logResultFormatter = defaultLogResultFormatter(),
                    modifier = Modifier.heightIn(max = 300.dp) // CRUCIAL
                )
                Spacer(modifier = Modifier.height(16.dp)) // Espace en bas après l'historique
            }
        }
    }
}

@Composable
fun StatRow(statItem: StatItem, modifier: Modifier = Modifier, legendColor: Color? = null) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (legendColor != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(legendColor)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = statItem.displayResult,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.stats_count_format, statItem.count),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = stringResource(R.string.stats_percentage_format, statItem.percentage),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        HorizontalDivider()
    }
}

val GameType.displayNameResourceId: Int
    get() = when (this) {
        GameType.COIN_FLIP -> R.string.coin_flip_screen_title
        GameType.MAGIC_EIGHT_BALL -> R.string.magic_ball_screen_title
        GameType.DICE_ROLL -> R.string.dice_roll_screen_title
        GameType.ROCK_PAPER_SCISSORS -> R.string.rps_screen_title
    }

@Composable
fun GlobalDistributionChart(chartEntries: List<GameGlobalShareEntry>, modifier: Modifier = Modifier) {
    if (chartEntries.isEmpty()) {
        return
    }
    val density = LocalDensity.current
    val barColor = MaterialTheme.colorScheme.primary

    val gameNameTextColor = MaterialTheme.colorScheme.onSurface
    val gameNameTextPaint = remember(gameNameTextColor, density) {
        AndroidPaint().apply {
            color = gameNameTextColor.toArgb()
            textAlign = AndroidPaint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
        }
    }

    val valueTextColorInBar = OrakniumBackground
    val valueTextPaint = remember(valueTextColorInBar, density) {
        AndroidPaint().apply {
            color = valueTextColorInBar.toArgb()
            textAlign = AndroidPaint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }

    val bottomPaddingForGameName = with(density) { 25.dp.toPx() }
    val topPadding = with(density) { 8.dp.toPx() }
    val spacingBetweenBars = with(density) { 8.dp.toPx() }
    val valueTextMarginFromBarBottomPx = with(density) { 4.dp.toPx() }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(220.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val numBars = chartEntries.size

        val totalSpacing = spacingBetweenBars * (numBars - 1).coerceAtLeast(0)
        val barWidth = if (numBars > 0) (canvasWidth - totalSpacing) / numBars else 0f

        if (barWidth <= 0f && numBars > 0) return@Canvas

        val maxPercentage = chartEntries.maxOfOrNull { it.sharePercentage }?.takeIf { it > 0f } ?: 1f
        val chartAreaHeight = canvasHeight - topPadding - bottomPaddingForGameName

        if (chartAreaHeight <= 0f) return@Canvas

        val textBounds = android.graphics.Rect()

        chartEntries.forEachIndexed { index, entry ->
            val barHeight = (entry.sharePercentage / maxPercentage) * chartAreaHeight
            val barLeft = index * (barWidth + spacingBetweenBars)
            val barTopY = canvasHeight - bottomPaddingForGameName - barHeight
            val barBottomY = canvasHeight - bottomPaddingForGameName

            drawRect(
                color = barColor,
                topLeft = Offset(x = barLeft, y = barTopY),
                size = Size(barWidth.coerceAtLeast(0f), barHeight.coerceAtLeast(0f))
            )

            val valueText = entry.totalPlaysForGame.toString()
            valueTextPaint.getTextBounds(valueText, 0, valueText.length, textBounds)
            val valueTextHeight = textBounds.height()

            if (barHeight > valueTextHeight + (2 * valueTextMarginFromBarBottomPx)) {
                val valueTextBaselineY = barBottomY - valueTextMarginFromBarBottomPx - textBounds.bottom
                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    barLeft + barWidth / 2,
                    valueTextBaselineY,
                    valueTextPaint
                )
            }

            val gameNameText = entry.gameDisplayName
            val gameNameBaselineY = canvasHeight - bottomPaddingForGameName + with(density) { 15.dp.toPx() }

            drawContext.canvas.nativeCanvas.drawText(
                gameNameText,
                barLeft + barWidth / 2,
                gameNameBaselineY,
                gameNameTextPaint
            )
        }
    }
}

// Previews
@Preview(showBackground = true, name = "Stats - Global with Chart")
@Composable
fun GameStatsScreenPreview_GlobalWithChart() {
    val previewStats = GameStatsData(
        title = "Statistiques Globales (Aperçu)",
        totalPlays = 140,
        statItems = listOf(
            StatItem(GameType.COIN_FLIP, "HEADS", "Pile!", 30, 75.0f),
            StatItem(GameType.COIN_FLIP, "TAILS", "Face!", 10, 25.0f),
            StatItem(GameType.DICE_ROLL, "4", "4", 60, 100.0f),
            StatItem(GameType.ROCK_PAPER_SCISSORS, "ROCK", "Pierre", 20, 50f),
            StatItem(GameType.ROCK_PAPER_SCISSORS, "PAPER", "Feuille", 20, 50f)
        )
    )
    val previewGlobalShares = listOf(
        GameGlobalShareEntry(GameType.DICE_ROLL, "Lancer de Dés", 60, (60f/140f)*100),
        GameGlobalShareEntry(GameType.COIN_FLIP, "Pile ou Face", 40, (40f/140f)*100),
        GameGlobalShareEntry(GameType.ROCK_PAPER_SCISSORS, "Pierre...", 40, (40f/140f)*100)
    )
    DivinationAppTheme {
        AppScaffold(title = "Aperçu Stats avec Graph.", canNavigateBack = false, onNavigateBack = {}) { paddingValues ->
            GameStatsContent(
                statsData = previewStats,
                globalGameShares = previewGlobalShares,
                specificGameType = null,
                fullHistoryForSpecificGame = emptyList(),
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview(showBackground = true, name = "Stats - Specific Game (Coin Flip)")
@Composable
fun GameStatsScreenPreview_SpecificGame() {
    val previewStats = GameStatsData(
        title = "Statistiques : Pile ou Face",
        totalPlays = 10,
        statItems = listOf(
            StatItem(GameType.COIN_FLIP, "HEADS", "Pile!", 6, 60.0f),
            StatItem(GameType.COIN_FLIP, "TAILS", "Face!", 4, 40.0f)
        )
    )
    val previewFullHistory = listOf(
        fr.antoinehory.divination.data.database.entity.LaunchLog(timestamp = Date(System.currentTimeMillis() - 10000), gameType = GameType.COIN_FLIP, result = "HEADS"),
        fr.antoinehory.divination.data.database.entity.LaunchLog(timestamp = Date(System.currentTimeMillis() - 20000), gameType = GameType.COIN_FLIP, result = "TAILS"),
        fr.antoinehory.divination.data.database.entity.LaunchLog(timestamp = Date(System.currentTimeMillis() - 30000), gameType = GameType.COIN_FLIP, result = "HEADS")
    )
    DivinationAppTheme {
        AppScaffold(title = "Aperçu Stats (Spécifique)", canNavigateBack = true, onNavigateBack = {}) { paddingValues ->
            GameStatsContent(
                statsData = previewStats,
                globalGameShares = emptyList(),
                specificGameType = GameType.COIN_FLIP,
                fullHistoryForSpecificGame = previewFullHistory,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Preview(showBackground = true, name = "Stats - No Data")
@Composable
fun GameStatsScreenPreview_NoData() {
    val previewStatsEmpty = GameStatsData(
        title = "Statistiques : Lancer de Dés",
        totalPlays = 0,
        statItems = emptyList(),
        isEmpty = true
    )
    DivinationAppTheme {
        AppScaffold(title = "Aperçu Stats (Aucune Donnée)", canNavigateBack = true, onNavigateBack = {}) { paddingValues ->
            GameStatsContent(
                statsData = previewStatsEmpty,
                globalGameShares = emptyList(),
                specificGameType = GameType.DICE_ROLL,
                fullHistoryForSpecificGame = emptyList(),
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

