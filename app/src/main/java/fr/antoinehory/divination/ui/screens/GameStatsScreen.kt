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
import androidx.compose.ui.graphics.drawscope.Stroke // Import pour la bordure
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
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.GameGlobalShareEntry
import fr.antoinehory.divination.viewmodels.GameStatsData
import fr.antoinehory.divination.viewmodels.GameStatsViewModel
import fr.antoinehory.divination.viewmodels.GameStatsViewModelFactory
import fr.antoinehory.divination.viewmodels.StatItem
import android.graphics.Paint as AndroidPaint

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
            0.5f // Couleur du milieu si une seule tranche
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
    borderWidthDp: Dp = 1.5.dp // Épaisseur de la bordure
) {
    if (statItems.isEmpty()) return

    val sortedItems = statItems.sortedByDescending { it.percentage }
    // Utiliser la somme des comptes pour la proportion, car les pourcentages peuvent être déjà normalisés par jeu
    val totalCount = sortedItems.sumOf { it.count }.toFloat().coerceAtLeast(0.001f)
    val density = LocalDensity.current
    val borderWidthPx = with(density) { borderWidthDp.toPx() }

    Box(modifier = modifier
        .aspectRatio(1f)
        .padding(8.dp) // Laisse de l'espace pour que la bordure ne soit pas coupée
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var currentStartAngle = -90f

            sortedItems.forEachIndexed { index, item ->
                // Calculer le sweepAngle basé sur le compte de l'item par rapport au totalCount du groupe
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

            // Dessiner la bordure APRÈS les tranches
            if (borderWidthPx > 0f) {
                drawArc(
                    color = borderColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false, // Important pour une bordure (style "Stroke")
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

    AppScaffold(
        title = statsData?.title ?: stringResource(id = R.string.stats_screen_title_global_loading),
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Affichage du total des lancers pour l'écran actuel (global ou spécifique)
        // Ce total est celui de statsData.totalPlays, qui est déjà correct
        // pour les stats globales et pour les stats spécifiques.
        if (statsData.totalPlays > 0) { // Changé de !statsData.isEmpty à statsData.totalPlays > 0
            item {
                Text(
                    // Pour les stats spécifiques, statsData.title contient déjà le nom du jeu.
                    // Pour les stats globales, ceci affiche le total de tous les jeux.
                    text = stringResource(R.string.stats_total_plays, statsData.totalPlays),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
        }

        // Graphique de distribution globale (barres)
        val chartShown = specificGameType == null && globalGameShares.isNotEmpty()
        if (chartShown) {
            item {
                Text(
                    text = stringResource(R.string.stats_global_distribution_chart_title),
                    style = MaterialTheme.typography.titleLarge,
                    // Ajustement du padding top si le total général est déjà affiché
                    modifier = Modifier.padding(top = if (statsData.totalPlays > 0) 8.dp else 16.dp, bottom = 8.dp)
                )
                GlobalDistributionChart(chartEntries = globalGameShares)
                Spacer(modifier = Modifier.height(16.dp))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // Message "Pas de données disponibles"
        if (statsData.statItems.isEmpty()) { // Simplifié : si pas d'items, alors pas de données à détailler
            // Si le total des jeux était aussi 0, le message est plus proéminent
            if (statsData.totalPlays == 0) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            // S'assurer que le padding est correct par rapport au graphique global possible
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
            // Si statItems est vide, il n'y a rien d'autre à faire dans cette LazyColumn pour les détails.
            return@LazyColumn
        }


        // Détail des statistiques
        if (statsData.statItems.isNotEmpty()) {
            val displayItems = statsData.statItems
            // CORRECTION PRINCIPALE ICI:
            // Pour les stats globales, on groupe toujours.
            // Pour les stats spécifiques, on n'a pas besoin de grouper car displayItems ne contient que ce jeu.
            val shouldGroupByGame = specificGameType == null

            if (shouldGroupByGame) { // Cas Global: on itère sur les jeux groupés
                val groupedByGame = displayItems.groupBy { it.gameType }
                groupedByGame.entries.forEachIndexed { groupIndex, (gameType, itemsInGroup) ->
                    // S'assurer qu'il y a des items pour ce groupe (devrait toujours être vrai si displayItems n'est pas vide)
                    if (itemsInGroup.isNotEmpty()) {
                        val itemColors = getStatItemColors(itemsInGroup, OrakniumGold, OrakniumBackground)
                        val totalPlaysForThisGame = itemsInGroup.sumOf { it.count }
                        item {
                            Text(
                                text = LocalContext.current.getString(gameType.displayNameResourceId),
                                style = MaterialTheme.typography.headlineSmall, // Titre du jeu
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    // Ajuster le padding top en fonction de si le graphique de barres a été montré
                                    top = if (chartShown || groupIndex > 0) 16.dp else 8.dp,
                                    bottom = 4.dp
                                )
                            )
                            Text(
                                text = stringResource(R.string.stats_total_plays_for_game, totalPlaysForThisGame), // Total pour ce jeu
                                style = MaterialTheme.typography.titleLarge, // Style comme le total général
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // Camembert pour ce jeu
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
                        // Liste des résultats pour ce jeu
                        items(itemsInGroup, key = { it.gameType.name + it.resultKey }) { statItem ->
                            StatRow(
                                statItem = statItem,
                                legendColor = itemColors[statItem.resultKey]
                            )
                        }

                        if (groupIndex < groupedByGame.size - 1) {
                            item {
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            } else { // Cas Spécifique: displayItems contient les stats du jeu unique
                val itemColors = getStatItemColors(displayItems, OrakniumGold, OrakniumBackground)
                // Le titre de l'écran et le total des jeux sont déjà gérés en haut
                // Camembert pour le jeu spécifique
                if (displayItems.isNotEmpty()) { // S'assurer qu'il y a des items avant de montrer le PieChart
                    item {
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
                }
                // Liste des résultats pour le jeu spécifique
                items(displayItems, key = { it.gameType.name + it.resultKey }) { statItem ->
                    // Pas besoin de la logique isFirstItem et du padding conditionnel compliqué ici,
                    // car la structure est plus simple pour l'écran spécifique.
                    StatRow(
                        statItem = statItem,
                        legendColor = itemColors[statItem.resultKey]
                        // modifier = Modifier (si un padding spécifique est nécessaire pour la première row)
                    )
                }
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
                        .size(10.dp) // Taille du carré de légende
                        .background(legendColor)
                )
                Spacer(Modifier.width(8.dp)) // Espace entre la légende et le texte
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
        Divider()
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
    val barColor = MaterialTheme.colorScheme.primary // Couleur des barres du graphique de distribution

    val gameNameTextColor = MaterialTheme.colorScheme.onSurface
    val gameNameTextPaint = remember(gameNameTextColor, density) {
        AndroidPaint().apply {
            color = gameNameTextColor.toArgb()
            textAlign = AndroidPaint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
        }
    }

    val valueTextColorInBar = OrakniumBackground // Couleur du texte à l'intérieur des barres
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
    val topPadding = with(density) { 8.dp.toPx() } // Espace au-dessus des barres
    val spacingBetweenBars = with(density) { 8.dp.toPx() }
    val valueTextMarginFromBarBottomPx = with(density) { 4.dp.toPx() } // Marge du texte de valeur par rapport au bas de la barre

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(220.dp) // Hauteur fixe pour le graphique de distribution
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val numBars = chartEntries.size

        val totalSpacing = spacingBetweenBars * (numBars - 1).coerceAtLeast(0)
        val barWidth = if (numBars > 0) (canvasWidth - totalSpacing) / numBars else 0f

        if (barWidth <= 0f && numBars > 0) return@Canvas // Ne rien dessiner si la largeur de barre est invalide

        // S'assurer que maxPercentage est au moins 1f pour éviter la division par zéro si tous les pourcentages sont à 0
        val maxPercentage = chartEntries.maxOfOrNull { it.sharePercentage }?.takeIf { it > 0f } ?: 1f
        val chartAreaHeight = canvasHeight - topPadding - bottomPaddingForGameName

        if (chartAreaHeight <= 0f) return@Canvas // Ne rien dessiner si la zone de graphique est invalide

        val textBounds = android.graphics.Rect() // Pour mesurer le texte

        chartEntries.forEachIndexed { index, entry ->
            val barHeight = (entry.sharePercentage / maxPercentage) * chartAreaHeight
            val barLeft = index * (barWidth + spacingBetweenBars)
            val barTopY = canvasHeight - bottomPaddingForGameName - barHeight // Coordonnée Y du haut de la barre
            val barBottomY = canvasHeight - bottomPaddingForGameName // Coordonnée Y du bas de la barre

            // Dessiner la barre
            drawRect(
                color = barColor,
                topLeft = Offset(x = barLeft, y = barTopY),
                size = Size(barWidth.coerceAtLeast(0f), barHeight.coerceAtLeast(0f))
            )

            // Afficher le nombre total de jeux dans la barre si elle est assez haute
            val valueText = entry.totalPlaysForGame.toString()
            valueTextPaint.getTextBounds(valueText, 0, valueText.length, textBounds)
            val valueTextHeight = textBounds.height()

            // Condition pour afficher le texte dans la barre (si la barre est assez haute)
            if (barHeight > valueTextHeight + (2 * valueTextMarginFromBarBottomPx)) {
                val valueTextBaselineY = barBottomY - valueTextMarginFromBarBottomPx - textBounds.bottom
                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    barLeft + barWidth / 2, // Centrer le texte horizontalement dans la barre
                    valueTextBaselineY,     // Positionner la baseline du texte
                    valueTextPaint
                )
            }

            // Afficher le nom du jeu sous la barre
            val gameNameText = entry.gameDisplayName
            // Positionner la baseline du nom du jeu
            val gameNameBaselineY = canvasHeight - bottomPaddingForGameName + with(density) { 15.dp.toPx() } // Ajuster pour l'alignement vertical

            drawContext.canvas.nativeCanvas.drawText(
                gameNameText,
                barLeft + barWidth / 2, // Centrer le nom du jeu horizontalement sous la barre
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
    DivinationAppTheme {
        AppScaffold(title = "Aperçu Stats (Spécifique)", canNavigateBack = true, onNavigateBack = {}) { paddingValues ->
            GameStatsContent(
                statsData = previewStats,
                globalGameShares = emptyList(),
                specificGameType = GameType.COIN_FLIP,
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
        isEmpty = true // Assurez-vous que isEmpty est correctement défini si vous l'utilisez
    )
    DivinationAppTheme {
        AppScaffold(title = "Aperçu Stats (Aucune Donnée)", canNavigateBack = true, onNavigateBack = {}) { paddingValues ->
            GameStatsContent(
                statsData = previewStatsEmpty,
                globalGameShares = emptyList(),
                specificGameType = GameType.DICE_ROLL,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

