package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.GameStatsData
import fr.antoinehory.divination.viewmodels.GameStatsViewModel
import fr.antoinehory.divination.viewmodels.GameStatsViewModelFactory
import fr.antoinehory.divination.viewmodels.StatItem

@Composable
fun GameStatsScreen(
    onNavigateBack: () -> Unit,
    specificGameType: GameType? // Reçu via la navigation
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    val viewModel: GameStatsViewModel = viewModel(
        factory = GameStatsViewModelFactory(application, launchLogRepository, specificGameType)
    )

    val statsData by viewModel.statsData.collectAsState()

    AppScaffold(
        title = statsData?.title ?: stringResource(id = R.string.stats_screen_title_global), // Titre par défaut pendant le chargement
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        GameStatsContent(
            statsData = statsData,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun GameStatsContent(
    statsData: GameStatsData?,
    modifier: Modifier = Modifier
) {
    if (statsData == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Afficher pendant le chargement
        }
        return
    }

    if (statsData.isEmpty) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.stats_no_data_available), // Vous devrez ajouter cette chaîne
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.stats_total_plays, statsData.totalPlays), // Vous devrez ajouter cette chaîne
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Si ce sont des stats globales, on pourrait grouper par jeu
        if (statsData.statItems.any { it.gameType != statsData.statItems.first().gameType } && statsData.statItems.isNotEmpty()) { // Détecte si plusieurs GameTypes
            val groupedByGame = statsData.statItems.groupBy { it.gameType }
            groupedByGame.forEach { (gameType, items) ->
                item {
                    Text(
                        text = LocalContext.current.getString(gameType.displayNameResourceId), // Vous devrez ajouter displayNameResourceId à GameType
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(items) { statItem ->
                    StatRow(statItem)
                }
            }
        } else { // Stats pour un jeu spécifique ou si tous les items sont du même jeu (cas stats globales avec un seul jeu joué)
            items(statsData.statItems) { statItem ->
                StatRow(statItem)
            }
        }
    }
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
            text = stringResource(R.string.stats_count_format, statItem.count), // Ex: "Count: %d"
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            // Format du pourcentage avec une décimale
            text = stringResource(R.string.stats_percentage_format, statItem.percentage), // Ex: "%.1f%%"
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Divider()
}

// Ajout nécessaire à votre enum GameType
val GameType.displayNameResourceId: Int
    get() = when (this) {
        GameType.COIN_FLIP -> R.string.coin_flip_screen_title
        GameType.MAGIC_EIGHT_BALL -> R.string.magic_ball_screen_title
        GameType.DICE_ROLL -> R.string.dice_roll_screen_title
        GameType.ROCK_PAPER_SCISSORS -> R.string.rps_screen_title
    }


@Preview(showBackground = true)
@Composable
fun GameStatsScreenPreview_WithData() {
    val previewStats = GameStatsData(
        title = "Statistics: Coin Flip",
        totalPlays = 10,
        statItems = listOf(
            StatItem(GameType.COIN_FLIP, "HEADS", "Heads!", 6, 60.0f),
            StatItem(GameType.COIN_FLIP, "TAILS", "Tails!", 4, 40.0f)
        )
    )
    DivinationAppTheme {
        AppScaffold(title = "Preview Stats", canNavigateBack = true, onNavigateBack = {}) { paddingValues ->
            GameStatsContent(statsData = previewStats, modifier = Modifier.padding(paddingValues))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameStatsScreenPreview_NoData() {
    val previewStatsEmpty = GameStatsData(
        title = "Statistics: Dice Roll",
        totalPlays = 0,
        statItems = emptyList(),
        isEmpty = true
    )
    DivinationAppTheme {
        AppScaffold(title = "Preview Stats", canNavigateBack = true, onNavigateBack = {}) { paddingValues ->
            GameStatsContent(statsData = previewStatsEmpty, modifier = Modifier.padding(paddingValues))
        }
    }
}
