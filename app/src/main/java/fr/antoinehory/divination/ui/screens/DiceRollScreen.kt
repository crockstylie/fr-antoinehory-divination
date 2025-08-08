package fr.antoinehory.divination.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons // Assurer cet import
import androidx.compose.material.icons.filled.Lock // Assurer cet import
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.painterResource // Non requis si on utilise Icons.Filled.Lock
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.DiceRollViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.repository.UserPreferencesRepository
import fr.antoinehory.divination.viewmodels.DiceRollViewModelFactory
import fr.antoinehory.divination.viewmodels.IndividualDiceRollResult
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiceRollScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit,
    onNavigateToDiceSetManagement: () -> Unit,
    onNavigateToInfo: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication

    val userPreferencesRepository = UserPreferencesRepository(context.applicationContext)
    val diceSetDao = AppDatabase.getDatabase(context.applicationContext).diceSetDao()

    val diceRollViewModel: DiceRollViewModel = viewModel(
        factory = DiceRollViewModelFactory(
            application = context.applicationContext as Application,
            launchLogRepository = application.launchLogRepository,
            userPreferencesRepository = userPreferencesRepository,
            diceSetDao = diceSetDao
        )
    )

    val currentMessage by diceRollViewModel.currentMessage.collectAsState()
    val activeDiceSet by diceRollViewModel.activeDiceSet.collectAsState()
    val diceResults by diceRollViewModel.diceResults.collectAsState()
    val isRolling by diceRollViewModel.isRolling.collectAsState()
    val recentLogs by diceRollViewModel.recentLogs.collectAsState()
    val totalRollValue by diceRollViewModel.totalRollValue.collectAsState()

    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    LaunchedEffect(interactionViewModel, diceRollViewModel, isRolling) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isRolling) {
                diceRollViewModel.performRoll()
            }
        }
    }

    val resultsAlpha by animateFloatAsState(
        targetValue = if (isRolling && diceResults.any { !it.isLocked && it.value == 0 } ) 0.3f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "diceResultsAlpha"
    )

    AppScaffold(
        title = stringResource(id = R.string.dice_roll_screen_title) + (activeDiceSet?.name?.let { " ($it)" } ?: ""),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        bottomBar = {
            BottomAppNavigationBar(
                onSettingsClick = onNavigateToDiceSetManagement,
                onStatsClick = { onNavigateToStats(GameType.DICE_ROLL) },
                onInfoClick = onNavigateToInfo
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .clickable {
                    if (!isRolling) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // CORRECTION: Message accéléromètre avec le texte
            if (interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable) {
                Text(
                    text = stringResource(id = R.string.dice_accelerometer_not_available_ui_message),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isRolling && diceResults.all { it.value == 0 && !it.isLocked } && diceResults.isNotEmpty()) {
                Text(
                    stringResource(R.string.dice_rolling_message),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(resultsAlpha)
                )
            } else if (diceResults.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .alpha(resultsAlpha),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5
                ) {
                    diceResults.forEachIndexed { index, result ->
                        DiceResultDisplay(
                            result = result,
                            index = index,
                            onClick = { idx -> diceRollViewModel.toggleLockState(idx) },
                            modifier = Modifier.sizeIn(minWidth = 60.dp, minHeight = 60.dp)
                        )
                    }
                }
                totalRollValue?.let { total ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.total_roll_value_format, total),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(resultsAlpha)
                    )
                }
            } else {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) { /* Placeholder */ }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                currentMessage,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            GameHistoryDisplay(recentLogs = recentLogs, gameType = GameType.DICE_ROLL)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DiceResultDisplay(
    result: IndividualDiceRollResult,
    index: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (result.isLocked) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        OrakniumGold
    }
    val textColor = if (result.isLocked) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        OrakniumBackground
    }

    Box(
        modifier = modifier
            .alpha(if (result.isLocked) 0.7f else 1f)
            .clickable { onClick(index) }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (result.value == 0 && !result.isLocked && result.diceType.sides > 0) "..." else "${result.value}/${result.diceType.sides}",
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        if (result.isLocked) {
            Icon(
                // CORRECTION: Utilisation de Icons.Filled.Lock
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.locked_dice_icon_description),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .padding(2.dp),
                tint = OrakniumGold
            )
        }
    }
}

// ... Previews ...
