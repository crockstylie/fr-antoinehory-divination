package fr.antoinehory.divination.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Keep existing wildcard import
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Divider // Corrected from HorizontalDivider if that was a typo in original context, assuming Material3 Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.data.model.GameType
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold

/**
 * Composable screen for the Dice Roll game.
 * It allows users to roll a set of dice, lock individual dice, and view recent roll history.
 * Interactions can be via tap or shake, based on user preference and device capability.
 *
 * @param onNavigateBack Callback to navigate to the previous screen.
 * @param interactionViewModel ViewModel for detecting user interactions (tap/shake). Defaults to a new instance.
 * @param onNavigateToStats Callback to navigate to the statistics screen for the Dice Roll game.
 * @param onNavigateToDiceSetManagement Callback to navigate to the dice set management screen.
 * @param onNavigateToInfo Callback to navigate to the application information screen.
 */
@OptIn(ExperimentalLayoutApi::class) // For FlowRow
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

    // Dependencies for the DiceRollViewModel.
    val userPreferencesRepository = UserPreferencesRepository(context.applicationContext)
    val diceSetDao = AppDatabase.getDatabase(context.applicationContext).diceSetDao()

    // ViewModel for managing dice roll logic and state.
    val diceRollViewModel: DiceRollViewModel = viewModel(
        factory = DiceRollViewModelFactory(
            application = context.applicationContext as Application,
            launchLogRepository = application.launchLogRepository,
            userPreferencesRepository = userPreferencesRepository,
            diceSetDao = diceSetDao
        )
    )

    // Collecting state from DiceRollViewModel.
    val currentMessage by diceRollViewModel.currentMessage.collectAsState()
    val activeDiceSet by diceRollViewModel.activeDiceSet.collectAsState()
    val diceResults by diceRollViewModel.diceResults.collectAsState()
    val isRolling by diceRollViewModel.isRolling.collectAsState()
    val recentLogs by diceRollViewModel.recentLogs.collectAsState()
    val totalRollValue by diceRollViewModel.totalRollValue.collectAsState()
    val hasLockedDice by diceRollViewModel.hasLockedDice.collectAsState()

    // Collecting state from InteractionDetectViewModel.
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    // Triggers a dice roll when an interaction (e.g., shake) is detected and dice are not currently rolling.
    LaunchedEffect(interactionViewModel, diceRollViewModel, isRolling) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isRolling) {
                diceRollViewModel.performRoll()
            }
        }
    }

    // Animation for the alpha of dice results, making them semi-transparent while rolling
    // if any non-locked dice have not yet received a value.
    val resultsAlpha by animateFloatAsState(
        targetValue = if (isRolling && diceResults.any { !it.isLocked && it.value == 0 } ) 0.3f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "diceResultsAlpha" // Label for animation tooling
    )

    AppScaffold(
        title = stringResource(id = R.string.dice_roll_screen_title) + (activeDiceSet?.name?.let { " ($it)" } ?: ""),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        bottomBar = {
            BottomAppNavigationBar(
                onSettingsClick = onNavigateToDiceSetManagement, // "Settings" button navigates to dice set management.
                onStatsClick = { onNavigateToStats(GameType.DICE_ROLL) },
                onInfoClick = onNavigateToInfo
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Allows content to scroll if it exceeds screen height.
                .padding(16.dp)
                .clickable { // Handles tap interaction for rolling dice.
                    if (!isRolling) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display a warning if shake interaction is preferred but accelerometer is not available.
            if (interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable) {
                Text(
                    text = stringResource(id = R.string.dice_accelerometer_not_available_ui_message),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button to unlock all dice, shown if any dice are locked and results are present.
            if (hasLockedDice && diceResults.isNotEmpty()) {
                TextButton(
                    onClick = { diceRollViewModel.unlockAllDice() },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(stringResource(id = R.string.unlock_all_dice_button))
                }
            } else {
                // Spacer to maintain layout consistency when the unlock button is not visible.
                Spacer(modifier = Modifier.height(8.dp + 36.dp)) // 36dp is approx. TextButton height
            }

            // Display "Rolling..." message or the dice results.
            if (isRolling && diceResults.all { it.value == 0 && !it.isLocked } && diceResults.isNotEmpty()) {
                // Shown when initially rolling and no non-locked dice have values yet.
                Text(
                    stringResource(R.string.dice_rolling_message),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(resultsAlpha).fillMaxHeight(0.3f) // Occupy some vertical space
                )
            } else if (diceResults.isNotEmpty()) {
                // Display individual dice results using FlowRow for responsiveness.
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .alpha(resultsAlpha), // Apply fade effect while rolling.
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5 // Adjust based on desired layout.
                ) {
                    diceResults.forEachIndexed { index, result ->
                        DiceResultDisplay(
                            result = result,
                            index = index,
                            onClick = { idx -> diceRollViewModel.toggleLockState(idx) }, // Toggle lock on click.
                            modifier = Modifier.sizeIn(minWidth = 60.dp, minHeight = 60.dp) // Ensure minimum size for dice.
                        )
                    }
                }
                // Display total roll value if available.
                totalRollValue?.let { total ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.total_roll_value_format, total),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(resultsAlpha) // Apply fade effect.
                    )
                }
            } else {
                // Placeholder Box to maintain layout consistency when no results are shown (e.g., initial state).
                Box(modifier = Modifier.size(120.dp).fillMaxHeight(0.3f), contentAlignment = Alignment.Center) {
                    // Content inside can be an icon or placeholder text if needed
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Main message text (e.g., "Roll!", "You rolled...", or instructions).
            Text(
                currentMessage,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            // Divider before showing recent game history.
            if (recentLogs.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }
            // Display recent game history.
            GameHistoryDisplay(recentLogs = recentLogs, gameType = GameType.DICE_ROLL)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Composable that displays a single dice result.
 * It shows the rolled value, the dice type (e.g., "/20"), and a lock icon if the dice is locked.
 * The appearance changes based on the lock state.
 *
 * @param result The [IndividualDiceRollResult] to display.
 * @param index The index of this dice in the list, used for the onClick callback.
 * @param onClick Lambda function invoked when the dice display is clicked, typically to toggle its lock state.
 * @param modifier [Modifier] to be applied to the outer Box of this composable.
 */
@Composable
fun DiceResultDisplay(
    result: IndividualDiceRollResult,
    index: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine background and text color based on whether the dice is locked.
    val backgroundColor = if (result.isLocked) {
        MaterialTheme.colorScheme.surfaceVariant // Different background for locked dice.
    } else {
        OrakniumGold // Primary color for unlocked dice.
    }
    val textColor = if (result.isLocked) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        OrakniumBackground
    }

    Box(
        modifier = modifier
            .alpha(if (result.isLocked) 0.7f else 1f) // Locked dice are slightly faded.
            .clickable { onClick(index) } // Allow clicking to toggle lock state.
    ) {
        Box(
            modifier = Modifier
                .matchParentSize() // Ensure inner Box matches the size of the outer clickable Box.
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp), // Padding inside the dice display.
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (result.value == 0 && !result.isLocked && result.diceType.sides > 0) {
                    // Display "..." if dice is rolling, not locked, and is a valid dice type.
                    buildAnnotatedString { append("...") }
                } else {
                    // Display "value/sides" (e.g., "15/20").
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)) {
                            append("${result.value}")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontSize = MaterialTheme.typography.bodySmall.fontSize)) {
                            append("/${result.diceType.sides}")
                        }
                    }
                },
                color = textColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Display a lock icon at the top-end if the dice is locked.
        if (result.isLocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.locked_dice_icon_description),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .padding(2.dp), // Small padding around the lock icon.
                tint = OrakniumGold // Ensure lock icon matches theme accent.
            )
        }
    }
}

/**
 * Preview composable for the [DiceRollScreen].
 * This preview shows a placeholder text as the screen relies heavily on ViewModel interactions.
 */
@Preview(showBackground = true)
@Composable
fun DiceRollScreenPreview() {
    // val context = LocalContext.current // Not strictly needed for this simple preview
    DivinationAppTheme {
        Text("DiceRollScreen Preview - ViewModel dependent") // Placeholder for preview
    }
}

