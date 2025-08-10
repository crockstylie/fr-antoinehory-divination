package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Pour MaterialTheme.colorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.GameGlobalShareEntry
import fr.antoinehory.divination.viewmodels.GameStatsData
import fr.antoinehory.divination.viewmodels.GameStatsViewModel
import fr.antoinehory.divination.viewmodels.GameStatsViewModelFactory
import fr.antoinehory.divination.viewmodels.StatItem

// Vico Imports - Alignés avec les exemples v2.1.3
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis // Corrigé
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis // Corrigé
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer // Pour ColumnProvider
import com.patrykandpatrick.vico.core.common.Dimensions // Pour le padding des TextComponent Vico

import kotlin.math.roundToInt


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

    AppScaffold(
        title = statsData?.title ?: stringResource(id = R.string.stats_screen_title_loading), // "Loading statistics..."
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        GameStatsContent(
            statsData = statsData,
            globalGameShares = globalGameShares,
            specificGameType = specificGameType,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun GameStatsContent(
    statsData: GameStatsData?,
    globalGameShares: List<GameGlobalShareEntry>,
    specificGameType: GameType?,
    modifier: Modifier = Modifier
) {
    if (statsData == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (statsData.isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.stats_no_data_available), // "No statistics data available."
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (specificGameType == null && globalGameShares.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.stats_global_share_chart_title), // "Plays distribution by game"
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )
                    GlobalGameShareBarChart(entries = globalGameShares)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            item {
                Text(
                    text = stringResource(R.string.stats_total_plays, statsData.totalPlays), // "Total plays: %d"
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp, top = if (specificGameType == null && globalGameShares.isNotEmpty()) 8.dp else 0.dp)
                )
            }

            val displayItems = statsData.statItems
            val shouldGroupByGame = specificGameType == null && displayItems.any { it.gameType != displayItems.firstOrNull()?.gameType }

            if (shouldGroupByGame) {
                val groupedByGame = displayItems.groupBy { it.gameType }
                groupedByGame.forEach { (gameType, items) ->
                    item {
                        Text(
                            text = LocalContext.current.getString(gameType.displayNameResourceId), // Uses existing game titles
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(items) { statItem ->
                        StatRow(statItem)
                    }
                }
            } else {
                items(displayItems) { statItem ->
                    StatRow(statItem)
                }
            }
        }
    }
}

@Composable
fun GlobalGameShareBarChart(entries: List<GameGlobalShareEntry>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(entries) {
        modelProducer.runTransaction {
            columnSeries {
                series(entries.map { it.sharePercentage })
            }
        }
    }

    // Signature du lambda : (CartesianMeasuringContext, valeurAxe: Double, positionAxe: AxisPosition) -> CharSequence
    // On ignore le premier et troisième paramètre avec '_' car non utilisés ici.
    // `axisValue` (le 2ème paramètre) est le Double que Vico nous donne à formater.
    val bottomAxisValueFormatter = CartesianValueFormatter { _, axisValue, _ ->
        val index = try {
            axisValue.roundToInt()
        } catch (e: Throwable) { -1 }
        entries.getOrNull(index)?.gameDisplayName ?: index.toString()
    }

    val startAxisValueFormatter = CartesianValueFormatter { _, axisValue, _ ->
        val percentage = try {
            axisValue.roundToInt()
        } catch (e: Throwable) { 0 }
        "$percentage%"
    }

    val axisLabelRotationDegrees = 0f
    // Utilisation de MaterialTheme.colorScheme pour les couleurs Vico
    val textComponentColor: Color = MaterialTheme.colorScheme.onSurface
    val columnLineColorVico: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary


    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                // Correction ici: Utilisation de ColumnProvider.series comme dans les exemples Vico v2.1.3
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        color = columnLineColorVico, // Doit être androidx.compose.ui.graphics.Color
                        thickness = 16.dp
                        // shape = RoundedCornerShape(bottomStartPercent = 25, bottomEndPercent = 25) // Optionnel pour coins arrondis
                    )
                )
                // Le paramètre `spacing` de rememberColumnCartesianLayer est pour l'espacement entre groupes de barres (multi-séries)
                // Pour l'espacement entre barres d'une même série, c'est géré autrement ou par la largeur des barres.
                // On peut ajouter `seriesSpacing` à ColumnProvider.series si nécessaire et disponible.
            ),
            startAxis = rememberStartAxis( // API Corrigée
                title = stringResource(R.string.stats_percentage_axis_title), // "Percentage (%)"
                valueFormatter = startAxisValueFormatter,
                titleComponent = rememberTextComponent(
                    color = textComponentColor, // Couleur du texte du titre de l'axe
                    padding = Dimensions(horizontal = 2.dp, vertical = 2.dp), // Corrigé: Utilisation de Dimensions de Vico
                    textSize = 10.sp
                    // typeface = Typeface.MONOSPACE, // Optionnel
                )
            ),
            bottomAxis = rememberBottomAxis( // API Corrigée
                valueFormatter = bottomAxisValueFormatter,
                labelRotationDegrees = axisLabelRotationDegrees,
                titleComponent = rememberTextComponent(
                    color = textComponentColor,
                    padding = Dimensions(horizontal = 2.dp, vertical = 2.dp), // Corrigé: Utilisation de Dimensions de Vico
                    textSize = 10.sp
                ),
                guideline = null
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun StatRow(statItem: StatItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statItem.displayResult,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.stats_count_format, statItem.count), // "Count: %d"
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            text = stringResource(R.string.stats_percentage_format, statItem.percentage), // "%.1f%%"
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Divider()
}

val GameType.displayNameResourceId: Int
    get() = when (this) {
        GameType.COIN_FLIP -> R.string.coin_flip_screen_title
        GameType.MAGIC_EIGHT_BALL -> R.string.magic_ball_screen_title
        GameType.DICE_ROLL -> R.string.dice_roll_screen_title
        GameType.ROCK_PAPER_SCISSORS -> R.string.rps_screen_title
    }

@Preview(showBackground = true)
@Composable
fun GameStatsScreenPreview_WithGlobalChart() {
    val previewStats = GameStatsData(title = "Global Statistics (Preview)", totalPlays = 100, statItems = listOf(StatItem(GameType.COIN_FLIP, "HEADS", "Heads!", 30, 75.0f), StatItem(GameType.COIN_FLIP, "TAILS", "Tails!", 10, 25.0f), StatItem(GameType.DICE_ROLL, "4", "4", 60, 100.0f)))
    val previewGlobalShares = listOf(GameGlobalShareEntry(GameType.COIN_FLIP, "Coin Flip", 40f, 40), GameGlobalShareEntry(GameType.DICE_ROLL, "Dice Roll", 60f, 60))
    DivinationAppTheme { AppScaffold(title = "Preview Stats avec Graph", canNavigateBack = false, onNavigateBack = {}) { paddingValues -> GameStatsContent(statsData = previewStats, globalGameShares = previewGlobalShares, specificGameType = null, modifier = Modifier.padding(paddingValues)) } }
}

@Preview(showBackground = true)
@Composable
fun GameStatsScreenPreview_SpecificGame() {
    val previewStats = GameStatsData(title = "Statistics: Coin Flip (Preview)", totalPlays = 10, statItems = listOf(StatItem(GameType.COIN_FLIP, "HEADS", "Heads!", 6, 60.0f), StatItem(GameType.COIN_FLIP, "TAILS", "Tails!", 4, 40.0f)))
    DivinationAppTheme { AppScaffold(title = "Preview Stats Spécifiques", canNavigateBack = false, onNavigateBack = {}) { paddingValues -> GameStatsContent(statsData = previewStats, globalGameShares = emptyList(), specificGameType = GameType.COIN_FLIP, modifier = Modifier.padding(paddingValues)) } }
}

