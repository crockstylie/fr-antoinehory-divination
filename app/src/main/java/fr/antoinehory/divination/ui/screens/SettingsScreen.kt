package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.SettingsViewModel
import fr.antoinehory.divination.viewmodels.SettingsViewModelFactory

/**
 * Extension composable function for [GameType] to get its localized display name.
 * This is used to present game types in a user-friendly format in the UI.
 *
 * @return A [String] representing the localized display name of the game type.
 */
@Composable
fun GameType.getDisplayName(): String {
    return when (this) {
        GameType.COIN_FLIP -> stringResource(id = R.string.coin_flip_screen_title)
        GameType.DICE_ROLL -> stringResource(id = R.string.dice_roll_screen_title)
        GameType.MAGIC_EIGHT_BALL -> stringResource(id = R.string.magic_ball_screen_title)
        GameType.ROCK_PAPER_SCISSORS -> stringResource(id = R.string.rps_screen_title)
        // Add other game types here if necessary.
    }
}

/**
 * Composable screen for application settings.
 * Allows users to configure interaction preferences (tap vs. shake) and manage application data,
 * such as clearing game statistics.
 *
 * @param onNavigateBack Callback to navigate to the previous screen.
 * @param interactionViewModel ViewModel for managing interaction mode preferences and availability.
 *                           Defaults to an instance provided by [viewModel].
 * @param settingsViewModel ViewModel for managing data-related settings, like clearing statistics.
 *                          Defaults to an instance created by [SettingsViewModelFactory].
 */
@OptIn(ExperimentalMaterial3Api::class) // For ExposedDropdownMenuBox, AlertDialog, etc.
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            (LocalContext.current.applicationContext as DivinationApplication).launchLogRepository
        )
    )
) {
    // Collect states from InteractionDetectViewModel.
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    // Collect states from SettingsViewModel.
    val showClearConfirmationDialog by settingsViewModel.showClearConfirmationDialog.collectAsState()
    val selectedOptionForClearFromVM by settingsViewModel.selectedGameOrOptionForClear.collectAsState()

    // UI state for the game selection dropdown.
    var expandedGameSelection by remember { mutableStateOf(false) }
    // UI state to track if "All Games" option is active in the dropdown logic (independent of VM selection).
    var isAllGamesOptionActive by rememberSaveable { mutableStateOf(false) }

    // Effect to reset isAllGamesOptionActive if a specific game is selected from the ViewModel.
    // This ensures that if the VM loads a specific game (e.g., from saved state), the local "All Games"
    // flag is correctly turned off.
    LaunchedEffect(selectedOptionForClearFromVM) {
        if (selectedOptionForClearFromVM != null) {
            isAllGamesOptionActive = false
        }
    }

    // Display a confirmation dialog when attempting to clear statistics.
    if (showClearConfirmationDialog) {
        val title: String
        val message: String

        // Determine if clearing all games or a specific game based on ViewModel state and local UI state.
        val isClearingAllGames = selectedOptionForClearFromVM == null && isAllGamesOptionActive
        val isClearingSpecificGame = selectedOptionForClearFromVM != null

        // Set dialog title and message based on the clearing action.
        if (isClearingAllGames) {
            title = stringResource(id = R.string.settings_clear_stats_for_all_games_dialog_title)
            message = stringResource(id = R.string.settings_clear_stats_for_all_games_dialog_message)
        } else if (isClearingSpecificGame) {
            val gameName = selectedOptionForClearFromVM?.getDisplayName() ?: ""
            title = stringResource(id = R.string.settings_clear_stats_for_game_dialog_title, gameName)
            message = stringResource(id = R.string.settings_clear_stats_for_game_dialog_message, gameName)
        } else {
            // Should not happen if dialog is shown, but provides default empty strings.
            title = ""
            message = ""
        }

        AlertDialog(
            onDismissRequest = { settingsViewModel.onDismissClearConfirmationDialog() },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.onConfirmClearSelectedStats()
                    // Reset local "All Games" active flag to ensure placeholder shows after action.
                    isAllGamesOptionActive = false
                }) {
                    Text(stringResource(id = R.string.settings_clear_stats_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { settingsViewModel.onDismissClearConfirmationDialog() }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    AppScaffold(
        title = stringResource(id = R.string.settings_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from the Scaffold.
                .verticalScroll(rememberScrollState()) // Allow content to scroll.
                .padding(horizontal = 16.dp, vertical = 8.dp) // Inner padding for content.
        ) {
            // Section for Interaction Mode settings.
            Text(
                stringResource(id = R.string.settings_interaction_mode_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            val interactionOptions = listOf(
                InteractionMode.TAP to stringResource(R.string.settings_interaction_mode_tap),
                InteractionMode.SHAKE to stringResource(R.string.settings_interaction_mode_shake)
            )
            interactionOptions.forEach { (mode, label) ->
                // Enable/disable shake option based on sensor availability.
                val isEnabled = when (mode) {
                    InteractionMode.SHAKE -> isShakeAvailable
                    InteractionMode.TAP -> true // Tap is always available.
                }
                SettingRadioItem(
                    title = label,
                    selected = interactionPrefs.activeInteractionMode == mode,
                    onClick = {
                        if (isEnabled) {
                            interactionViewModel.setActiveInteractionMode(mode)
                        }
                    },
                    enabled = isEnabled
                )
            }
            // Display a message if shake interaction is not available.
            if (!isShakeAvailable) {
                Text(
                    stringResource(id = R.string.settings_interaction_mode_shake_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) // Visual separator.

            // Section for Data Management settings.
            Text(
                stringResource(id = R.string.settings_data_management_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                stringResource(id = R.string.settings_clear_stats_for_game_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dropdown menu for selecting a game to clear stats for, or "All Games".
            val allGamesOptionText = stringResource(id = R.string.settings_all_games_option)
            val placeholderText = stringResource(id = R.string.settings_select_game_placeholder)

            // Determine the text displayed in the dropdown anchor based on selection.
            val displayedTextInDropdown = when {
                selectedOptionForClearFromVM != null -> selectedOptionForClearFromVM!!.getDisplayName()
                isAllGamesOptionActive -> allGamesOptionText
                else -> placeholderText
            }

            ExposedDropdownMenuBox(
                expanded = expandedGameSelection,
                onExpandedChange = { expandedGameSelection = !expandedGameSelection },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = displayedTextInDropdown,
                    onValueChange = {}, // Read-only field.
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.settings_game_to_clear_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGameSelection) },
                    modifier = Modifier
                        .menuAnchor() // Required for ExposedDropdownMenuBox.
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(
                    expanded = expandedGameSelection,
                    onDismissRequest = { expandedGameSelection = false }
                ) {
                    // "All Games" option in the dropdown.
                    DropdownMenuItem(
                        text = { Text(allGamesOptionText) },
                        onClick = {
                            settingsViewModel.onGameOrOptionSelectedForClear(null) // Null for all games.
                            isAllGamesOptionActive = true // Set local flag.
                            expandedGameSelection = false // Close dropdown.
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    // Individual game type options in the dropdown.
                    settingsViewModel.availableGameTypes.forEach { gameType ->
                        DropdownMenuItem(
                            text = { Text(gameType.getDisplayName()) },
                            onClick = {
                                settingsViewModel.onGameOrOptionSelectedForClear(gameType)
                                isAllGamesOptionActive = false // Unset local flag.
                                expandedGameSelection = false // Close dropdown.
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button to trigger the clearing of selected stats.
            Button(
                onClick = {
                    // Only trigger if a valid option (not the placeholder) is selected.
                    if (displayedTextInDropdown != placeholderText) {
                        settingsViewModel.onClearSelectedStatsClicked()
                    }
                },
                enabled = displayedTextInDropdown != placeholderText, // Enable button only if not placeholder.
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Error color for delete action.
            ) {
                Text(stringResource(id = R.string.settings_clear_stats_for_selected_game_button))
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer at the end of the column.
        }
    }
}

/**
 * A composable that represents a single radio button item in a settings list.
 * It includes a title, a radio button, and handles click events.
 *
 * @param title The text to display next to the radio button.
 * @param selected Boolean indicating whether this radio item is currently selected.
 * @param onClick Lambda function to be invoked when the item is clicked.
 * @param modifier Optional [Modifier] to be applied to the root Row of this item.
 * @param enabled Boolean indicating whether this radio item is enabled and can be interacted with. Defaults to true.
 */
@Composable
fun SettingRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.RadioButton // Semantics for accessibility.
            )
            .padding(vertical = 12.dp), // Vertical padding for the clickable area.
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // onClick is handled by the Row's clickable modifier.
            enabled = enabled,
            modifier = Modifier.size(24.dp) // Fixed size for the RadioButton.
        )
        Spacer(Modifier.width(16.dp)) // Space between RadioButton and text.
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            // Adjust text color based on enabled state for visual feedback.
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.weight(1f) // Text takes remaining space.
        )
    }
}

/**
 * Preview composable for the [SettingsScreen] in portrait orientation.
 */
@Preview(showBackground = true, name = "Settings Screen Portrait")
@Composable
fun SettingsScreenPreview() {
    DivinationAppTheme {
        SettingsScreen(onNavigateBack = {}) // Empty lambda for onNavigateBack in preview.
    }
}

/**
 * Preview composable for the [SettingsScreen] in landscape orientation.
 * Demonstrates how the screen might look with different width/height ratios.
 */
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Settings Screen Landscape")
@Composable
fun SettingsScreenPreviewLandscape() {
    DivinationAppTheme {
        SettingsScreen(onNavigateBack = {}) // Empty lambda for onNavigateBack in preview.
    }
}

