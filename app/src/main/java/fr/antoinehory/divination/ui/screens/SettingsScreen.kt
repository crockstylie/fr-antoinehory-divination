package fr.antoinehory.divination.ui.screens

// import android.app.Application // Pas nécessaire si non utilisé dans la factory
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
// import androidx.compose.ui.graphics.Color // Plus utilisé si SettingTextItem est supprimé
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

@Composable
fun GameType.getDisplayName(): String { // Reste identique
    return when (this) {
        GameType.COIN_FLIP -> stringResource(id = R.string.coin_flip_screen_title)
        GameType.DICE_ROLL -> stringResource(id = R.string.dice_roll_screen_title)
        GameType.MAGIC_EIGHT_BALL -> stringResource(id = R.string.magic_ball_screen_title)
        GameType.ROCK_PAPER_SCISSORS -> stringResource(id = R.string.rps_screen_title)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    val showClearConfirmationDialog by settingsViewModel.showClearConfirmationDialog.collectAsState()
    val selectedOptionForClearFromVM by settingsViewModel.selectedGameOrOptionForClear.collectAsState()

    var expandedGameSelection by remember { mutableStateOf(false) }
    var isAllGamesOptionActive by rememberSaveable { mutableStateOf(false) }

    // Gérer la réinitialisation de isAllGamesOptionActive si selectedOptionForClearFromVM est explicitement un jeu (pas null)
    // Cela arrive si l'utilisateur sélectionne un jeu après avoir sélectionné "Tous les jeux".
    // La réinitialisation après suppression (où selectedOptionForClearFromVM devient null)
    // est gérée dans le onClick du bouton de confirmation du dialogue.
    LaunchedEffect(selectedOptionForClearFromVM) {
        if (selectedOptionForClearFromVM != null) {
            isAllGamesOptionActive = false
        }
    }

    if (showClearConfirmationDialog) {
        val title: String
        val message: String

        // Déterminer le message basé sur ce qui est effectivement sélectionné pour l'action
        val isClearingAllGames = selectedOptionForClearFromVM == null && isAllGamesOptionActive
        val isClearingSpecificGame = selectedOptionForClearFromVM != null

        if (isClearingAllGames) {
            title = stringResource(id = R.string.settings_clear_stats_for_all_games_dialog_title)
            message = stringResource(id = R.string.settings_clear_stats_for_all_games_dialog_message)
        } else if (isClearingSpecificGame) {
            val gameName = selectedOptionForClearFromVM?.getDisplayName() ?: ""
            title = stringResource(id = R.string.settings_clear_stats_for_game_dialog_title, gameName)
            message = stringResource(id = R.string.settings_clear_stats_for_game_dialog_message, gameName)
        } else {
            // Cas de fallback / placeholder, le dialogue ne devrait pas s'afficher si le bouton est désactivé
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
                    isAllGamesOptionActive = false // Réinitialiser pour afficher le placeholder après l'action
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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // --- Section Mode d'interaction (inchangée) ---
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
                val isEnabled = when (mode) {
                    InteractionMode.SHAKE -> isShakeAvailable
                    InteractionMode.TAP -> true
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
            if (!isShakeAvailable) {
                Text(
                    stringResource(id = R.string.settings_interaction_mode_shake_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                stringResource(id = R.string.settings_data_management_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- Section modifiée pour effacer les statistiques ---
            Text(
                stringResource(id = R.string.settings_clear_stats_for_game_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val allGamesOptionText = stringResource(id = R.string.settings_all_games_option)
            val placeholderText = stringResource(id = R.string.settings_select_game_placeholder)

            val displayedTextInDropdown = when {
                selectedOptionForClearFromVM != null -> selectedOptionForClearFromVM!!.getDisplayName() // Un jeu spécifique est sélectionné
                isAllGamesOptionActive -> allGamesOptionText // "Tous les jeux" a été activement sélectionné
                else -> placeholderText // État placeholder/initial
            }

            ExposedDropdownMenuBox(
                expanded = expandedGameSelection,
                onExpandedChange = { expandedGameSelection = !expandedGameSelection },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = displayedTextInDropdown,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.settings_game_to_clear_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGameSelection) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(
                    expanded = expandedGameSelection,
                    onDismissRequest = { expandedGameSelection = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(allGamesOptionText) },
                        onClick = {
                            settingsViewModel.onGameOrOptionSelectedForClear(null) // VM utilise null pour "Tous les jeux"
                            isAllGamesOptionActive = true // L'UI sait que "Tous les jeux" est activement choisi
                            expandedGameSelection = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    settingsViewModel.availableGameTypes.forEach { gameType ->
                        DropdownMenuItem(
                            text = { Text(gameType.getDisplayName()) },
                            onClick = {
                                settingsViewModel.onGameOrOptionSelectedForClear(gameType)
                                isAllGamesOptionActive = false // Un jeu spécifique est choisi
                                expandedGameSelection = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // S'assurer que le dialogue ne s'affiche pas pour le placeholder
                    if (displayedTextInDropdown != placeholderText) {
                        settingsViewModel.onClearSelectedStatsClicked()
                    }
                },
                enabled = displayedTextInDropdown != placeholderText,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.settings_clear_stats_for_selected_game_button))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

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
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen Portrait")
@Composable
fun SettingsScreenPreview() {
    DivinationAppTheme {
        SettingsScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Settings Screen Landscape")
@Composable
fun SettingsScreenPreviewLandscape() {
    DivinationAppTheme {
        SettingsScreen(onNavigateBack = {})
    }
}
